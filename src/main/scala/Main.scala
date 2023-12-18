import bot.MeetingReminderBot
import cats.effect.{ExitCode, IO, IOApp, Resource}
import config.AppConfig
import dao.{MeetingParticipantSql, MeetingSql, NotificationSql, UserSql}
import doobie.Transactor
import io.github.liquibase4s.Liquibase
import io.github.liquibase4s.cats.CatsMigrationHandler._
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.proxy.ProxyServer
import storage._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      config <- Resource.eval(AppConfig.load)
      transactor = Transactor.fromDriverManager[IO](
        config.db.driver,
        config.db.url,
        config.db.user,
        config.db.password
      )
      _ <- Liquibase[IO](config.liquibase).migrate().toResource
      _ <- Resource.eval(IO(println("Some text")))
      storage = MeetingStorage.make(MeetingSql.make, transactor)
      telegramBotBackend = AsyncHttpClientCatsBackend.usingClient[IO](
        asyncHttpClient(
          new DefaultAsyncHttpClientConfig.Builder()
            .setProxyServer(
              new ProxyServer.Builder("127.0.0.1", 2080)
                .build()
            )
            .build()
        )
      )
      bot = MeetingReminderBot[IO](
        config.bot.token,
        telegramBotBackend,
        CommandsAwareStorage[IO](
          PostgresMeetingStorage(MeetingSql.make, transactor),
          PostgresUserStorage(UserSql.make, transactor),
          PostgresMeetingParticipantStorage(MeetingParticipantSql.make, transactor),
          PostgresNotificationStorage(NotificationSql.make, transactor)
        )
      )

      _ <- bot.notifier(IO.sleep(1.second)).start.toResource
      _ <- bot.startPolling().toResource
      /*_ <- MeetingReminderBot(config.bot.token, telegramBotBackend, storage)
        .request(user
          SendMessage(
            1399878184,
            "Intentional message"
          )
        )
        .toResource*/
      _ <- Resource.eval(IO(println(config.liquibase)))
    } yield ()).use_.as(ExitCode.Success)
}

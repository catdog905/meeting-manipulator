import bot.MeetingReminderBot
import cats.effect.std.Random
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.bot4s.telegram.methods.SendMessage
import config.AppConfig
import dao.MeetingSql
import doobie.Transactor
import io.github.liquibase4s.Liquibase
import io.github.liquibase4s.cats.CatsMigrationHandler._
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.proxy.ProxyServer
import storage.{CommandsAwareStorage, InMemoryMeetingParticipantStorage, InMemoryMeetingStorage, InMemoryUserStorage, MeetingStorage}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

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

      random <- Random.scalaUtilRandom[IO].toResource
      bot <- MeetingReminderBot[IO](
        config.bot.token,
        telegramBotBackend,
        CommandsAwareStorage[IO](
          InMemoryMeetingStorage(random),
          InMemoryUserStorage(random),
          InMemoryMeetingParticipantStorage()
        )
      )
        .startPolling()
        .toResource
      /*_ <- MeetingReminderBot(config.bot.token, telegramBotBackend, storage)
        .request(
          SendMessage(
            1399878184,
            "Intentional message"
          )
        )
        .toResource*/
      _ <- Resource.eval(IO(println(config.liquibase)))
    } yield ()).use_.as(ExitCode.Success)
}

package bot

import bot.chatbased.{BotResponse, InMemoryUserChatPool, Panic}
import cats.Applicative
import cats.effect.Async
import cats.implicits.toFunctorOps
import cats.syntax.all._
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models.Message
import domain.ChatId
import error.AppError
import storage.CommandsAwareStorage
import sttp.client3.SttpBackend

case class MeetingReminderBot[F[_]: Async](
  token: String,
  backend: SttpBackend[F, Any],
  commandsAwareStorage: CommandsAwareStorage[F]
) extends TelegramBot[F](token, backend)
  with Polling[F] {
  implicit val implicitCommandsAwareStorage: CommandsAwareStorage[F] = commandsAwareStorage
  val userChatPool: InMemoryUserChatPool[F] = InMemoryUserChatPool[F](commandsAwareStorage)

  override def receiveMessage(msg: Message): F[Unit] = {
    def response(msg: Message): F[BotResponse[_]] =
      for {
        userIdEither <- commandsAwareStorage.userId(ChatId(msg.source))
        botResponse <- userIdEither match {
          case Right(userId)         => userChatPool.updatePoolByUserMessage(userId, msg)
          case Left(error: AppError) => Applicative[F].pure(Panic(error))
        }
      } yield botResponse

    val sendMessageF: BotResponse[_] => F[Unit] = botResponse => request(SendMessage(msg.source, botResponse.show)).void

    response(msg).flatMap(sendMessageF)
  }

  def notifier(sleeper: F[Unit]): F[Unit] =
    for {
      _ <- sleeper
      _ <- request(SendMessage(407956969, "hello"))
      _ <- notifier(sleeper)
    } yield ()
}

package bot

import bot.chatbased.{BotResponse, InMemoryUserChatPool, Panic, UserChatPool}
import bot.command.UserCommand
import cats.{Applicative, Monad}
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO}
import cats.syntax.functor._
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods._
import domain.{ChatId, UserId}
import storage.{CommandsAwareStorage, MeetingStorage}
import sttp.client3.SttpBackend
import cats.syntax.all._
import com.bot4s.telegram.models.Message
import error.AppError
import cats.implicits.toFunctorOps

case class MeetingReminderBot[F[_]: Monad : Async](
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

    val sendMessageF: BotResponse[_] => F[Unit] = botResponse =>
      request(SendMessage(msg.source, botResponse.show)).void

    response(msg).flatMap(sendMessageF)
  }

}

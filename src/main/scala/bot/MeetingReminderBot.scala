package bot

import bot.chatbased.{BotResponse, InMemoryUserChatPool, Panic}
import cats.Applicative
import cats.effect.Async
import cats.implicits.toFunctorOps
import cats.syntax.all._
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models.Message
import domain.{ChatId, MeetingId, Notification, User}
import error.{AppError, NoSuchUserFound}
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

  def notifier(sleeper: F[Unit]): F[Unit] = {
    def notificationsWithChatIds: F[Either[List[AppError], List[(ChatId, Notification)]]] =
      for {
        notificationsToSendEither <- commandsAwareStorage.notificationStorage.getNotificationsToSend
        notificationsToSendWithChatId <- notificationsToSendEither match {
          case Left(error) => Applicative[F].pure { List(Left(error)) }
          case Right(notificationsToSend) =>
            notificationsToSend.map { notification =>
              commandsAwareStorage.userStorage
                .getUserById(notification.userId)
                .map({
                  case Left(error)       => Left(error)
                  case Right(None)       => Left(NoSuchUserFound(notification.userId).asPersistenceError)
                  case Right(Some(user)) => Right((user.chatId, notification))
                })
            }.sequence
        }
      } yield notificationsToSendWithChatId.partitionMap(identity) match {
        case (Nil, rights) => Right(rights)
        case (lefts, _)    => Left(lefts)
      }
    for {
      _ <- sleeper
      _ <- Applicative[F].pure { println("problem") }
      delete <- notificationsWithChatIds.map {
        case Right(lst) => lst.map {
          case (chatId, notification) => request(SendMessage(chatId.value, "hello"))
            .flatMap {
              _ => commandsAwareStorage.notificationStorage.delete(notification.notificationId)
            }
        }.sequence
      }
      _ <- notifier(sleeper)
    } yield ()
  }
}

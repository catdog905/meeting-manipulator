package bot.command

import cats.{Monad, Show}
import domain.{MeetingId, UserId}
import cats.implicits.toFunctorOps
import error.AppError
import storage.CommandsAwareStorage
import tofu.logging.derivation.show.generate

final case class CancelMeetingCommand[F[_]: Monad](
  meetingId: MeetingId,
  initiator: UserId,
  implicit val commandsAwareStorage: CommandsAwareStorage[F]
) extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] =
    commandsAwareStorage.cancelMeeting(meetingId, initiator).map {
      case Left(error) => Left(error)
      case Right(_)    => Right(None)
    }

  override val showO: Show[Nothing] = _ => ""
}

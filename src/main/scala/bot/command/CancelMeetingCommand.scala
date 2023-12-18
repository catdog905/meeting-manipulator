package bot.command

import cats.Monad
import cats.implicits.toFunctorOps
import domain.{MeetingId, UserId}
import error.AppError
import storage.CommandsAwareStorage

abstract final case class CancelMeetingCommand[F[_]: Monad](
  meetingId: MeetingId,
  initiator: UserId,
  implicit val commandsAwareStorage: CommandsAwareStorage[F]
) extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] =
    commandsAwareStorage.cancelMeeting(meetingId, initiator).map {
      case Left(error) => Left(error)
      case Right(_)    => Right(None)
    }

}

package bot.command

import cats.implicits.toFunctorOps
import cats.{Applicative, Monad}
import domain.{CreateMeeting, CreateMeetingWithParticipants, MeetingId, UserId}
import error.AppError
import storage.CommandsAwareStorage

final case class ArrangeMeetingCommand[F[_]: Monad](
  createMeeting: CreateMeeting,
  participants: List[UserId],
  initiator: UserId
)(implicit val commandsAwareStorage: CommandsAwareStorage[F])
  extends UserCommand[F, MeetingId] {
  override def act: F[Either[AppError, Option[MeetingId]]] =
    commandsAwareStorage
      .arrangeMeeting(
        CreateMeetingWithParticipants(createMeeting, participants)
      )
      .map {
        case Left(error)      => Left(error)
        case Right(meetingId) => Right(Some(meetingId))
      }
}

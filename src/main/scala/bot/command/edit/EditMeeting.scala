package bot.command.edit

import bot.command.UserCommand
import domain.{MeetingId, UserId}
import error.AppError

final case class EditMeeting[F[_]](meetingId: MeetingId, editMeetingAction: EditMeetingAction, initiator: UserId)
  extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] = ???
}


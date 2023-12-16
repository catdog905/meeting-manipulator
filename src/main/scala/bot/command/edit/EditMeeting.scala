package bot.command.edit

import bot.command.UserCommand
import cats.Show
import domain.{MeetingId, UserId}
import error.AppError
import tofu.logging.derivation.show.generate

final case class EditMeeting[F[_]](meetingId: MeetingId, editMeetingAction: EditMeetingAction, initiator: UserId)
  extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] = ???

  override val showO: Show[Nothing] = _ => ""
}


package bot.command

import bot.command.edit.EditMeetingAction
import cats.{Monad, Show}
import domain.{CreateMeeting, MeetingId, MeetingLocation, User, UserId}
import error.AppError
import storage.CommandsAwareStorage

import scala.util.Try

trait UserCommand[+F[_], O] {
  val initiator: UserId
  val showO: Show[O]
  def act: F[Either[AppError, Option[O]]]
}
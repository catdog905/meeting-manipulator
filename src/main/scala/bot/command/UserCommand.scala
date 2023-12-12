package bot.command

import bot.command.edit.EditMeetingAction
import cats.Monad
import domain.{CreateMeeting, MeetingId, MeetingLocation, User, UserId}
import error.AppError
import storage.CommandsAwareStorage

import scala.util.Try

trait UserCommand[F[_], O] {
  val initiator: UserId
  def act: F[Either[AppError, Option[O]]]
}

object UserCommand {
  def apply[F[_]: Monad](strRepresentation: String, initiator: UserId)(implicit
    commandsAwareStorage: CommandsAwareStorage[F]
  ): Option[UserCommand[F, MeetingId]] = strRepresentation match {
    case s"/new_meeting $name" => Some(ArrangeMeetingCommand(CreateMeeting(name), Nil, initiator))
    case _ => None
  }
}

package error

import domain.{MeetingId, UserId}

import scala.language.implicitConversions

sealed trait StorageError {
  self =>

  val message: String
  val cause: Option[Throwable] = None

  val asPersistenceError: PersistenceError = PersistenceError(self)
}
final case class NoSuchMeetingFound(message: String) extends StorageError

object NoSuchMeetingFound {
  def apply(meetingId: MeetingId): NoSuchMeetingFound = NoSuchMeetingFound(s"No meeting with name $meetingId was found")
}

final case class UserAlreadyJoinedMeeting(message: String) extends StorageError

object UserAlreadyJoinedMeeting {
  def apply(userId: UserId, meetingId: MeetingId): UserAlreadyJoinedMeeting =
    UserAlreadyJoinedMeeting(s"User with id $userId are already joined meeting with id $meetingId")
}

final case class UsersAlreadyJoinedMeeting(message: String) extends StorageError

object UsersAlreadyJoinedMeeting {
  def apply(users: List[UserId], meetingId: MeetingId): UserAlreadyJoinedMeeting =
    UserAlreadyJoinedMeeting(s"Users with ids $users are already joined meeting with id $meetingId")
}


final case class UserAlreadyExists(message: String) extends StorageError

object UserAlreadyExists{
  def apply(userId: UserId): UserAlreadyExists = UserAlreadyExists(s"User with id $userId already exists")
}

final case class NoSuchUserFound(message: String) extends StorageError

object NoSuchUserFound{
  def apply(userId: UserId): NoSuchUserFound = NoSuchUserFound(s"No user with id $userId was found")
}

final case class InternalStorageError(message: String) extends StorageError

final case class NoPermissionToCancelMeeting(message: String) extends StorageError

object NoPermissionToCancelMeeting {
  def apply(userId: UserId, meetingId: MeetingId): NoPermissionToCancelMeeting =
    NoPermissionToCancelMeeting(s"User with id $userId can have no permissions ot cancel meeting with id $meetingId")
}
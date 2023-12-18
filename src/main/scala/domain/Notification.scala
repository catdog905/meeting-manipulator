package domain

import cats.Show
import doobie.{Meta, Read}
import error.ParsingError

import java.time.Duration
import scala.util.{Failure, Success, Try}

case class Notification(
  notificationId: NotificationId,
  meetingId: MeetingId,
  userId: UserId,
  timeToMeeting: TimeToMeeting
)

object Notification {
  implicit val show: Show[Notification] = Show.show(notification => s"Meeting ${notification.userId} starts soon")
}


case class CreateNotification(
  meetingId: MeetingId,
  userId: UserId,
  timeToMeeting: TimeToMeeting
)

case class NotificationId(id: Long)

object NotificationId {

  implicit val notificationIdMeta: Meta[NotificationId] = Meta[Long].timap(NotificationId(_))(_.id)
  implicit val read: Read[NotificationId] = Read[Long].map(NotificationId.apply)
}


case class TimeToMeeting(value: Duration)

object TimeToMeeting {

  implicit val timeToMeetingMeta: Meta[TimeToMeeting] =
    Meta[String].timap(raw => TimeToMeeting(Duration.parse(raw)))(timeToMeeting => timeToMeeting.value.toString)
  implicit val read: Read[TimeToMeeting] = Read[String].map(raw => TimeToMeeting(Duration.parse(raw)))
}
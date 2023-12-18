package dao

import alleycats.Extract.extractCoflatMapIsComonad
import domain.{CreateNotification, Notification, NotificationId}
import doobie._
import doobie.implicits._

trait NotificationSql {
  def create(createNotification: CreateNotification): ConnectionIO[NotificationId]
  def deleteNotification(notificationId: NotificationId): ConnectionIO[Unit]
  def getNotificationsToSend: ConnectionIO[List[Notification]]
}


object NotificationSql {
  object sqls {

    def insertNotification(createNotification: CreateNotification): Update0 =
      sql"""
           INSERT INTO meeting_reminder.notification (meeting_id, user_id, time_to_meeting)
           VALUES (${createNotification.meetingId}, ${createNotification.userId}, ${createNotification.timeToMeeting})
         """.update

    def deleteNotification(notificationId: NotificationId): Update0 =
      sql"""
           DELETE
           FROM meeting_reminder.notification
           WHERE notification_id = $notificationId
         """.update

    def selectNotificationsToSend: Query0[Notification] =
      sql"""
           SELECT notification_id, meeting_id, user_id, time_to_meeting
           FROM meeting_reminder.notification
           JOIN meeting_reminder.meeting on notification.meeting_id = meeting.id
           WHERE now() + time_to_meeting >= date_time
         """.query[Notification]
  }

  private final class Impl extends NotificationSql {
    import sqls._
    override def create(createNotification: CreateNotification): ConnectionIO[NotificationId] =
      insertNotification(createNotification)
        .withUniqueGeneratedKeys[NotificationId]("notification_id")

    override def deleteNotification(notificationId: NotificationId): ConnectionIO[Unit] =
      sqls.deleteNotification(notificationId).run.map {_ => ()}

    override def getNotificationsToSend: ConnectionIO[List[Notification]] =
      selectNotificationsToSend.to[List]
  }

  def make: NotificationSql = new Impl
}

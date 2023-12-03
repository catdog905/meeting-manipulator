package dao

import cats.syntax.applicative._
import domain._
import doobie._
import doobie.implicits._

trait MeetingParticipantSql {
  def addMeetingParticipants(meetingId: MeetingId, users: Seq[UserId]): ConnectionIO[Unit]

  def getMeetingParticipants(meetingId: MeetingId): ConnectionIO[List[User]]
}

object MeetingParticipantSql {
  object sqls {

    def insertMeetingParticipants(meetingId: MeetingId, users: Seq[UserId]): Update[(MeetingId, UserId)] =
      Update[(MeetingId, UserId)]("""
           INSERT INTO meeting_reminder.meeting_participant
           VALUES (meeting_id, user_id)
         """)

    def selectMeetingParticipants(meetingId: MeetingId): Query0[User] =
      sql"""
           SELECT u.id
           FROM meeting_reminder.meeting_participant
           LEFT JOIN meeting_reminder."user" u on u.id = meeting_participant.user_id
           WHERE meeting_id == $meetingId
         """.query[User]
  }

  private final class Impl extends MeetingParticipantSql {
    import sqls._

    override def addMeetingParticipants(meetingId: MeetingId, users: Seq[UserId]): ConnectionIO[Unit] =
      insertMeetingParticipants(meetingId, users)
        .updateMany(users.map((meetingId, _)))
        .flatMap(_ => ().pure[ConnectionIO])

    override def getMeetingParticipants(meetingId: MeetingId): ConnectionIO[List[User]] = {
      selectMeetingParticipants(meetingId).to[List]
    }
  }

  def make: MeetingParticipantSql = new Impl
}

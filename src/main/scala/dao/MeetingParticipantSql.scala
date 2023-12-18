package dao

import cats.syntax.applicative._
import domain._
import doobie._
import doobie.implicits._

trait MeetingParticipantSql {
  def addMeetingParticipants(meetingId: MeetingId, users: Seq[UserId]): ConnectionIO[Unit]

  def getMeetingParticipants(meetingId: MeetingId): ConnectionIO[List[UserId]]
}

object MeetingParticipantSql {
  object sqls {

    def insertMeetingParticipants(meetingId: MeetingId, users: Seq[UserId]): Update[(MeetingId, UserId)] =
      Update[(MeetingId, UserId)]("""
           INSERT INTO meeting_reminder.meeting_participant
           VALUES (?, ?)
         """)

    def selectMeetingParticipants(meetingId: MeetingId): Query0[UserId] =
      sql"""
           SELECT user_id
           FROM meeting_reminder.meeting_participant
           WHERE meeting_id = $meetingId
         """.query[UserId]
  }

  private final class Impl extends MeetingParticipantSql {
    import sqls._

    override def addMeetingParticipants(meetingId: MeetingId, users: Seq[UserId]): ConnectionIO[Unit] =
      insertMeetingParticipants(meetingId, users)
        .updateMany(users.map((meetingId, _)))
        .flatMap(_ => ().pure[ConnectionIO])

    override def getMeetingParticipants(meetingId: MeetingId): ConnectionIO[List[UserId]] = {
      selectMeetingParticipants(meetingId).to[List]
    }
  }

  def make: MeetingParticipantSql = new Impl
}

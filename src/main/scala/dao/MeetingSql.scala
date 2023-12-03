package dao

import domain._
import doobie._
import doobie.implicits._

trait MeetingSql {
  def create(meeting: CreateMeeting): ConnectionIO[MeetingId]

  def getMeetingsHostedBy(userId: UserId): ConnectionIO[List[MeetingWithoutParticipants]]
}

object MeetingSql {
  object sqls {
    def insertMeetingSql(meeting: CreateMeeting): Update0 =
      sql"""
           INSERT INTO meeting_reminder.meeting
           VALUES (date_time, title, location_id, host)
           (${meeting.dateTime}, ${meeting.title}, ${meeting.locationId}, ${meeting.host})
           """.update

    def selectMeetingsHostedBy(userId: UserId): Query0[MeetingWithoutParticipants] =
      sql"""
           SELECT m.id, date_time, title, location_id, host
           FROM meeting_reminder.meeting_participant
           LEFT JOIN meeting_reminder.meeting m on meeting_participant.meeting_id = m.id
           WHERE meeting_participant.user_id == $userId
         """.query[MeetingWithoutParticipants]
  }

  private final class Impl extends MeetingSql {
    import sqls._

    override def create(createMeeting: CreateMeeting): ConnectionIO[MeetingId] =
      insertMeetingSql(createMeeting)
        .withUniqueGeneratedKeys[MeetingId]("id")

    override def getMeetingsHostedBy(userId: UserId): ConnectionIO[List[MeetingWithoutParticipants]] =
      selectMeetingsHostedBy(userId).to[List]
  }

  def make: MeetingSql = new Impl
}

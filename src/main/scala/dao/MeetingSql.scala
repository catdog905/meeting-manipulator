package dao

import domain._
import doobie._
import doobie.implicits._

trait MeetingSql {
  def create(meeting: CreateMeeting): ConnectionIO[MeetingId]

  def getMeetingsHostedBy(meetingHost: MeetingHost): ConnectionIO[List[Meeting]]
  def cancelMeeting(meetingId: MeetingId): ConnectionIO[Unit]
  def getMeetingById(meetingId: MeetingId): ConnectionIO[Meeting]
}

object MeetingSql {
  object sqls {
    def insertMeetingSql(meeting: CreateMeeting): Update0 =
      sql"""
           INSERT INTO meeting_reminder.meeting (date_time, duration, title, location_id, host)
           VALUES (${meeting.dateTime}, ${meeting.duration}::interval, ${meeting.title}, ${meeting.locationId}, ${meeting.host})
           """.update

    def selectMeetingsHostedBy(meetingHost: MeetingHost): Query0[Meeting] =
      sql"""
           SELECT m.id, date_time, title, location_id, host
           FROM meeting_reminder.meeting_participant
           LEFT JOIN meeting_reminder.meeting m on meeting_participant.meeting_id = m.id
           WHERE meeting_participant.user_id == $meetingHost
         """.query[Meeting]

    def deleteMeetingById(meetingId: MeetingId): Update0 =
      sql"""
           DELETE FROM meeting_reminder.meeting
           WHERE id = $meetingId
         """.update

    def selectMeetingById(meetingId: MeetingId): Query0[Meeting] =
      sql"""
           SELECT id, date_time, iso_8601_format(duration), title, location_id, host
           FROM meeting_reminder.meeting
           WHERE id = $meetingId
         """.query[Meeting]
  }

  private final class Impl extends MeetingSql {
    import sqls._

    override def create(createMeeting: CreateMeeting): ConnectionIO[MeetingId] =
      insertMeetingSql(createMeeting)
        .withUniqueGeneratedKeys[MeetingId]("id")

    override def getMeetingsHostedBy(meetingHost: MeetingHost): ConnectionIO[List[Meeting]] =
      selectMeetingsHostedBy(meetingHost).to[List]

    override def cancelMeeting(meetingId: MeetingId): doobie.ConnectionIO[Unit] =
      deleteMeetingById(meetingId).run.map {_ => ()}

    override def getMeetingById(meetingId: MeetingId): doobie.ConnectionIO[Meeting] =
      selectMeetingById(meetingId).unique
  }

  def make: MeetingSql = new Impl
}

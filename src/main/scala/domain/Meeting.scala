package domain

import java.time.{Period, ZonedDateTime}

final case class Meeting(
  id: MeetingId,
  dateTime: MeetingDateTime,
  duration: MeetingDuration,
  title: MeetingTitle,
  location: MeetingLocation,
  host: MeetingHost
)

object Meeting {
  def apply(id: MeetingId, createMeeting: CreateMeeting): Meeting = new Meeting(
    id,
    createMeeting.dateTime,
    createMeeting.duration,
    createMeeting.title,
    createMeeting.location,
    createMeeting.host
  )
}

final case class CreateMeeting(
  dateTime: MeetingDateTime,
  duration: MeetingDuration,
  title: MeetingTitle,
  location: MeetingLocation,
  host: MeetingHost
)

object CreateMeeting {
  def apply(name: String): CreateMeeting =
    CreateMeeting(
      MeetingDateTime(ZonedDateTime.now().plus(Period.ofDays(3))),
      MeetingDuration(Period.ofMonths(30)),
      MeetingTitle(name),
      MeetingLocation(OnlineMeetingLocation(URL("fake-location"), 0)),
      MeetingHost(UserId(0))
    )
}

final case class CreateMeetingWithParticipants(createMeeting: CreateMeeting, participants: List[UserId])

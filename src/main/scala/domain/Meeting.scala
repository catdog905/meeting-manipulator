package domain

final case class Meeting(
  id: MeetingId,
  dateTime: MeetingDateTime,
  duration: MeetingDuration,
  title: MeetingTitle,
  locationId: LocationId,
  host: MeetingHost
)

object Meeting {
  def apply(id: MeetingId, createMeeting: CreateMeeting): Meeting = new Meeting(
    id,
    createMeeting.dateTime,
    createMeeting.duration,
    createMeeting.title,
    createMeeting.locationId,
    createMeeting.host
  )
}

final case class CreateMeeting(
  dateTime: MeetingDateTime,
  duration: MeetingDuration,
  title: MeetingTitle,
  locationId: LocationId,
  host: MeetingHost
)

final case class CreateMeetingWithParticipants(createMeeting: CreateMeeting, participants: List[UserId])

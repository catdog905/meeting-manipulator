package domain

import doobie.Read

final case class Meeting(
  id: MeetingId,
  dateTime: MeetingDateTime,
  title: MeetingTitle,
  location: Location,
  host: MeetingHost,
  participants: Seq[User]
)

final case class MeetingWithoutParticipants(
  id: MeetingId,
  dateTime: MeetingDateTime,
  title: MeetingTitle,
  location: Location,
  host: MeetingHost
)

object MeetingWithoutParticipants {
  implicit val meetingWithoutParticipantsMeta: Read[MeetingWithoutParticipants] =
    Read[(MeetingId, MeetingDateTime, MeetingTitle, Location, MeetingHost)]
      .map {
        case (id: MeetingId, dateTime: MeetingDateTime, title: MeetingTitle, location: Location, host: MeetingHost) =>
          MeetingWithoutParticipants(
            id,
            dateTime,
            title,
            location,
            host
          )
      }
}

final case class CreateMeeting(
  dateTime: MeetingDateTime,
  title: MeetingTitle,
  locationId: LocationId,
  host: MeetingHost,
  participants: Seq[User]
)

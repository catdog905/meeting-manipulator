package bot.command.edit

import domain._

sealed trait EditMeetingAction

final case class RenameMeeting(newTitle: MeetingTitle) extends EditMeetingAction

final case class RescheduleMeeting(newMeetingDateTime: MeetingDateTime, newMeetingDuration: MeetingDuration)
  extends EditMeetingAction

final case class RelocateMeeting(meetingId: MeetingId, newLocation: MeetingLocation) extends EditMeetingAction

final case class AddMeetingParticipant(meetingId: MeetingId, newParticipant: UserId) extends EditMeetingAction

final case class RemoveMeetingParticipant(meetingId: MeetingId, removeParticipant: UserId) extends EditMeetingAction
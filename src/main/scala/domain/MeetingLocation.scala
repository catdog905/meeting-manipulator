package domain

import doobie.Meta

sealed trait MeetingLocation {
  val id: LocationId
}

object MeetingLocation {
  def apply(location: OnlineMeetingLocation): MeetingLocation = location

  // This implicit Get instance will handle the conversion from the database representation to Location
  implicit val metaLocation: Meta[MeetingLocation] = Meta[String]
    .timap(a => MeetingLocation(OnlineMeetingLocation(URL(a), 0)))(_.toString)
}

case class OnlineMeetingLocation(url: URL, id: LocationId) extends MeetingLocation

case class OfflineMeetingLocation(address: Address, id: LocationId) extends MeetingLocation

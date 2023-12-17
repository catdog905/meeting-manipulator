package domain

import doobie.Meta

sealed trait MeetingLocation {
  val id: LocationId
}

object MeetingLocation {
  def apply(location: OnlineMeetingLocation): MeetingLocation = location
  def apply(stringRepresentation: String): MeetingLocation =
    OfflineMeetingLocation(Address(stringRepresentation), -1)

  // This implicit Get instance will handle the conversion from the database representation to Location
  implicit val metaLocation: Meta[MeetingLocation] = Meta[String]
    .timap(a => MeetingLocation(OnlineMeetingLocation(URL(a), 0)))(_.toString)
  val format: Format = Format("String")
}

case class OnlineMeetingLocation(url: URL, id: LocationId) extends MeetingLocation

case class OfflineMeetingLocation(address: Address, id: LocationId) extends MeetingLocation

package domain

import doobie.Meta

sealed trait Location {
  val id: LocationId
}

object Location {
  def apply(location: OnlineLocation): Location = location

  // This implicit Get instance will handle the conversion from the database representation to Location
  implicit val metaLocation: Meta[Location] = Meta[String]
    .timap(a => Location(OnlineLocation(URL(a), 0)))(_.toString)
}

case class OnlineLocation(url: URL, id: LocationId) extends Location

case class OfflineLocation(address: Address, id: LocationId) extends Location

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.postgres.implicits._
import doobie.{Meta, Read}
import io.estatico.newtype.macros.newtype

import java.time.ZonedDateTime
import scala.language.implicitConversions

package object domain {
  @derive(encoder, decoder)
  @newtype
  case class MeetingId(value: Long)

  object MeetingId {
    implicit val meetingIdMeta: Meta[MeetingId] = Meta[Long].timap(MeetingId(_))(_.value)
    implicit val read: Read[MeetingId] = Read[Long].map(MeetingId.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class LocationId(value: Long)

  object LocationId {
    implicit val locationIdMeta: Meta[LocationId] = Meta[Long].timap(LocationId(_))(_.value)
    implicit val read: Read[LocationId] = Read[Long].map(LocationId.apply)

    implicit def fromInt(id: Int): LocationId = LocationId(id)
  }

  @derive(encoder, decoder)
  @newtype
  case class MeetingDateTime(dateTime: ZonedDateTime)

  object MeetingDateTime {
    implicit val meetingDateTimeMeta: Meta[MeetingDateTime] = Meta[ZonedDateTime].timap(MeetingDateTime(_))(_.dateTime)
    implicit val read: Read[MeetingDateTime] = Read[ZonedDateTime].map(MeetingDateTime.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class MeetingTitle(title: String)

  object MeetingTitle {
    implicit val meetingTitleMeta: Meta[MeetingTitle] = Meta[String].timap(MeetingTitle(_))(_.title)
    implicit val read: Read[MeetingTitle] = Read[String].map(MeetingTitle.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class UserId(id: Int)

  object UserId {
    implicit val userIdMeta: Meta[UserId] = Meta[Int].timap(UserId(_))(_.id)
    implicit val read: Read[UserId] = Read[Int].map(UserId.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class URL(value: String)

  object URL {
    implicit val urlMeta: Meta[URL] = Meta[String].timap(URL(_))(_.value)
    implicit val read: Read[URL] = Read[String].map(URL.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class Address(value: String)

  object Address {
    implicit val addressMeta: Meta[Address] = Meta[String].timap(Address(_))(_.value)
    implicit val read: Read[Address] = Read[String].map(Address.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class MeetingHost(value: String)

  object MeetingHost {
    implicit val meetingHostMeta: Meta[MeetingHost] = Meta[String].timap(MeetingHost(_))(_.value)
    implicit val read: Read[MeetingHost] = Read[String].map(MeetingHost.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class ChatId(value: Int)
}

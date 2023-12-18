package domain

import cats.Show
import cats.syntax.all._
import doobie.postgres.implicits._
import doobie.{Meta, Read}
import error.ParsingError

import java.time.{Duration, ZonedDateTime}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

case class Format(str: String) {
  def show: String = str
}

case class MeetingId(value: Long)

object MeetingId {
  implicit val meetingIdMeta: Meta[MeetingId] = Meta[Long].timap(MeetingId(_))(_.value)
  implicit val read: Read[MeetingId] = Read[Long].map(MeetingId.apply)

  implicit val show: Show[MeetingId] = Show.show(meetingId => Show[Long].show(meetingId.value))
}

case class LocationId(value: Long)

object LocationId {
  implicit val locationIdMeta: Meta[LocationId] = Meta[Long].timap(LocationId(_))(_.value)
  implicit val read: Read[LocationId] = Read[Long].map(LocationId.apply)

  implicit def fromInt(id: Int): LocationId = LocationId(id.toLong)
  def apply(stringRepresentation: String): Either[ParsingError, LocationId] =
    Try(stringRepresentation.toLong) match {
      case Failure(exception) => ParsingError("LocationId", exception).asLeft
      case Success(value)     => LocationId(value).asRight
    }
}

final case class MeetingDateTime(dateTime: ZonedDateTime)

object MeetingDateTime {
  def apply(stringRepresentation: String): Either[ParsingError, MeetingDateTime] =
    Try(ZonedDateTime.parse(stringRepresentation)) match {
      case Success(zonedDateTime) => MeetingDateTime(zonedDateTime).asRight
      case Failure(exception)     => ParsingError("MeetingDateTime", exception).asLeft
    }

  implicit val meetingDateTimeMeta: Meta[MeetingDateTime] = Meta[ZonedDateTime].timap(MeetingDateTime(_))(_.dateTime)
  implicit val read: Read[MeetingDateTime] = Read[ZonedDateTime].map(MeetingDateTime.apply)
  implicit val format: Format =
    Format("""ZonedDateTime.java format[example = "2007-12-03T10:15:30+01:00[Europe/Paris]"]""")
}

case class MeetingDuration(value: Duration)

object MeetingDuration {
  def apply(stringRepresentation: String): Either[ParsingError, MeetingDuration] =
    Try(Duration.parse(stringRepresentation)) match {
      case Success(value)     => MeetingDuration(value).asRight
      case Failure(exception) => ParsingError("MeetingDuration", exception).asLeft
    }

  implicit val meetingHostMeta: Meta[MeetingDuration] =
    Meta[String].timap(raw => MeetingDuration(Duration.parse(raw)))(meetingDuration => meetingDuration.value.toString)
  implicit val read: Read[MeetingDuration] = Read[String].map(raw => MeetingDuration(Duration.parse(raw)))
  val format: Format = Format("""ISO-8601""")
}

case class MeetingTitle(title: String)

object MeetingTitle {
  implicit val meetingTitleMeta: Meta[MeetingTitle] = Meta[String].timap(MeetingTitle(_))(_.title)
  implicit val read: Read[MeetingTitle] = Read[String].map(MeetingTitle.apply)
  val format: Format = Format("String")
}

case class UserId(id: Long)

object UserId {
  def apply(stringRepresentation: String): Either[ParsingError, UserId] =
    Try(stringRepresentation.toLong) match {
      case Failure(exception) => ParsingError("MeetingHost", exception).asLeft
      case Success(value)     => UserId(value).asRight
    }

  implicit val userIdMeta: Meta[UserId] = Meta[Long].timap(UserId(_))(_.id)
  implicit val read: Read[UserId] = Read[Long].map(UserId.apply)

  val format: Format = Format("Long")
}

case class URL(value: String)

object URL {
  implicit val urlMeta: Meta[URL] = Meta[String].timap(URL(_))(_.value)
  implicit val read: Read[URL] = Read[String].map(URL.apply)
}

case class Address(value: String)

object Address {
  implicit val addressMeta: Meta[Address] = Meta[String].timap(Address(_))(_.value)
  implicit val read: Read[Address] = Read[String].map(Address.apply)
}

case class MeetingHost(value: UserId)

object MeetingHost {
  def apply(stringRepresentation: String): Either[ParsingError, MeetingHost] =
    UserId(stringRepresentation).map(MeetingHost(_))

  implicit val meetingHostMeta: Meta[MeetingHost] = Meta[UserId].timap(MeetingHost(_))(_.value)
  implicit val read: Read[MeetingHost] = Read[UserId].map(MeetingHost.apply)

}

case class ChatId(value: Long)

object ChatId {
  implicit val meetingHostMeta: Meta[ChatId] = Meta[Long].timap(ChatId(_))(_.value)
  implicit val read: Read[ChatId] = Read[Long].map(ChatId.apply)

  def apply(stringRepresentation: String): Either[ParsingError, ChatId] =
    Try(stringRepresentation.toLong) match {
      case Failure(exception) => Left(ParsingError("ChatId", exception))
      case Success(value)     => Right(ChatId(value))
    }
}

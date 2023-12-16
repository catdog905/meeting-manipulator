import cats.Show
import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.postgres.implicits._
import doobie.{Meta, Read}
import error.{AppError, ParsingError}
import io.estatico.newtype.macros.newtype

import java.time.{Duration, Period, ZonedDateTime}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}
import cats.syntax.all._
import com.bot4s.telegram.models.ChatId

package object domain {
  @derive(encoder, decoder)
  @newtype
  case class MeetingId(value: Long)

  object MeetingId {
    implicit val meetingIdMeta: Meta[MeetingId] = Meta[Long].timap(MeetingId(_))(_.value)
    implicit val read: Read[MeetingId] = Read[Long].map(MeetingId.apply)

    implicit val show: Show[MeetingId] = Show.show(meetingId => Show[Long].show(meetingId.value))
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
    def apply(stringRepresentation: String): Either[ParsingError, MeetingDateTime] =
      Try(ZonedDateTime.parse(stringRepresentation)) match {
        case Success(zonedDateTime) => MeetingDateTime(zonedDateTime).asRight
        case Failure(exception) => ParsingError("MeetingDateTime", exception).asLeft
      }

    implicit val meetingDateTimeMeta: Meta[MeetingDateTime] = Meta[ZonedDateTime].timap(MeetingDateTime(_))(_.dateTime)
    implicit val read: Read[MeetingDateTime] = Read[ZonedDateTime].map(MeetingDateTime.apply)
  }

  @derive(encoder, decoder)
  @newtype
  case class MeetingDuration(value: Period)

  object MeetingDuration {
    def apply(stringRepresentation: String): Either[ParsingError, MeetingDuration] =
      Try(Period.parse(stringRepresentation)) match {
        case Success(value) => MeetingDuration(value).asRight
        case Failure(exception) => ParsingError("MeetingDuration", exception).asLeft
      }

    implicit val meetingHostMeta: Meta[MeetingDuration] = Meta[Int].timap(_ => MeetingDuration(Period.ofDays(1)))(_ => 1) //TODO: fix
    implicit val read: Read[MeetingDuration] = Read[Int].map(_ => MeetingDuration(Period.ofDays(1))) //TODO: fix
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
  case class UserId(id: Long)

  object UserId {
    def apply(stringRepresentation: String): Either[ParsingError, UserId] =
      Try(stringRepresentation.toLong) match {
        case Failure(exception) => ParsingError("MeetingHost", exception).asLeft
        case Success(value) => UserId(value).asRight
      }


    implicit val userIdMeta: Meta[UserId] = Meta[Long].timap(UserId(_))(_.id)
    implicit val read: Read[UserId] = Read[Long].map(UserId.apply)
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
  case class MeetingHost(value: UserId)

  object MeetingHost {
    def apply(stringRepresentation: String): Either[ParsingError, MeetingHost] =
      UserId(stringRepresentation).map(MeetingHost(_))

    implicit val meetingHostMeta: Meta[MeetingHost] = Meta[UserId].timap(MeetingHost(_))(_.value)
    implicit val read: Read[MeetingHost] = Read[UserId].map(MeetingHost.apply)

  }

  @derive(encoder, decoder)
  @newtype
  case class ChatId(value: Long)

  object ChatId {
    implicit val meetingHostMeta: Meta[ChatId] = Meta[Long].timap(ChatId(_))(_.value)
    implicit val read: Read[ChatId] = Read[Long].map(ChatId.apply)
  }
}

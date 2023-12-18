package storage

import cats.effect.IO
import cats.effect.std.Random
import cats.syntax.either._
import dao.MeetingSql
import domain._
import doobie._
import doobie.implicits._
import error.{AppError, InternalError, NoSuchMeetingFound}

import scala.collection.mutable

trait MeetingStorage[F[_]] {
  def create(createMeeting: CreateMeeting): F[Either[AppError, MeetingId]]
  def cancel(meetingId: MeetingId): F[Either[AppError, Unit]]
  def getMeetingsHostedBy(meetingHost: MeetingHost): F[Either[AppError, List[Meeting]]]
  def getById(meetingId: MeetingId): F[Either[AppError, Meeting]]
}

object MeetingStorage {

  def make(
    sql: MeetingSql,
    transactor: Transactor[IO]
  ): MeetingStorage[IO] = {
    PostgresMeetingStorage(sql, transactor)
  }
}

final case class PostgresMeetingStorage(meetingSql: MeetingSql, transactor: Transactor[IO]) extends MeetingStorage[IO] {
  override def create(
    createMeeting: CreateMeeting
  ): IO[Either[AppError, MeetingId]] =
    meetingSql.create(createMeeting).transact(transactor).attempt.map {
      case Left(th)         => InternalError(th).asLeft
      case Right(meetingId) => meetingId.asRight
    }

  override def getMeetingsHostedBy(meetingHost: MeetingHost): IO[Either[AppError, List[Meeting]]] =
    meetingSql.getMeetingsHostedBy(meetingHost).transact(transactor).attempt.map {
      case Left(th)        => InternalError(th).asLeft
      case Right(meetings) => meetings.asRight
    }

  override def cancel(meetingId: MeetingId): IO[Either[AppError, Unit]] =
    meetingSql.cancelMeeting(meetingId).transact(transactor).attempt.map {
      case Left(th)        => InternalError(th).asLeft
      case Right(meetings) => meetings.asRight
    }

  override def getById(meetingId: MeetingId): IO[Either[AppError, Meeting]] =
    meetingSql.getMeetingById(meetingId).transact(transactor).attempt.map {
      case Left(th)       => InternalError(th).asLeft
      case Right(meeting) => meeting.asRight
    }
}

final case class InMemoryMeetingStorage(storage: mutable.Map[MeetingId, Meeting], random: Random[IO])
  extends MeetingStorage[IO] {

  override def create(createMeeting: CreateMeeting): IO[Either[AppError, MeetingId]] =
    for {
      randomId <- random.nextLong
      meetingId = MeetingId(randomId)
      meeting = Meeting(meetingId, createMeeting)
      _ <- IO { storage.put(meetingId, meeting) }
    } yield Right(meetingId)

  override def getMeetingsHostedBy(meetingHost: MeetingHost): IO[Either[AppError, List[Meeting]]] =
    IO { storage.filter { case (_, meeting) => meeting.host == meetingHost }.values.toList.asRight }

  override def cancel(meetingId: MeetingId): IO[Either[AppError, Unit]] =
    IO {
      storage.find { case (meetingIdInStorage, _) => meetingIdInStorage == meetingId } match {
        case None => NoSuchMeetingFound(meetingId).asPersistenceError.asLeft
        case Some((meetingId, _)) => {
          storage -= meetingId
          ().asRight
        }
      }
    }

  override def getById(meetingId: MeetingId): IO[Either[AppError, Meeting]] =
    IO {
      storage.get(meetingId) match {
        case Some(meeting) => meeting.asRight
        case None          => NoSuchMeetingFound(meetingId).asPersistenceError.asLeft
      }
    }
}

object InMemoryMeetingStorage {
  def apply(random: Random[IO]): InMemoryMeetingStorage = InMemoryMeetingStorage(mutable.Map(), random)
}

package storage

import cats.effect.IO
import cats.effect.std.Random
import domain._
import error.{AppError, AppPersistenceError, InternalError, InternalStorageError, NoSuchMeetingFound, UserAlreadyJoinedMeeting, UsersAlreadyJoinedMeeting}
import cats.syntax.either._
import dao.MeetingParticipantSql
import doobie.Transactor

import doobie.implicits._

import collection.mutable.Map
import scala.collection.mutable

trait MeetingParticipantStorage[F[_]] {
  def getMeetingParticipants(meetingId: MeetingId): F[Either[AppError, List[UserId]]]
  def addMeetingParticipant(meetingId: MeetingId, userId: UserId): F[Either[AppError, Unit]]
  def addMeetingParticipants(meetingId: MeetingId, participants: List[UserId]): F[Either[AppError, Unit]]
  def getMeetingsWithParticipant(userId: UserId): F[Either[AppError, List[MeetingId]]]
  def removeMeetingParticipant(userId: UserId, meetingId: MeetingId): F[Either[AppError, Unit]]
  def removeMeetingParticipants(userIds: List[UserId], meetingId: MeetingId): F[Either[AppError, Unit]]
  def removeMeetingParticipants(meetingId: MeetingId): F[Either[AppError, Unit]]
}

final case class PostgresMeetingParticipantStorage(
  meetingParticipantSql: MeetingParticipantSql,
  transactor: Transactor[IO]
) extends MeetingParticipantStorage[IO] {

  override def getMeetingParticipants(meetingId: MeetingId): IO[Either[AppError, List[UserId]]] =
    meetingParticipantSql.getMeetingParticipants(meetingId).transact(transactor).attempt.map {
      case Left(th)                   => InternalError(th).asLeft
      case Right(meetingParticipants) => meetingParticipants.asRight
    }

  override def addMeetingParticipant(meetingId: MeetingId, userId: UserId): IO[Either[AppError, Unit]] = ???

  override def addMeetingParticipants(meetingId: MeetingId, participants: List[UserId]): IO[Either[AppError, Unit]] =
    meetingParticipantSql.addMeetingParticipants(meetingId, participants).transact(transactor).attempt.map {
      case Left(th) => InternalError(th).asLeft
      case Right(()) => ().asRight
    }

  override def getMeetingsWithParticipant(userId: UserId): IO[Either[AppError, List[MeetingId]]] = ???

  override def removeMeetingParticipant(userId: UserId, meetingId: MeetingId): IO[Either[AppError, Unit]] = ???

  override def removeMeetingParticipants(userIds: List[UserId], meetingId: MeetingId): IO[Either[AppError, Unit]] = ???

  override def removeMeetingParticipants(meetingId: MeetingId): IO[Either[AppError, Unit]] = ???
}

final case class InMemoryMeetingParticipantStorage(storage: mutable.Map[MeetingId, List[UserId]])
  extends MeetingParticipantStorage[IO] {

  override def getMeetingParticipants(meetingId: MeetingId): IO[Either[AppError, List[UserId]]] =
    IO {
      storage.get(meetingId) match {
        case None               => Left(NoSuchMeetingFound(meetingId).asPersistenceError)
        case Some(participants) => participants.asRight
      }
    }

  override def addMeetingParticipant(meetingId: MeetingId, userId: UserId): IO[Either[AppError, Unit]] =
    addMeetingParticipants(meetingId, userId :: Nil)

  override def getMeetingsWithParticipant(userId: UserId): IO[Either[AppError, List[MeetingId]]] =
    IO { storage.filter { case (_, users) => users.contains(userId) }.keys.toList.asRight }

  override def removeMeetingParticipant(userId: UserId, meetingId: MeetingId): IO[Either[AppError, Unit]] =
    removeMeetingParticipants(userId :: Nil, meetingId)

  override def addMeetingParticipants(meetingId: MeetingId, participants: List[UserId]): IO[Either[AppError, Unit]] =
    for {
      result <- IO {
        storage.get(meetingId) match {
          case Some(meetingParticipants) =>
            meetingParticipants.filter(participants.contains(_)) match {
              case List() => storage.update(meetingId, participants ::: meetingParticipants).asRight
              case joinedParticipants: List[UserId] =>
                UsersAlreadyJoinedMeeting(joinedParticipants, meetingId).asPersistenceError.asLeft
            }
          case None => {
            storage.put(meetingId, participants)
            ().asRight
          }
        }
      }
    } yield result

  override def removeMeetingParticipants(userIds: List[UserId], meetingId: MeetingId): IO[Either[AppError, Unit]] = IO {
    storage.find { case (meetingIdInMap, _) =>
      meetingId == meetingIdInMap
    } match {
      case None => NoSuchMeetingFound(meetingId).asPersistenceError.asLeft
      case Some((meetingId, participants)) =>
        storage.update(meetingId, participants.filterNot(userIds.contains(_))).asRight
    }
  }

  override def removeMeetingParticipants(meetingId: MeetingId): IO[Either[AppError, Unit]] = IO {
    storage.find { case (meetingIdInMap, _) =>
      meetingId == meetingIdInMap
    } match {
      case None => NoSuchMeetingFound(meetingId).asPersistenceError.asLeft
      case Some((meetingId, _)) =>
        storage.update(meetingId, Nil).asRight
    }
  }
}

object InMemoryMeetingParticipantStorage {
  def apply(): InMemoryMeetingParticipantStorage =
    InMemoryMeetingParticipantStorage(mutable.Map())
}

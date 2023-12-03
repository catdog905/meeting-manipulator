package service

import cats.Id
import cats.effect.IO
import cats.syntax.either._
import dao.MeetingSql
import domain._
import domain.errors._
import doobie._
import doobie.implicits._
import tofu.logging.Logging
import tofu.logging.Logging.Make

trait MeetingStorage[F[_]] {
  def create(createMeeting: CreateMeeting): F[Either[AppError, MeetingId]]
  def getMeetingsHostedBy(userId: UserId): F[Either[AppError, List[MeetingWithoutParticipants]]]
}

object MeetingStorage {
  private final class Impl(meetingSql: MeetingSql, transactor: Transactor[IO]) extends MeetingStorage[IO] {

    override def create(
      createMeeting: CreateMeeting
    ): IO[Either[AppError, MeetingId]] =
      meetingSql.create(createMeeting).transact(transactor).attempt.map {
        case Left(th)         => InternalError(th).asLeft
        case Right(meetingId) => meetingId.asRight
      }

    override def getMeetingsHostedBy(userId: UserId): IO[Either[AppError, List[MeetingWithoutParticipants]]] =
      meetingSql.getMeetingsHostedBy(userId).transact(transactor).attempt.map {
        case Left(th)        => InternalError(th).asLeft
        case Right(meetings) => meetings.asRight
      }
  }

  def make(
    sql: MeetingSql,
    transactor: Transactor[IO]
  ): MeetingStorage[IO] = {
    val logs: Make[IO] = Logging.Make.plain[IO]
    implicit val logging: Id[Logging[IO]] = logs.forService[MeetingStorage[IO]]
    new Impl(sql, transactor)
  }

  doobie.free.connection.WeakAsyncConnectionIO
}

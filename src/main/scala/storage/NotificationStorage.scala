package storage

import cats.effect.IO
import cats.effect.std.Random
import dao.{MeetingSql, NotificationSql}
import domain.{CreateMeeting, CreateNotification, Meeting, MeetingHost, MeetingId, Notification, NotificationId}
import doobie.Transactor
import doobie.implicits._
import cats.syntax.either._
import error.{AppError, InternalError, NoSuchMeetingFound}

import scala.collection.mutable

trait NotificationStorage[F[_]] {
  def create(createNotification: CreateNotification): F[Either[AppError, NotificationId]]
  def delete(notificationId: NotificationId): F[Either[AppError, Unit]]
  def getNotificationsToSend: F[Either[AppError, List[Notification]]]
}

object NotificationStorage {

  def make(
            sql: MeetingSql,
            transactor: Transactor[IO]
          ): MeetingStorage[IO] = {
    PostgresMeetingStorage(sql, transactor)
  }
}

final case class PostgresNotificationStorage(notificationSql: NotificationSql, transactor: Transactor[IO]) extends NotificationStorage[IO] {
  override def create(createNotification: CreateNotification): IO[Either[AppError, NotificationId]] =
    notificationSql.create(createNotification).transact(transactor).attempt.map {
      case Left(th) => InternalError(th).asLeft
      case Right(notificationId) => notificationId.asRight
    }


  override def delete(notificationId: NotificationId): IO[Either[AppError, Unit]] =
    notificationSql.deleteNotification(notificationId).transact(transactor).attempt.map {
      case Left(th) => InternalError(th).asLeft
      case Right(()) => ().asRight
    }

  override def getNotificationsToSend: IO[Either[AppError, List[Notification]]] =
    notificationSql.getNotificationsToSend.transact(transactor).attempt.map {
      case Left(th) => InternalError(th).asLeft
      case Right(notifications) => notifications.asRight
    }
}
package storage

import cats.syntax.all._
import cats.{Applicative, Monad}
import domain._
import error.{AppError, NoPermissionToCancelMeeting}

// Every method should be covered by a transaction

case class CommandsAwareStorage[F[_]: Monad](
  meetingStorage: MeetingStorage[F],
  userStorage: UserStorage[F],
  meetingParticipantStorage: MeetingParticipantStorage[F]
) {
  def userId(chatId: ChatId): F[Either[AppError, UserId]] = userStorage.getUserIdByChatId(chatId).flatMap {
    case Some(userId) => Applicative[F].pure(userId.asRight)
    case None         => userStorage.addUser(CreateUser(chatId))
  }

  // TODO: Check whether all participants exist in UserStorage
  def arrangeMeeting(createMeetingWithParticipants: CreateMeetingWithParticipants): F[Either[AppError, MeetingId]] =
    for {
      meetingIdEither <- meetingStorage.create(createMeetingWithParticipants.createMeeting)
      meetingWithParticipants <- meetingIdEither match {
        case Left(error: AppError) => Applicative[F].pure(error.asLeft)
        case Right(meetingId) =>
          meetingParticipantStorage
            .addMeetingParticipants(meetingId, createMeetingWithParticipants.participants)
            .map({
              case Left(error) => error.asLeft
              case Right(_)    => meetingId.asRight
            })
      }
    } yield meetingWithParticipants

  def cancelMeeting(meetingId: MeetingId, initiator: UserId): F[Either[AppError, Unit]] = {
    meetingStorage.getById(meetingId) flatMap {
      case Left(error) => Applicative[F].pure(error.asLeft)
      case Right(meeting) =>
        if (meeting.host.value == initiator)
          for {
            participantsRemoveEither <- meetingParticipantStorage.removeMeetingParticipants(meetingId)
            meetingRemoveEither <- participantsRemoveEither match {
              case Left(error) => Applicative[F].pure(error.asLeft)
              case Right(_)    => meetingStorage.cancel(meetingId)
            }
          } yield meetingRemoveEither
        else
          Applicative[F].pure(NoPermissionToCancelMeeting(initiator, meetingId).asPersistenceError.asLeft)
    }
  }
}

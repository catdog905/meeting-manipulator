package storage

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import dao.{MeetingParticipantSql, MeetingSql, UserSql}
import domain._
import doobie.implicits._
import error.AppError
import io.github.liquibase4s.cats.CatsMigrationHandler.liquibaseHandlerForCats
import org.scalatest.BeforeAndAfter
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.{Duration, Period, ZoneId, ZonedDateTime}

class MeetingParticipantStorageSpec extends AsyncFreeSpec with AsyncIOSpec with BeforeAndAfter with Matchers {

  "PostgresMeetingParticipantStorage" - {
    "should add list of participants to meeting" in {
      PostgresDataBaseFactory.transactor
        .use { transactor =>
          val userStorage = PostgresUserStorage(UserSql.make, transactor)
          val meetingStorage = PostgresMeetingStorage(MeetingSql.make, transactor)
          val meetingParticipantStorage = PostgresMeetingParticipantStorage(MeetingParticipantSql.make, transactor)
          for {
            hostId <- userStorage.addUser(CreateUser(ChatId(42)))
            location <- sql"""INSERT INTO meeting_reminder.location (id, address) VALUES (0, '108')""".update.run
              .transact(transactor)
            meetingId <- hostId match {
              case Left(error) => IO { Left(error) }
              case Right(hostId: UserId) =>
                meetingStorage
                  .create(
                    CreateMeeting(
                      MeetingDateTime(
                        ZonedDateTime.of(2023, 12, 1, 8, 12, 30, 0, ZoneId.of("UTC")).plus(Period.ofDays(3))
                      ),
                      MeetingDuration(Duration.ofMinutes(30)),
                      MeetingTitle("Test-meeting"),
                      LocationId(0),
                      MeetingHost(hostId)
                    )
                  )
                  .map {
                    case Left(error: AppError)       => Left(error)
                    case Right(meetingId: MeetingId) => Right(meetingId)
                  }
            }
            participantsIdsEither <- List
              .range(80, 83)
              .map((number: Int) => userStorage.addUser(CreateUser(ChatId(number))))
              .sequence
            participantsIds <- IO {
              participantsIdsEither.partitionMap(identity) match {
                case (Nil, rights) => Right(rights)
                case (lefts, _)    => Left(lefts)
              }
            }
            participants <- participantsIds match {
              case Left(error :: _) => IO { Left(error) }
              case Right(participantsIds) =>
                meetingId match {
                  case Left(error) => IO { Left(error) }
                  case Right(meetingId: MeetingId) =>
                    meetingParticipantStorage.addMeetingParticipants(meetingId, participantsIds).map {
                      case Left(error) => Left(error)
                      case Right(())   => Right(())
                    }
                }
            }
            participantsUserIdsCheck <- meetingId match {
              case Left(error) => IO { Left(error) }
              case Right(meetingId: MeetingId) =>
                meetingParticipantStorage.getMeetingParticipants(meetingId)
            }
            participantsUsersCheck <- participantsUserIdsCheck match {
              case Left(error) => IO { Left(error) }
              case Right(userIds) =>
                userIds
                  .map { userStorage.getUserById }
                  .sequence
                  .map { lst => lst.sequence }
            }
          } yield participantsUsersCheck
        }
        .asserting {
          case Left(error) => fail(s"Failed to create a user with such ChatId + $error")
          case Right(participants) =>
            println(participants.collect { case Some(value) => value.chatId }.toSet)
            participants
              .collect { case Some(value) => value.chatId }
              .toSet
              .shouldEqual(List.range(80, 83).map { ChatId(_) }.toSet)
        }
    }
  }
}

package storage

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import com.github.nscala_time.time.Imports
import com.opentable.db.postgres.embedded.{EmbeddedPostgres, LiquibasePreparer}
import com.opentable.db.postgres.junit.{EmbeddedPostgresRules, PreparedDbRule}
import dao.MeetingSql
import domain._
import doobie.WeakAsync.doobieWeakAsyncForAsync
import io.github.liquibase4s.cats.CatsMigrationHandler._
import doobie.hikari.HikariTransactor
import doobie.implicits.toSqlInterpolator
import doobie.{ExecutionContexts, Transactor}
import org.junit.Rule
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.PostgreSQLContainer
import storage.MeetingStorage
import doobie._
import doobie.implicits._
import doobie.syntax._
import error.AppError
import io.github.liquibase4s.{Liquibase, LiquibaseConfig}
import cats.syntax.either._

import java.time.{Duration, Period, ZoneId, ZonedDateTime}
import scala.concurrent.ExecutionContext

class MeetingStorageSpec extends AsyncFreeSpec with AsyncIOSpec with BeforeAndAfter with Matchers {

  "PostgresMeetingStorage" - {
    "should create a new meeting" in {
      val createMeeting = CreateMeeting(
        MeetingDateTime(ZonedDateTime.of(2023, 12, 1, 8, 12, 30, 0, ZoneId.of("UTC")).plus(Period.ofDays(3))),
        MeetingDuration(Duration.ofMinutes(30)),
        MeetingTitle("Test-meeting"),
        LocationId(0),
        MeetingHost(UserId(0))
      )
      PostgresDataBaseFactory.transactor
        .use { transactor =>
          for {
            _ <- sql"""INSERT INTO meeting_reminder."user" (id, chat_id) VALUES (0, 42)""".update.run.transact(
              transactor
            )
            _ <- sql"INSERT INTO meeting_reminder.location (id, address) VALUES (0, '108')".update.run.transact(
              transactor
            )
            meetingIdEither <- PostgresMeetingStorage(MeetingSql.make, transactor).create(createMeeting)
            createdMeetingWithId <-
              meetingIdEither match {
                case Left(error) => IO { Left(error) }
                case Right(meetingId) =>
                  PostgresMeetingStorage(MeetingSql.make, transactor).getById(meetingId).flatMap {
                    case Left(error)    => IO { Left(error) }
                    case Right(meeting) => IO { (meetingId, meeting).asRight }
                  }
              }
          } yield createdMeetingWithId
        }
        .asserting {
          case Left(error: AppError) => fail(s"Failed to create meeting: $error")
          case Right((meetingId: MeetingId, meeting: Meeting)) =>
            val checkMeeting = Meeting(meetingId, createMeeting)
            meeting.dateTime.dateTime.toLocalDateTime shouldEqual checkMeeting.dateTime.dateTime.toLocalDateTime
            meeting.duration shouldBe checkMeeting.duration
            meeting.locationId shouldBe checkMeeting.locationId
            meeting.host     shouldBe meeting.host
        }
    }

//    "should retrieve meetings hosted by a specific host" in {
//      // Define a MeetingHost for testing purposes
//      val meetingHost = MeetingHost(UserId(123))
//
//      storage.getMeetingsHostedBy(meetingHost).attempt.unsafeToFuture().map {
//        case Left(error)        => fail(s"Failed to retrieve meetings hosted by the host: $error")
//        case Right(meetingList) =>
//          // Successfully retrieved the meetings, assert on the retrieved list
//          // Example: assert some conditions about the size or properties of the retrieved meetingList
//          meetingList should not be empty
//      }
//    }
//
//    "should cancel a meeting by its ID" in {
//      // Create a sample meeting to be canceled (you may use an existing meeting ID)
//      val meetingIdToCancel = MeetingId(456)
//
//      storage.cancel(meetingIdToCancel).attempt.unsafeToFuture().map {
//        case Left(error) => fail(s"Failed to cancel meeting: $error")
//        case Right(_)    => // Meeting canceled successfully
//          // Optionally, retrieve the canceled meeting by ID and assert its canceled status
//          storage.getById(meetingIdToCancel).unsafeToFuture().map {
//            case Left(_) => // Meeting is canceled as expected
//            case Right(cancelledMeeting) =>
//              fail(s"Meeting cancellation failed, retrieved meeting: $cancelledMeeting")
//          }
//      }
//    }
//
//    "should retrieve a meeting by its ID" in {
//      // Define a MeetingId for testing purposes
//      val meetingIdToRetrieve = MeetingId(789)
//
//      storage.getById(meetingIdToRetrieve).attempt.unsafeToFuture().map {
//        case Left(error)    => fail(s"Failed to retrieve meeting by ID: $error")
//        case Right(meeting) =>
//          // Successfully retrieved the meeting, assert on its properties or existence
//          // Example: assert some conditions about the retrieved meeting
//          meeting.id shouldBe meetingIdToRetrieve
//      }
//    }
  }
}

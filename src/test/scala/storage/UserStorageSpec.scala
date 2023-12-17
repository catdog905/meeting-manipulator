package storage

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dao.{MeetingSql, UserSql}
import domain.{ChatId, CreateMeeting, CreateUser, LocationId, Meeting, MeetingDateTime, MeetingDuration, MeetingHost, MeetingId, MeetingTitle, UserId}
import doobie.implicits.toSqlInterpolator
import error.AppError
import io.github.liquibase4s.cats.CatsMigrationHandler.liquibaseHandlerForCats
import org.scalatest.BeforeAndAfter
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import doobie.implicits._

import java.time.{Duration, Period, ZoneId, ZonedDateTime}

class UserStorageSpec extends AsyncFreeSpec with AsyncIOSpec with BeforeAndAfter with Matchers {

  "PostgresUserStorage" - {
    "should create new User from new ChatId" in {
      PostgresDataBaseFactory.transactor
        .use { transactor =>
          PostgresUserStorage(UserSql.make, transactor).addUser(CreateUser(ChatId(42)))
        }
        .asserting {
          case Left(error) => fail(s"Failed to create a user with such ChatId + $error")
          case Right(userId) =>
            userId shouldBe a[UserId]
        }
    }

    "should return UserId of user with specified ChatId" in {
      PostgresDataBaseFactory.transactor
        .use { transactor =>
          for {
            _ <-
              sql"""INSERT INTO meeting_reminder."user" (id, chat_id) VALUES (88, 42)""".update.run.transact(
                transactor
              )
            userIdOption <- PostgresUserStorage(UserSql.make, transactor).getUserIdByChatId(ChatId(42))
          } yield userIdOption
        }
        .asserting {
          case None => fail(s"Failed to found a user with such ChatId")
          case Some(userId) =>
            userId shouldEqual UserId(88)
        }
    }
  }
}

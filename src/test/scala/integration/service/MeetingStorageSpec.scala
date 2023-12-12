package integration.service

import cats.effect.IO
import com.opentable.db.postgres.embedded.{EmbeddedPostgres, LiquibasePreparer}
import com.opentable.db.postgres.junit.{EmbeddedPostgresRules, PreparedDbRule}
import dao.MeetingSql
import domain._
import doobie.Transactor
import org.junit.Rule
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import storage.MeetingStorage

import java.time.ZonedDateTime
import scala.concurrent.ExecutionContext

class MeetingStorageSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  // https://www.baeldung.com/scala/testcontainers-scala

  /*val _embeddedPostgresRules: PreparedDbRule =
    EmbeddedPostgresRules.preparedDatabase(LiquibasePreparer.forClasspathLocation("db/migrations/changelog.xml"));

  @Rule
  def embeddedPostgres: PreparedDbRule = _embeddedPostgresRules

  "hello" should "add 1 point" in {
    val db = EmbeddedPostgres.builder.start
    val dataSource = db.getPostgresDatabase
    val transactor = Transactor.fromDataSource[IO](dataSource, ExecutionContext.global)
    val storage = MeetingStorage.make(MeetingSql.make, transactor)
    // given
    storage.create(
      CreateMeeting(
        MeetingDateTime(ZonedDateTime.now()),
        MeetingTitle("title"),
        LocationId(11),
        MeetingHost("host"),
        Seq.empty
      )
    )
  }*/

}

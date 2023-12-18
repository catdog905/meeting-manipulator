package storage

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import io.github.liquibase4s.{Liquibase, LiquibaseConfig, MigrationHandler}
import org.testcontainers.containers.PostgreSQLContainer

object PostgresDataBaseFactory {
  def transactor(implicit migrationHandler: MigrationHandler[IO]): Resource[IO, Transactor[IO]] = for {
    postgres <- makePostgres
    ce <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      postgres.getJdbcUrl,
      postgres.getUsername,
      postgres.getPassword,
      ce
    )
    _ <- Liquibase[IO](
      LiquibaseConfig(
        url = postgres.getJdbcUrl,
        user = postgres.getUsername,
        password = postgres.getPassword,
        driver = "org.postgresql.Driver",
        changelog = "db/migrations/changelog.xml"
      )
    ).migrate().toResource
  } yield xa

  private def makePostgres: Resource[IO, PostgreSQLContainer[Nothing]] =
    Resource.make(IO {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres")
      container.start()
      container
    })(c => IO(c.stop()))

}

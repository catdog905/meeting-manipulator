package service

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import dao.UserSql
import domain.errors.{AppError, InternalError}
import domain.{CreateUser, UserId}
import doobie.Transactor
import doobie.implicits._

trait UserStorage[F[_]] {
  def addUser(createUser: CreateUser): F[Either[AppError, UserId]]
}

object UserStorage {
  private final class Impl(userSql: UserSql, transactor: Transactor[IO]) extends UserStorage[IO] {
    override def addUser(createUser: CreateUser): IO[Either[AppError, UserId]] =
      userSql.addNewUser(createUser).transact(transactor).attempt.map {
        case Left(th)      => InternalError(th).asLeft
        case Right(userId) => userId.asRight
      }
  }
}

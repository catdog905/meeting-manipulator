package storage

import cats.effect.IO
import cats.syntax.option._
import cats.effect.std.{Random, UUIDGen}
import cats.implicits.catsSyntaxEitherId
import dao.UserSql
import domain.{ChatId, CreateUser, User, UserId}
import doobie.Transactor
import doobie.implicits._
import error.{AppError, InternalError, NoSuchUserFound, StorageError, UserAlreadyExists, UserAlreadyJoinedMeeting}

import scala.collection.mutable

trait UserStorage[F[_]] {
  def addUser(createUser: CreateUser): F[Either[AppError, UserId]]
  def getUserIdByChatId(chatId: ChatId): F[Option[UserId]]
  def getUserById(userId: UserId): F[Either[AppError, Option[User]]]
  def getUsersByIds(userIds: List[UserId]): F[Either[AppError, List[User]]]
}

final case class PostgresUserStorage(userSql: UserSql, transactor: Transactor[IO]) extends UserStorage[IO] {
  override def addUser(createUser: CreateUser): IO[Either[AppError, UserId]] =
    for {
      userExists <- getUserIdByChatId(createUser.chatId)
      result <- userExists match {
        case Some(userId) =>
          IO.pure(Left(UserAlreadyExists(userId).asPersistenceError))
        case None =>
          userSql.addNewUser(createUser).transact(transactor).attempt.map {
            case Left(th) => InternalError(th).asLeft
            case Right(userId) => userId.asRight
          }
      }
    } yield result

  override def getUserById(userId: UserId): IO[Either[AppError, Option[User]]] =
    userSql.getUserById(userId).transact(transactor).attempt.map {
      case Left(th) => InternalError(th).asLeft
      case Right(option) => option.asRight
    }

  override def getUsersByIds(userIds: List[UserId]): IO[Either[AppError, List[User]]] = ???

  override def getUserIdByChatId(chatId: ChatId): IO[Option[UserId]] =
    userSql.getUserByChatId(chatId).transact(transactor).attempt.map {
      case Left(_)      => None
      case Right(userId) => userId.some
    }
}

final case class InMemoryUserStorage(storage: mutable.Map[UserId, User], random: Random[IO]) extends UserStorage[IO] {

  override def addUser(createUser: CreateUser): IO[Either[AppError, UserId]] =
    for {
      userExists <- IO {
        storage.find { case (_, user) => user.chatId == createUser.chatId }
      }
      result <- userExists match {
        case Some((userId, _)) =>
          IO.pure(Left(UserAlreadyExists(userId).asPersistenceError))
        case None =>
          for {
            userId <- random.nextLong
            _ <- IO {
              val newUser = User(UserId(userId), createUser)
              storage += (UserId(userId) -> newUser)
            }
          } yield Right(UserId(userId))
      }
    } yield result

  override def getUserById(userId: UserId): IO[Either[AppError, Option[User]]] =
    ???

  override def getUsersByIds(userIds: List[UserId]): IO[Either[AppError, List[User]]] =
    IO { storage.filter({ case (id, _) => userIds.contains(id) }).values.toList.asRight }

  override def getUserIdByChatId(chatId: ChatId): IO[Option[UserId]] =
    IO { storage.collectFirst { case (userId, user) if user.chatId == chatId => userId } }
}

object InMemoryUserStorage {
  def apply(random: Random[IO]): InMemoryUserStorage = InMemoryUserStorage(mutable.Map(), random)
}

package dao

import domain._
import doobie._
import doobie.implicits._

trait UserSql {
  def addNewUser(createUser: CreateUser): ConnectionIO[UserId]
  def getUserByChatId(chatId: ChatId): ConnectionIO[UserId]
  def getUserById(id: UserId): ConnectionIO[Option[User]]
}

object UserSql {
  object sqls {

    def insertUser(createUser: CreateUser): Update0 =
      sql"""
           INSERT INTO meeting_reminder."user" (chat_id)
           VALUES (${createUser.chatId})
         """.update

    def selectUserIdByChatId(chatId: ChatId): Query0[UserId] =
      sql"""
           SELECT id, chat_id
           FROM meeting_reminder."user"
           WHERE chat_id = $chatId
         """.query[UserId]

    def selectUserById(userId: UserId): Query0[User] =
      sql"""
           SELECT id, chat_id
           FROM meeting_reminder."user"
           WHERE id = $userId
         """.query[User]
  }

  private final class Impl extends UserSql {

    import sqls._

    override def addNewUser(createUser: CreateUser): doobie.ConnectionIO[UserId] =
      insertUser(createUser)
        .withUniqueGeneratedKeys[UserId]("id")

    override def getUserByChatId(chatId: ChatId): doobie.ConnectionIO[UserId] =
      selectUserIdByChatId(chatId).unique

    override def getUserById(id: UserId): doobie.ConnectionIO[Option[User]] =
      selectUserById(id).option
  }

  def make: UserSql = new Impl
}

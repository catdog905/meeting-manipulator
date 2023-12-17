package dao

import domain._
import doobie._
import doobie.implicits._

trait UserSql {
  def addNewUser(createUser: CreateUser): ConnectionIO[UserId]
  def getUserByChatId(chatId: ChatId): ConnectionIO[UserId]
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
  }

  private final class Impl extends UserSql {

    import sqls._

    override def addNewUser(createUser: CreateUser): doobie.ConnectionIO[UserId] =
      insertUser(createUser)
        .withUniqueGeneratedKeys[UserId]("id")

    override def getUserByChatId(chatId: ChatId): doobie.ConnectionIO[UserId] =
      selectUserIdByChatId(chatId).unique
  }

  def make: UserSql = new Impl
}

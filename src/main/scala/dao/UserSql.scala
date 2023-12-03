package dao

import domain._
import doobie._
import doobie.implicits._

trait UserSql {
  def addNewUser(createUser: CreateUser): ConnectionIO[UserId]
}

object UserSql {
  object sqls {

    def insertUser(createUser: CreateUser): Update0 =
      sql"""
           INSERT INTO meeting_reminder."user"
           VALUES (chat_id)
           (${createUser.chat_id})
         """.update
  }

  private final class Impl extends UserSql {

    import sqls._

    override def addNewUser(createUser: CreateUser): doobie.ConnectionIO[UserId] =
      insertUser(createUser)
        .withUniqueGeneratedKeys("id")
  }

  def make: UserSql = new Impl
}

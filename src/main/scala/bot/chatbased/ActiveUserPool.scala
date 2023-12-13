package bot.chatbased

import com.bot4s.telegram.models.Message
import domain.UserId

import scala.collection.mutable

trait ActiveUserPool {
  def updateUserStateByMessage(userId: UserId, message: Message): UserStateUpdateResponse
}

sealed trait UserStateUpdateResponse

case class ArgumentRequest() extends UserStateUpdateResponse

case class RepeatArgumentRequest() extends UserStateUpdateResponse

case class CommandSummary() extends UserStateUpdateResponse


case class InMemoryActiveUserPool(storage: mutable.Map[UserId, UserState]) extends ActiveUserPool  {
  override def updateUserStateByMessage(userId: UserId, message: Message): UserStateUpdateResponse = ???
}

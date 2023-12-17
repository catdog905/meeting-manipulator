package domain

//@derive(loggable, encoder, decoder)
case class User(id: UserId, chatId: ChatId)

object User {

  def apply(userId: UserId, createUser: CreateUser): User = User(userId, createUser.chatId)
}

case class CreateUser(
  chatId: ChatId
)

package domain

//@derive(loggable, encoder, decoder)
case class User(id: UserId, chatId: ChatId)

object User {

  def apply(userId: UserId, createUser: CreateUser): User = User(userId, createUser.chat_id)
}

case class CreateUser(
  chat_id: ChatId
)

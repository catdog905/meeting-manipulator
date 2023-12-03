package domain

//@derive(loggable, encoder, decoder)
case class User(id: UserId)

case class CreateUser(
  chat_id: ChatId
)

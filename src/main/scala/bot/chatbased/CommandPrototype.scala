package bot.chatbased

import bot.command.UserCommand
import domain.UserId
import error.AppError

trait CommandPrototype[F[_], O] {
  def argumentNames: List[String]
  def addArgument(argumentName: String, argumentValue: String): Either[AppError, CommandPrototype[F, O]]
  def build(initiator: UserId): Option[UserCommand[F, O]]
}
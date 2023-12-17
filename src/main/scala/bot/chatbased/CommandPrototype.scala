package bot.chatbased

import bot.command.UserCommand
import domain.{Format, UserId}
import error.AppError

trait CommandPrototype[F[_], O] {
  def argumentNamesWithFormat: Map[String, Format]
  def addArgument(argumentName: String, argumentValue: String): F[Either[AppError, CommandPrototype[F, O]]]
  def build(initiator: UserId): Option[UserCommand[F, O]]
}
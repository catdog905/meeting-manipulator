package bot.chatbased

import cats.Show
import error.{AppError, IncorrectInput}

sealed trait BotResponse[+O]
case class ArgumentRequest(error: Option[IncorrectInput] = None) extends BotResponse[Nothing]
case class Panic(error: AppError) extends BotResponse[Nothing]
case class CommandSummary(result: String) extends BotResponse[String]
object CommandSummary {
  val Done: CommandSummary = CommandSummary("Done")
  def apply(error: AppError): CommandSummary = CommandSummary(error.message)
  def apply[O](output: O)(implicit show: Show[O]): CommandSummary = CommandSummary(Show[O].show(output))
}
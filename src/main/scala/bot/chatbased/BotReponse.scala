package bot.chatbased

import cats.Show
import error.{AppError, IncorrectInput}
import tofu.logging.derivation.show.generate

sealed trait BotResponse[+O]
object BotResponse {
  implicit def show[F[_], O]: Show[BotResponse[O]] = Show.show {
    case argumentRequest: ArgumentRequest[_] =>
      Show[ArgumentRequest[F]].show(argumentRequest.asInstanceOf[ArgumentRequest[F]])
    case panic: Panic                   => Show[Panic].show(panic)
    case commandSummary: CommandSummary => Show[CommandSummary].show(commandSummary)
    case noResponse: NoResponse         => Show[NoResponse].show(noResponse)
  }
}

case class NoResponse() extends BotResponse[Nothing] {
  implicit val show: Show[NoResponse] = Show.show(_ => "")
}

case class ArgumentRequest[F[_]](prototype: CommandPrototype[F, String], error: Option[IncorrectInput] = None)
  extends BotResponse[Nothing]
object ArgumentRequest {
  implicit def show[F[_]]: Show[ArgumentRequest[F]] = {
    Show.show(argumentRequest =>
      """You can see the list of required arguments below
        |Write them one by on in the following format:
        |"$argumentName:$argumentValue"
        |""".stripMargin
        +
          argumentRequest.prototype.argumentNamesWithFormat
            .map({ case (name, value) => (name, value.show) })
            .mkString("\n")
    )
  }
}

case class Panic(error: AppError) extends BotResponse[Nothing]
object Panic {
  implicit val panicShow: Show[Panic] = Show.show { case Panic(error) => error.message + error.cause }
}

case class CommandSummary(result: String) extends BotResponse[String]
object CommandSummary {
  val Done: CommandSummary = CommandSummary("Done")
  implicit val commandSummaryShow: Show[CommandSummary] = Show.show(commandSummary => commandSummary.result)
  def apply(error: AppError): CommandSummary = CommandSummary(error.message + error.cause)
  def apply[O](output: O)(implicit show: Show[O]): CommandSummary = CommandSummary(Show[O].show(output))
}

package error

sealed trait InteractionError {
  self =>

  val message: String
  val cause: Option[Throwable] = None
  val advice: Option[String] = None

  val asApplicationError: AppInteractionError = AppInteractionError(self)
}

final case class IncorrectInput(message: String) extends InteractionError

object IncorrectInput {
  def apply(): IncorrectInput = IncorrectInput("Incorrect input")
  def apply(parsingError: ParsingError): IncorrectInput = IncorrectInput(parsingError.message)
}

final case class ParsingError(objective: String, cause: Throwable) {
  val message: String = s"Failed to parse $objective"
}

package error

sealed trait InteractionError {
  self =>

  val message: String
  val cause: Option[Throwable] = None
  val advice: Option[String] = None

  val asApplicationError: AppInteractionError = AppInteractionError(self)
}

final case class IncorrectInput(message: String) extends InteractionError //TODO

object IncorrectInput {
  def apply(): IncorrectInput = IncorrectInput("Incorrect input")
}
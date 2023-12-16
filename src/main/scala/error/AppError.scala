package error

import cats.implicits.catsSyntaxOptionId

sealed trait AppError {
  val message: String
  val cause: Option[Throwable] = None
}

final case class AppPersistenceError(error: StorageError) extends AppError {
  override val message: String = s"PersistenceError: ${error.message}"
  override val cause: Option[Throwable] = error.cause
}

final case class AppInteractionError(error: InteractionError) extends AppError {
  override val message: String = s"InteractionError: ${error.message}"
  override val cause: Option[Throwable] = error.cause
}

final case class InternalError(
  cause0: Throwable
) extends AppError {
  override val message: String = "Internal error"
  override val cause: Option[Throwable] = cause0.some
}

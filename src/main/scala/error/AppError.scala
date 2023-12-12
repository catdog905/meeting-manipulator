package error

import cats.implicits.catsSyntaxOptionId

sealed abstract class AppError(
  val message: String,
  val cause: Option[Throwable] = None
)

final case class PersistenceError(error: StorageError)
  extends AppError(s"PersistenceError: ${error.message}", error.cause)

final case class InternalError(
  cause0: Throwable
) extends AppError("Internal error", cause0.some)

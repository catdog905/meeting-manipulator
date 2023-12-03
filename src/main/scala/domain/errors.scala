package domain

import cats.implicits.catsSyntaxOptionId

object errors {
  sealed abstract class AppError(
    val message: String,
    val cause: Option[Throwable] = None
  )

  case class InternalError(
    cause0: Throwable
  ) extends AppError("Internal error", cause0.some)
}

package bot.command

import cats.Show
import domain.UserId
import error.AppError
import tofu.logging.derivation.show.generate

final case class GetArrangedMeetingsCommand[F[_]](initiator: UserId) extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] = ???

  override val showO: Show[Nothing] = _ => ""
}

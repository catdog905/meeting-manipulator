package bot.command

import domain.UserId
import error.AppError

abstract final case class GetArrangedMeetingsCommand[F[_]](initiator: UserId) extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] = ???

}

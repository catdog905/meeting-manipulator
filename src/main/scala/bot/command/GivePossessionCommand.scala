package bot.command

import domain.UserId
import error.AppError

abstract final case class GivePossessionCommand[F[_]](newHost: UserId, initiator: UserId)
  extends UserCommand[F, Nothing] {
  override def act: F[Either[AppError, Option[Nothing]]] = ???

}

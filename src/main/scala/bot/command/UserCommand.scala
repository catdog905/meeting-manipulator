package bot.command

import cats.Show
import domain.UserId
import error.AppError

trait UserCommand[+F[_], O] {
  val initiator: UserId
  val showO: Show[O]
  def act: F[Either[AppError, Option[O]]]
}
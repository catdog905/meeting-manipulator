package bot

import bot.command.UserCommand
import cats.Monad
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO}
import cats.syntax.functor._
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models._
import domain.UserId
import storage.{CommandsAwareStorage, MeetingStorage}
import sttp.client3.SttpBackend

case class MeetingReminderBot[F[_]: Monad : Async](token: String, backend: SttpBackend[F, Any], commandsAwareStorage: CommandsAwareStorage[F]
) extends TelegramBot[F](token, backend)
  with Polling[F] {
  implicit val implicitCommandsAwareStorage: CommandsAwareStorage[F] = commandsAwareStorage
  implicit val implicitCommandsAwareStoradge: CommandsAwareStorage[IO] = commandsAwareStorage.asInstanceOf[CommandsAwareStorage[IO]]

  override def receiveMessage(msg: Message): F[Unit] = {
    request(
      SendMessage(
        msg.source, // 1399878184,
        msg.text match {
          case Some(text)    => UserCommand[IO](text, UserId(0)).fold("Incorrect command")(_.act.unsafeRunSync().toString)
          case None          => "meow"
        }
      )
    ).void
  }

}

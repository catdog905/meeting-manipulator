package bot

import cats.effect.Async
import cats.syntax.functor._
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models._
import service.MeetingStorage
import sttp.client3.SttpBackend

case class MeetingReminderBot[F[_]: Async](
  token: String,
  backend: SttpBackend[F, Any],
  storage: MeetingStorage[F]
) extends TelegramBot[F](token, backend)
  with Polling[F] {

  override def receiveMessage(msg: Message): F[Unit] = {
    request(
      SendMessage(
        msg.source, // 1399878184,
        msg.text match {
          case Some("Hello") => s"Goodbye ${msg.source}"
          case Some(text)    => "Test"
          case None          => "meow"
        }
      )
    ).void
  }

  /*override def receiveExtMessage(extMessage: (Message, Option[User])): F[Unit] =
    request(
      SendMessage(
        extMessage._1.source,
        extMessage._1.text match {
          case Some("Hello") => s"ExtMessage Goodbye ${extMessage._1.source} ${extMessage._2.get.username.get}"
          case Some(text) => "ExtMessage Катя сама любимая колбаска"
          case None => "ExtMessage блдолро"
        }
      )
    ).void*/

  override def receiveEditedMessage(editedMessage: Message): F[Unit] =
    throw new RuntimeException()
}

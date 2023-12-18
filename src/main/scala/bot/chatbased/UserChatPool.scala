package bot.chatbased

import bot.command.UserCommand
import cats.syntax.all._
import cats.{Applicative, Monad}
import com.bot4s.telegram.models.Message
import domain.UserId
import error.AppError
import storage.CommandsAwareStorage

import scala.collection.mutable

//UserChatPool should became immutable or cooperate with doobie
trait UserChatPool[F[_]] {
  def updatePoolByUserMessage(userId: UserId, message: Message): F[BotResponse[_]]
}

class InMemoryUserChatPool[F[_]: Monad](storage: mutable.Map[UserId, UserState[F]])(implicit
  val commandsAwareStorage: CommandsAwareStorage[F]
) extends UserChatPool[F] {

  override def updatePoolByUserMessage(userId: UserId, message: Message): F[BotResponse[_]] = {
    (storage.get(userId) match {
      case None =>
        storage.addOne((userId, InactiveState(userId)))
        InactiveState(userId)
      case Some(state) => state
    }).updateStateByMessage(message).flatMap {
//      case Left(AppInteractionError(error: IncorrectInput)) =>
//        Applicative[F].pure(Panic(error))
      case Left(error: AppError) =>
        Applicative[F].pure(Panic(error))
      case Right(ReadyToExecuteCommand(command: UserCommand[F, _])) => {
        storage.put(userId, InactiveState(userId))
        command.act.flatMap {
          case Left(error: AppError) =>
            Applicative[F].pure(CommandSummary(error))
          case Right(None) =>
            Applicative[F].pure(CommandSummary.Done)
          case Right(Some(commandOutput)) =>
            Applicative[F].pure(CommandSummary(commandOutput)(command.showO))
        }
      }
      case Right(state: CommandBuildingState[_, _]) =>
        state.receivingStatus match{
          case receivingStatus: ArgumentsFetching[_, _] =>
            storage.put(userId, state)
            Applicative[F].pure(ArgumentRequest(receivingStatus.prototype.asInstanceOf[CommandPrototype[F, String]]))
          case _ => 
            storage.put(userId, state)
            Applicative[F].pure(NoResponse())
        }
      case Right(state: UserState[F]) =>
        storage.put(userId, state)
        Applicative[F].pure(NoResponse())
    }
  }
}

object InMemoryUserChatPool {
  def apply[F[_]: Monad](commandsAwareStorage: CommandsAwareStorage[F]
  ): InMemoryUserChatPool[F] = {
    implicit val implicitCommandAwareStorage: CommandsAwareStorage[F] = commandsAwareStorage
    new InMemoryUserChatPool[F](mutable.Map.empty[UserId, UserState[F]])
  }
}

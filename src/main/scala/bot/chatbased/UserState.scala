package bot.chatbased

import bot.command.ArrangeMeetingCommand.ArrangeMeetingCommandPrototype
import bot.command.{ArrangeMeetingCommand, UserCommand}
import cats.Monad
import cats.implicits.catsSyntaxEitherId
import com.bot4s.telegram.models.Message
import error.{AppError, IncorrectInput}
import storage.CommandsAwareStorage
import dev.bgahagan.regex.intrpl._
import domain.UserId

sealed trait UserState {
  def updateStateByMessage(message: Message): Either[AppError, UserState]
}

// 0) User in unknown state
// 1) Declare a command
// 2) Bot ask data
// 3) Bot receive data
// 4) Repeat 2-3
// 5) Bot prints the result and returns User to unknown state

case class InactiveState[F[_]: Monad, O](userId: UserId)(implicit val commandsAwareStorage: CommandsAwareStorage[F])
  extends UserState {
  override def updateStateByMessage(message: Message): Either[AppError, UserState] =
    message.text match {
      case None => IncorrectInput().asApplicationError.asLeft
      case Some(text) if text.startsWith("/new_meeting") =>
        CommandBuildingState(
          userId,
          ArgumentsFetching(ArrangeMeetingCommandPrototype[F](), userId)
        ).asRight
      case _ => IncorrectInput("Incorrect command").asApplicationError.asLeft
    }
}
case class CommandBuildingState[F[_]: Monad, O](userId: UserId, receivingStatus: CommandBuildingStatus[F, O])(implicit
  val commandsAwareStorage: CommandsAwareStorage[F]
) extends UserState {
  override def updateStateByMessage(message: Message): Either[AppError, UserState] = {
    message.text match {
      case Some(text) if text.startsWith("/new_meeting") =>
        CommandBuildingState(
          userId,
          ArgumentsFetching(ArrangeMeetingCommandPrototype[F](), userId)
        ).asRight
      case _ =>
        receivingStatus.appendArgumentByMessage(message) match {
          case Left(error)                            => error.asLeft
          case Right(status: ArgumentsFetching[F, O]) => CommandBuildingState(userId, status).asRight
          case Right(status: BuildingDone[F, O])      => ReadyToExecuteCommand[F, O](status.userCommand).asRight
        }
    }
  }
}

case class ReadyToExecuteCommand[F[_]: Monad, O](command: UserCommand[F, O]) extends UserState {

  override def updateStateByMessage(message: Message): Either[AppError, UserState] =
    ReadyToExecuteCommand[F, O](command).asRight
}

sealed trait CommandBuildingStatus[+F[_], +O] {
  def appendArgumentByMessage(message: Message): Either[AppError, CommandBuildingStatus[F, O]]
}

case class ArgumentsFetching[F[_]: Monad, O](prototype: CommandPrototype[F, O], initiator: UserId)
  extends CommandBuildingStatus[F, O] {
  override def appendArgumentByMessage(message: Message): Either[AppError, CommandBuildingStatus[F, O]] =
    message.text match {
      case None => IncorrectInput("No arguments provided").asApplicationError.asLeft
      case Some(s"$argumentName:$argumentValue") =>
        prototype.addArgument(argumentName, argumentValue) match {
          case Left(error) => error.asLeft
          case Right(prototype) =>
            prototype.build(initiator) match {
              case None          => ArgumentsFetching(prototype, initiator).asRight
              case Some(command) => BuildingDone[F, O](command).asRight
            }
        }
      case Some(_) => IncorrectInput("Wrong arguments format").asApplicationError.asLeft
    }
}
case class BuildingDone[F[_], O](userCommand: UserCommand[F, O]) extends CommandBuildingStatus[F, O] {
  self =>
  override def appendArgumentByMessage(message: Message): Either[AppError, CommandBuildingStatus[F, O]] = self.asRight
}

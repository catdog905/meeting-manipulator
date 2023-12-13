package bot.chatbased

import bot.command.UserCommand
import cats.implicits.catsSyntaxEitherId
import com.bot4s.telegram.models.Message
import error.{AppError, IncorrectInput}

sealed trait UserState {
  def updateStateByMessage(message: Message): Either[AppError, UserState]
}

// 0) User in unknown state
// 1) Declare a command
// 2) Bot ask data
// 3) Bot receive data
// 4) Repeat 2-3
// 5) Bot prints the result and returns User to unknown state

case object InactiveState extends UserState {
  override def updateStateByMessage(message: Message): Either[AppError, UserState] = ???
    //message.text.fold(IncorrectInput().asApplicationError.asLeft) {
    //case rawInput if rawInput.startsWith("new-meeting") => FetchingCommandArgumentsState(
    //  ???
    //)
  //}
}
case class FetchingCommandArgumentsState[F[_], O](userCommand: UserCommand[F, O]) extends UserState {
  override def updateStateByMessage(message: Message): Either[AppError, UserState] = ???
}

sealed trait ReceivedCommandArguments {
  def appendArgumentByMessage(message: Message): Either[AppError, ReceivedCommandArguments]
}

case object NoArgumentsReceived extends ReceivedCommandArguments {
  override def appendArgumentByMessage(message: Message): Either[AppError, ReceivedCommandArguments] = ???
}

case object SomeArgumentReceived extends ReceivedCommandArguments {
  override def appendArgumentByMessage(message: Message): Either[AppError, ReceivedCommandArguments] = ???
}

case object EveryArgumentReceived extends ReceivedCommandArguments {
  override def appendArgumentByMessage(message: Message): Either[AppError, ReceivedCommandArguments] = ???
}
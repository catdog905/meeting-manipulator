package bot.command

import bot.chatbased.{CommandPrototype}
import cats.implicits.toFunctorOps
import cats.{Applicative, Monad, ParallelArityFunctions, Show}
import domain.{ChatId, CreateMeeting, CreateMeetingWithParticipants, Format, MeetingDateTime, MeetingDuration, MeetingHost, MeetingId, MeetingLocation, MeetingTitle, UserId}
import error.{AppError, IncorrectInput, ParsingError}
import storage.CommandsAwareStorage
import sttp.tapir.server.interceptor.RequestResult.Failure
import cats.syntax.all._

import scala.util.Try

final case class ArrangeMeetingCommand[F[_]: Monad](
  createMeeting: CreateMeeting,
  participants: List[UserId],
  initiator: UserId
)(implicit val commandsAwareStorage: CommandsAwareStorage[F])
  extends UserCommand[F, MeetingId] {
  override def act: F[Either[AppError, Option[MeetingId]]] =
    commandsAwareStorage
      .arrangeMeeting(
        CreateMeetingWithParticipants(createMeeting, participants)
      )
      .map {
        case Left(error: AppError)      => Left(error)
        case Right(meetingId) => Right(Some(meetingId))
      }

  override val showO: Show[MeetingId] = Show[MeetingId]
}

object ArrangeMeetingCommand {
  case class ArrangeMeetingCommandPrototype[FF[_]: Monad](
    dateTime: Option[MeetingDateTime] = None,
    duration: Option[MeetingDuration] = None,
    title: Option[MeetingTitle] = None,
    location: Option[MeetingLocation] = None,
    participants: Option[List[UserId]] = None
  )(implicit val commandsAwareStorage: CommandsAwareStorage[FF])
    extends CommandPrototype[FF, MeetingId] {

    override def build(initiator: UserId): Option[UserCommand[FF, MeetingId]] =
      (dateTime, duration, title, location, participants) match {
        case (Some(datetime), Some(duration), Some(title), Some(location), Some(participants)) =>
          Some(
            ArrangeMeetingCommand[FF](
              CreateMeeting(
                datetime,
                duration,
                title,
                location,
                MeetingHost(initiator)
              ),
              participants,
              initiator
            )
          )
        case _ => None
      }

    override def argumentNamesWithFormat: Map[String, Format] = {
      def addFilledArgumentsToList(arguments: List[(String, Option[Any])]): List[(String, Format)] = arguments match {
        case Nil => Nil
        case argument :: arguments =>
          val nameAndFormat: Option[(String, Format)] = argument match {
            case ("dateTime", None) => ("dateTime", MeetingDateTime.format).some
            case ("duration", None) => ("duration", MeetingDuration.format).some
            case ("title", None)    => ("title", MeetingTitle.format).some
            case ("location", None) => ("location", MeetingLocation.format).some
            case ("participants", None) =>
              ("participants", Format(s"${UserId.format} ${UserId.format} ${UserId.format}...")).some
            case _ => None
          }
          nameAndFormat match {
            case None                => addFilledArgumentsToList(arguments)
            case Some(nameAndFormat) => nameAndFormat :: addFilledArgumentsToList(arguments)
          }
      }
      addFilledArgumentsToList(
        List(
          ("dateTime", dateTime),
          ("duration", duration),
          ("title", title),
          ("location", location),
          ("participants", participants)
        )
      ).toMap
    }

    override def addArgument(
      argumentName: String,
      argumentValue: String
    ): Either[AppError, CommandPrototype[FF, MeetingId]] = {
      def newPrototype(
        dateTime: Option[MeetingDateTime] = dateTime,
        duration: Option[MeetingDuration] = duration,
        title: Option[MeetingTitle] = title,
        location: Option[MeetingLocation] = location,
        participants: Option[List[UserId]] = participants
      ): ArrangeMeetingCommandPrototype[FF] =
        ArrangeMeetingCommandPrototype(
          dateTime,
          duration,
          title,
          location,
          participants
        )
      argumentName match {
        case "dateTime" =>
          MeetingDateTime(argumentValue) match {
            case Left(error)     => IncorrectInput(error).asApplicationError.asLeft
            case Right(dateTime) => newPrototype(dateTime = dateTime.some).asRight
          }
        case "duration" =>
          MeetingDuration(argumentValue) match {
            case Left(error)     => IncorrectInput(error).asApplicationError.asLeft
            case Right(duration) => newPrototype(duration = duration.some).asRight
          }
        case "title"    => newPrototype(title = MeetingTitle(argumentValue).some).asRight
        case "location" => newPrototype(location = MeetingLocation(argumentValue).some).asRight
        case "participants" =>
          val participantStrIds: List[String] = argumentValue.split(" +").toList.filter(_ != "")
          val participantEitherIds: List[Either[ParsingError, UserId]] = participantStrIds.map(UserId(_))
          participantEitherIds.collectFirst { case Left(error) => error } match {
            case None =>
              newPrototype(
                participants = participantEitherIds.collect { case Right(userId) => userId }.some
              ).asRight
            case Some(error) => IncorrectInput(error).asApplicationError.asLeft
          }
        case _ => IncorrectInput(s"No such argument with name $argumentName").asApplicationError.asLeft
      }
    }
  }
}

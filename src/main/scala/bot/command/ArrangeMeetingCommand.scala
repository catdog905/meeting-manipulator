package bot.command

import bot.chatbased.CommandPrototype
import cats.implicits.toFunctorOps
import cats.{Applicative, Monad, ParallelArityFunctions, Show}
import domain.{ChatId, CreateMeeting, CreateMeetingWithParticipants, Format, LocationId, MeetingDateTime, MeetingDuration, MeetingHost, MeetingId, MeetingLocation, MeetingTitle, UserId}
import error.{AppError, IncorrectInput, NoSuchUserFound, ParsingError}
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
        case Left(error: AppError) => Left(error)
        case Right(meetingId)      => Right(Some(meetingId))
      }

  override val showO: Show[MeetingId] = Show[MeetingId]
}

object ArrangeMeetingCommand {
  case class ArrangeMeetingCommandPrototype[FF[_]: Monad](
    dateTime: Option[MeetingDateTime] = None,
    duration: Option[MeetingDuration] = None,
    title: Option[MeetingTitle] = None,
    location: Option[LocationId] = None,
    participants: Option[List[UserId]] = None
  )(implicit val commandsAwareStorage: CommandsAwareStorage[FF])
    extends CommandPrototype[FF, MeetingId] {

    override def build(initiator: UserId): Option[UserCommand[FF, MeetingId]] =
      (dateTime, duration, title, location, participants) match {
        case (Some(datetime), Some(duration), Some(title), Some(locationId), Some(participants)) =>
          Some(
            ArrangeMeetingCommand[FF](
              CreateMeeting(
                datetime,
                duration,
                title,
                locationId,
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
    ): FF[Either[AppError, CommandPrototype[FF, MeetingId]]] = {
      def newPrototype(
        dateTime: Option[MeetingDateTime] = dateTime,
        duration: Option[MeetingDuration] = duration,
        title: Option[MeetingTitle] = title,
        locationId: Option[LocationId] = location,
        participants: Option[List[UserId]] = participants
      ): ArrangeMeetingCommandPrototype[FF] =
        ArrangeMeetingCommandPrototype(
          dateTime,
          duration,
          title,
          locationId,
          participants
        )
      argumentName match {
        case "dateTime" =>
          Applicative[FF].pure {
            MeetingDateTime(argumentValue) match {
              case Left(error)     => IncorrectInput(error).asApplicationError.asLeft
              case Right(dateTime) => newPrototype(dateTime = dateTime.some).asRight
            }
          }
        case "duration" =>
          Applicative[FF].pure {
            MeetingDuration(argumentValue) match {
              case Left(error)     => IncorrectInput(error).asApplicationError.asLeft
              case Right(duration) => newPrototype(duration = duration.some).asRight
            }
          }
        case "title" => Applicative[FF].pure { newPrototype(title = MeetingTitle(argumentValue).some).asRight }
        case "location" =>
          Applicative[FF].pure {
            LocationId(argumentValue) match {
              case Left(error)       => IncorrectInput(error).asApplicationError.asLeft
              case Right(locationId) => newPrototype(locationId = locationId.some).asRight
            }
          }
        case "participants" =>
          val participantStrIds: List[String] = argumentValue.split(" +").toList.filter(_ != "")
          val participantChatIds: List[Either[AppError, ChatId]] = participantStrIds
            .map(ChatId(_))
            .map({
              case Left(error)   => IncorrectInput(error).asApplicationError.asLeft
              case Right(chatId) => chatId.asRight
            })
          val participantUserIds: FF[List[Either[AppError, UserId]]] =
            participantChatIds.map {
              case Left(error) => Applicative[FF].pure { error.asLeft[UserId] }
              case Right(chatId: ChatId) =>
                commandsAwareStorage.userStorage
                  .getUserIdByChatId(chatId)
                  .flatMap {
                    case None         => Applicative[FF].pure { NoSuchUserFound(chatId).asPersistenceError.asLeft[UserId] }
                    case Some(userId) => Applicative[FF].pure { userId.asRight[AppError] }
                  }
            }.sequence
          participantUserIds.flatMap { lst =>
            Applicative[FF].pure {
              lst.collectFirst { case Left(error) => error } match {
                case None =>
                  newPrototype(
                    participants = lst.collect { case Right(userId) => userId }.some
                  ).asRight
                case Some(error) => error.asLeft
              }
            }
          }
        case _ =>
          Applicative[FF].pure { IncorrectInput(s"No such argument with name $argumentName").asApplicationError.asLeft }
      }
    }
  }
}

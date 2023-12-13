package bot.chatbased

sealed trait CommandPrototype

case object ArrangeMeetingCommandPrototype extends CommandPrototype

case object CancelMeetingCommandPrototype extends CommandPrototype

case object GetArrangedMeetingsCommandPrototype extends CommandPrototype

case object GivePossessionCommandPrototype extends CommandPrototype
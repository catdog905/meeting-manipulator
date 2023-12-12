CREATE SCHEMA meeting_reminder;

CREATE TABLE meeting_reminder.location (
    id SERIAL PRIMARY KEY,
    link        varchar[],
    address     varchar[],
    CONSTRAINT location_link_address_exclusivity CHECK (link is NULL or address is NULL)
);

COMMENT ON TABLE meeting_reminder.location IS 'Meeting location (online or offline)';
COMMENT ON COLUMN meeting_reminder.location.link IS 'A link for connection to web meeting';
COMMENT ON COLUMN meeting_reminder.location.address IS 'Meeting address description';

CREATE TABLE meeting_reminder."user" (
    id      int PRIMARY KEY,
    chat_id int NOT NULL
);

COMMENT ON TABLE meeting_reminder."user" IS 'Users in the system';
COMMENT ON COLUMN meeting_reminder."user".id IS 'Id of the user';

CREATE TABLE meeting_reminder.meeting (
    id          SERIAL       PRIMARY KEY,
    date_time   timestamptz  NOT NULL,
    duration    interval     NOT NULL,
    title       varchar[]    NOT NULL,
    location_id int          NOT NULL REFERENCES meeting_reminder.location(id),
    host        int          NOT NULL REFERENCES meeting_reminder."user"(id)
);

COMMENT ON TABLE meeting_reminder.meeting IS 'All conducted and planned meetings';
COMMENT ON COLUMN meeting_reminder.meeting.id IS 'Id of the meeting';
COMMENT ON COLUMN meeting_reminder.meeting.date_time IS 'Date and time of the meeting';
COMMENT ON COLUMN meeting_reminder.meeting.duration IS 'Duration of the meeting';
COMMENT ON COLUMN meeting_reminder.meeting.title IS 'Name of the meeting';
COMMENT ON COLUMN meeting_reminder.meeting.location_id IS 'Location of the meeting';

CREATE TABLE meeting_reminder.meeting_participant (
    meeting_id int REFERENCES meeting_reminder.meeting(id),
    user_id    int REFERENCES meeting_reminder."user"(id),
    PRIMARY KEY (meeting_id, user_id)
);

COMMENT ON TABLE meeting_reminder.meeting_participant IS 'Table with every meeting participant';
COMMENT ON COLUMN meeting_reminder.meeting_participant.meeting_id IS 'Meeting ID';
COMMENT ON COLUMN meeting_reminder.meeting_participant.user_id IS 'User ID';
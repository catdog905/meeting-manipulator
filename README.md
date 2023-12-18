# MeetingReminder
**MeetingReminder** is a bot that helps with meeting conducting.
We can use it to arrange offline meetings with other telegram users.
Before the meeting start bot notifies every participant about the meeting.

## How to run?
Use docker-compose.yaml to build 
```bash
docker-compose up
```

- But due to deadline pressure docker-compose way to run the application is not well-tested.
- Another way to run this application is to remove `scala` service from docker-compose, 
run it to obtain an active database on the computer, and only then run the MeetingReminder application 
with the following environment variables
```
DB_NAME=postgres;DB_PASS=1234;DB_URL=localhost;DB_USER=postgres;BOT_TOKEN=6957676242:AAEIVOZLa7GsTyRSAKeCVUhAUQHQvBXhGew
```
The application will instantiate the initial schema itself using liquibase and run the telegram bot.
For testing purposes I've already sat up the bot in telegram. Its alias is **@echo905_bot**

## About the bot
Overall, the idea was to create bot that can replace or complement the meeting planning functionality of most email 
engines. This bot can be considered as just one another interface for Outlook calendar.

Further, finishing the core functionality, I'm going to integrate this bot with Outlook API and deploy it in Innopolis
university as an interface for booking rooms and planning meetings without necessary to quit the telegram messenger.

### What will be improved:
- Replace chat based user interface with WebApp. Chat based interface was intentionally built for the scala course.
- Finish current functionality
- Add Outlook API
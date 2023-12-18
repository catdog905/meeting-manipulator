FROM sbtscala/scala-sbt:graalvm-ce-22.3.3-b1-java17_1.9.8_3.3.1
# For a Alpine Linux version, comment above and uncomment below:
# FROM 1science/sbt

RUN mkdir -p /meeting_reminder
RUN mkdir -p /meeting_reminder/out

ENV HTTP_PROXY="http://127.0.0.1:2080"
ENV HTTPS_PROXY="https://127.0.0.1:2080"

WORKDIR /meeting_reminder

COPY . /meeting_reminder
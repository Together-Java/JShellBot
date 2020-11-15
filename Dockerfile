FROM openjdk:11-jdk-slim

RUN adduser jshell
RUN apt-get update && apt-get -y install gettext-base dumb-init

WORKDIR /opt/app
COPY target .
COPY etc/_example.bot.properties .
ARG token
ENV TOKEN=$token
RUN envsubst '${TOKEN}' < _example.bot.properties > bot.properties && rm _example.bot.properties && chmod 400 bot.properties
RUN chown -R jshell /opt/app && chmod +x JShellBot.jar

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["java", "-jar", "JShellBot.jar"]




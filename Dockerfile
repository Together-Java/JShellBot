FROM adoptopenjdk/openjdk11:latest
ARG token
ARG config_file=bot.properties.standard
ENV JSHELL_TOKEN=$token
# ENV JSHELL_BOT_CONFIG=/opt/app/$config_file
RUN mkdir /opt/app
WORKDIR /opt/app
COPY ./target/JShellBot.jar ./app.jar
COPY ./$config_file ./bot.properties

CMD ["java", "-jar", "app.jar"]
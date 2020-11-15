FROM openjdk:11-jdk-slim

RUN adduser jshell
RUN apt-get update && apt-get -y install curl gettext-base dumb-init

ARG maven_version=3.6.3
RUN mkdir -p /usr/share/maven && \
    curl -fsSL http://apache.osuosl.org/maven/maven-3/$maven_version/binaries/apache-maven-${maven_version}-bin.tar.gz | \
    tar -xzC /usr/share/maven --strip-components=1 && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
RUN mvn --version

WORKDIR /var/src
COPY src/ .
COPY pom.xml .
RUN mvn package && mkdir /var/app && cp -r target/* /var/app

WORKDIR /var/app
COPY etc/_example.bot.properties .
ARG token
ENV TOKEN=$token
RUN envsubst '${TOKEN}' < _example.bot.properties > bot.properties && rm _example.bot.properties && chmod 400 bot.properties
RUN chown -R jshell /var/app && chmod +x JShellBot.jar

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["java", "-jar", "/var/app/JShellBot.jar"]




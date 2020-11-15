# JShellBot
A Discord bot that allows you to interact with it as if it was a JShell session.

### Technologies used
* JDA (Java Discord API)
* JShell
* Some lightweight ASM for scanning the executed code

# Requirements
* Maven 3
* Java 11
* Docker (if you want to build as docker image)

## How to build
### IDE (i.e. IntelliJ)
Navigate to the `bot.properties` file and set your token there. You should be able to run the project
### Maven
```
mvn package
```
### Docker
You can build using the maven plugin. There is a `discord.token` you can pass to the maven build command:
```
mvn install -Ddiscord.token=yourtoken
```


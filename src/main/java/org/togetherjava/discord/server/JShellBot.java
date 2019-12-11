package org.togetherjava.discord.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JShellBot {

  private static final Logger LOGGER = LoggerFactory.getLogger(JShellBot.class);

  public static void main(String[] args) {
    JShellBot bot = new JShellBot();
    try {
      bot.start();
    } catch (Exception e) {
      LOGGER.error("Error starting the bot: ", e);
      System.exit(3);
    }
  }

  private void start() throws Exception {
    Config config = getConfig();

    if (config.getString("token") == null) {
      LOGGER.error("Token not set in config. Please add it under the `token` key.");
      System.exit(2);
    }

    JDA jda = new JDABuilder(AccountType.BOT)
        .setToken(config.getString("token"))
        .addEventListener(new CommandHandler(config))
        .build();
    jda.awaitReady();

    LOGGER.info("Goliath Online");
  }

  private Config getConfig() throws IOException {
    String botConfigPath = System.getenv("JSHELL_BOT_CONFIG");

    if (botConfigPath == null) {
      botConfigPath = "bot.properties";
    }

    Path path = Paths.get(botConfigPath);

    if (Files.notExists(path)) {
      LOGGER.error(
          "No config given. Please set the 'JSHELL_BOT_CONFIG' environment variable"
              + " or provide a 'bot.properties' file in the same directory as this jar file"
      );
      System.exit(1);
    } else {
      Config config = new Config(path);
      String token = System.getenv("JSHELL_TOKEN");
      if(token != null){
        //environment variable override for token i.e. building from container
        config.setString("token", token);
      }
      return config;
    }

    return new Config(path);
  }
}

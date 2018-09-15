package org.togetherjava.discord.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//todo convert to server daemen
public class JShellBot {

  private static final Logger LOGGER = LoggerFactory.getLogger(JShellBot.class);

  public static void main(String[] args) {
    JShellBot bot = new JShellBot();
    try {
      bot.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void start() throws Exception {
    Config config;
    String botConfigPathString = System.getenv("JSHELL_BOT_CONFIG");

    //if there is no path specified via env var then load from bot.properties in the resources path
    Path botConfigPath = botConfigPathString == null ? null
        : Paths.get(botConfigPathString);

    if (botConfigPath == null) {
      Properties prop = new Properties();
      prop.load(JShellBot.class.getResourceAsStream("/bot.properties"));
      config = new Config(prop);
    } else {
      config = new Config(botConfigPath);
    }

    if (config.getString("token") != null) {
      JDA jda = new JDABuilder(AccountType.BOT)
          .setToken(config.getString("token"))
          .addEventListener(new CommandHandler(config))
          .build();
      jda.awaitReady();

      LOGGER.info("Goliath Online");
    } else {
      LOGGER.error("Token not set or config file not found in '" + botConfigPathString + "'");
      System.exit(1);
    }
  }
}

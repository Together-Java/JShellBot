package org.togetherjava.discord.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.api.IDiscordClient;

import java.nio.file.Path;
import java.nio.file.Paths;

//todo convert to server daemen
public class JShellBot {
    static Logger log = LogManager.getLogger(JShellBot.class);

    public static void main(String[] args) {
        JShellBot bot = new JShellBot();
        try {
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @throws Exception
     */
    public void start() throws Exception {
        log.info("Goliath Online");

        String botConfigPathString = System.getenv("JSHELL_BOT_CONFIG");
        Path botConfigPath = botConfigPathString == null ? null : Paths.get(botConfigPathString);

        Config config = new Config(botConfigPath);

        IDiscordClient client = BotUtils.buildDiscordClient(config.getString("token"));
        client.getDispatcher().registerListener(new EventHandler(config));
        client.login();
    }
}

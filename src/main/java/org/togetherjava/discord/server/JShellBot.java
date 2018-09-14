package org.togetherjava.discord.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.System.exit;


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

    /**
     * @throws Exception
     */
    public void start() throws Exception {
        LOGGER.info("Goliath Online");
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
            IDiscordClient client = BotUtils.buildDiscordClient(config.getString("token"));
            client.getDispatcher().registerListener(new EventHandler(config));
            System.out.println(client);
            System.out.println(client.getApplicationName());
            client.login();
        } else {
            LOGGER.error("Token not set or config file not found in");
            exit(1);
        }
    }
}

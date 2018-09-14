package org.togetherjava.discord.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotUtils.class);

    public static IDiscordClient buildDiscordClient(String token) {
        return new ClientBuilder()
                .withToken(token)
                .build();
    }

    public static void sendMessage(IChannel channel, String message) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(message);
            } catch (DiscordException ex) {
                LOGGER.error("Message could not be sent with error: ", ex);
            }
        });
    }
}

package org.togetherjava.discord.server;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {
    public static IDiscordClient buildDiscordClient(String token){
        return new ClientBuilder()
                .withToken(token)
                .build();
    }

    public static void sendMessage(IChannel channel, String message){
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            }
            catch(DiscordException ex){
                JShellBot.log.error("Message could not be sent with error: ");
                JShellBot.log.trace(ex.getStackTrace());
            }
        });
    }
}

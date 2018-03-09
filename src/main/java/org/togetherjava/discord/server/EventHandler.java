package org.togetherjava.discord.server;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandler {
    private ConcurrentHashMap<String,JShell> sessions;

    public EventHandler(){
        sessions = new ConcurrentHashMap<>();
    }
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
        String message = event.getMessage().getContent();
        if(message.startsWith(BotUtils.BOT_PREFIX)){
            String command = message.substring((BotUtils.BOT_PREFIX + " ").length() - 1, message.length());
            String authorID = event.getAuthor().getStringID();
            JShell shell = sessions.get(authorID);
            if(shell == null){
                shell = JShell.create();
                sessions.put(authorID, shell);
            }

            List<SnippetEvent> results = shell.eval(command);
            for(SnippetEvent e : results){
                String errMsg = "";
                if(e.exception() != null) errMsg = " | " + e.exception().getMessage();
                BotUtils.sendMessage(event.getChannel(),
                "$" + e.snippet().id() + ": " + e.snippet().source() + " = " +
                            e.value() + errMsg);
            }
        }
    }
}

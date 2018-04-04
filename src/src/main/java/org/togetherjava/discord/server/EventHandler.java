package org.togetherjava.discord.server;

import jdk.jshell.SnippetEvent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventHandler {
    private static final Pattern CODE_BLOCK_EXTRACTOR_PATTERN = Pattern.compile("```(java)?\\s*([\\w\\W]+)```");

    private ConcurrentHashMap<String, JShellWrapper> sessions;
    private final String botPrefix;

    public EventHandler(Config config) {
        sessions = new ConcurrentHashMap<>();
        botPrefix = config.getString("prefix");
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContent();
        if (message.startsWith(botPrefix)) {
            String command = parseCommandFromMessage(message);
            String authorID = event.getAuthor().getStringID();
            JShellWrapper shell = sessions.get(authorID);
            if (shell == null) {
                shell = new JShellWrapper();
                sessions.put(authorID, shell);
            }

            executeCommand(event.getAuthor(), shell, command, event.getChannel());
        }
    }

    private String parseCommandFromMessage(String messageContent) {
        String withoutPrefix = messageContent.substring(botPrefix.length(), messageContent.length());

        Matcher codeBlockMatcher = CODE_BLOCK_EXTRACTOR_PATTERN.matcher(withoutPrefix);

        if (codeBlockMatcher.find()) {
            return codeBlockMatcher.group(2);
        }

        return withoutPrefix;
    }

    private void executeCommand(IUser user, JShellWrapper shell, String command, IChannel channel) {
        JShellWrapper.JShellResult results = shell.eval(command);

        for (SnippetEvent e : results.getEvents()) {
            String errMsg = "";
            if (e.exception() != null) {
                errMsg = " | " + e.exception().getClass().getSimpleName() + ": " + e.exception().getMessage();
            }

            sendSnippetResponse(user, command, channel, e.snippet().id(), e.value(), errMsg);
        }

        if (!results.getStdOut().trim().isEmpty()) {
            sendStandardOut(user, results.getStdOut(), channel);
        }
    }

    private void sendSnippetResponse(IUser user, String command, IChannel channel, String id, String value, String error) {
        MessageBuilder messageBuilder = new MessageBuilder(channel.getClient());
        messageBuilder.withChannel(channel);

        messageBuilder.appendContent(user.mention() + ": $" + id + ": ");

        if (command.contains("\n")) {
            messageBuilder.appendCode("java", command);
        } else {
            messageBuilder.appendContent(command, MessageBuilder.Styles.INLINE_CODE);
        }

        messageBuilder.appendContent(" = " + value + error);

        messageBuilder.send();
    }

    private void sendStandardOut(IUser user, String stdOut, IChannel channel) {
        String truncated = stdOut;

        if (truncated.length() > 1600) {
            truncated = truncated.substring(0, 1600);
        }

        new MessageBuilder(channel.getClient())
                .withChannel(channel)
                .appendContent(user.mention() + ": ")
                .appendContent("STD-OUT:\n", MessageBuilder.Styles.BOLD)
                .appendQuote(truncated)
                .send();
    }
}

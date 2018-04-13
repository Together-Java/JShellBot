package org.togetherjava.discord.server;

import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.apache.commons.lang3.StringUtils;
import org.togetherjava.discord.server.execution.JShellSessionManager;
import org.togetherjava.discord.server.execution.JShellWrapper;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

import java.awt.*;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventHandler {
    private static final Pattern CODE_BLOCK_EXTRACTOR_PATTERN = Pattern.compile("```(java)?\\s*([\\w\\W]+)```");
    private static final Color ERROR_COLOR = new Color(255, 99, 71);
    private static final Color SUCCESS_COLOR = new Color(118, 255, 0);

    private JShellSessionManager jShellSessionManager;
    private final String botPrefix;

    public EventHandler(Config config) {
        jShellSessionManager = new JShellSessionManager(Duration.ofMinutes(15), config);
        botPrefix = config.getString("prefix");
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContent();
        if (message.startsWith(botPrefix)) {
            String command = parseCommandFromMessage(message);
            String authorID = event.getAuthor().getStringID();
            JShellWrapper shell = jShellSessionManager.getSessionOrCreate(authorID);

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
        try {
            JShellWrapper.JShellResult results = shell.eval(command);
            sendEvalResponse(results, shell, user, channel);
        } catch (Throwable e) {
            sendErrorResponse(user, channel, e);
        }
    }

    private void sendEvalResponse(JShellWrapper.JShellResult result, JShellWrapper shell, IUser user, IChannel channel) {
        for (SnippetEvent snippetEvent : result.getEvents()) {
            MessageBuilder messageBuilder = new MessageBuilder(channel.getClient())
                    .withChannel(channel);

            EmbedBuilder snippetResponse = buildSnippetResponse(snippetEvent, shell, user);

            if (!result.getStdOut().isEmpty()) {
                snippetResponse.appendField(
                        "Output",
                        StringUtils.truncate(result.getStdOut(), EmbedBuilder.FIELD_CONTENT_LIMIT),
                        true
                );
            }

            messageBuilder.withEmbed(snippetResponse.build());

            messageBuilder.send();
        }
    }

    private EmbedBuilder buildSnippetResponse(SnippetEvent snippetEvent, JShellWrapper shell, IUser user) {
        Snippet snippet = snippetEvent.snippet();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withColor(SUCCESS_COLOR)
                .withTitle(user.getName() + "'s Result")
                .appendField("Snippet-ID", "$" + snippet.id(), true)
                .appendField(
                        "Value",
                        "`" + Objects.toString(snippetEvent.value()) + "`",
                        true
                );

        if (snippetEvent.value() == null) {
            shell.getSnippetDiagnostics(snippet).forEach(diag -> {
                embedBuilder.appendField(
                        "Error message",
                        StringUtils.truncate(diag.getMessage(Locale.ENGLISH), EmbedBuilder.FIELD_CONTENT_LIMIT),
                        true
                );
                embedBuilder.withColor(ERROR_COLOR);
            });
        }

        if (snippetEvent.exception() != null) {
            embedBuilder.appendField("Exception", snippetEvent.exception().getMessage(), true);
            embedBuilder.withColor(ERROR_COLOR);
        }

        return embedBuilder;
    }

    private void sendErrorResponse(IUser user, IChannel channel, Throwable exception) {
        MessageBuilder messageBuilder = new MessageBuilder(channel.getClient())
                .withChannel(channel);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withColor(ERROR_COLOR)
                .withTitle(user.getName() + "'s Result");

        embedBuilder.appendField("Type", exception.getClass().getSimpleName(), true);
        embedBuilder.appendField("Message", "`" + exception.getMessage() + "`", true);

        if (exception.getCause() != null) {
            embedBuilder.appendField("Cause type", exception.getCause().getClass().getSimpleName(), true);
            embedBuilder.appendField("Cause Message", "`" + exception.getCause().getMessage() + "`", true);
        }

        messageBuilder.withEmbed(embedBuilder.build());

        messageBuilder.send();

    }
}

package org.togetherjava.discord.server;

import jdk.jshell.Diag;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.togetherjava.discord.server.execution.JShellSessionManager;
import org.togetherjava.discord.server.execution.JShellWrapper;
import org.togetherjava.discord.server.io.input.InputSanitizerManager;
import org.togetherjava.discord.server.rendering.RendererManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventHandler {
    private static final Pattern CODE_BLOCK_EXTRACTOR_PATTERN = Pattern.compile("```(java)?\\s*([\\w\\W]+)```");

    private JShellSessionManager jShellSessionManager;
    private final String botPrefix;
    private RendererManager rendererManager;
    private InputSanitizerManager sanitizerManager;

    @SuppressWarnings("WeakerAccess")
    public EventHandler(Config config) {
        this.jShellSessionManager = new JShellSessionManager(config);
        this.botPrefix = config.getString("prefix");
        this.rendererManager = new RendererManager();
        this.sanitizerManager = new InputSanitizerManager();
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

        return sanitizerManager.sanitize(withoutPrefix);
    }

    private void executeCommand(IUser user, JShellWrapper shell, String command, IChannel channel) {
        MessageBuilder messageBuilder = buildCommonMessage(channel);
        EmbedBuilder embedBuilder;
        try {
            JShellWrapper.JShellResult results = shell.eval(command);

            SnippetEvent snippetEvent = results.getEvents().get(0);

            embedBuilder = buildCommonEmbed(user, snippetEvent.snippet());
            rendererManager.renderJShellResult(embedBuilder, results);

            for (Diag diag : (Iterable<Diag>) shell.getSnippetDiagnostics(snippetEvent.snippet())::iterator) {
                rendererManager.renderObject(embedBuilder, diag);
            }

        } catch (UnsupportedOperationException e) {
            embedBuilder = buildCommonEmbed(user, null);
            rendererManager.renderObject(embedBuilder, e);
            messageBuilder.withEmbed(embedBuilder.build());
        }
        messageBuilder.withEmbed(embedBuilder.build());
        messageBuilder.send();
    }

    private EmbedBuilder buildCommonEmbed(IUser user, Snippet snippet) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withTitle(user.getName() + "'s Result");

        if (snippet != null) {
            embedBuilder.appendField("Snippet-ID", "$" + snippet.id(), true);
        }

        return embedBuilder;
    }

    private MessageBuilder buildCommonMessage(IChannel channel) {
        return new MessageBuilder(channel.getClient())
                .withChannel(channel);
    }
}

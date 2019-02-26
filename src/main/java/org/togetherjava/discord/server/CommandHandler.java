package org.togetherjava.discord.server;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.jshell.Diag;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.togetherjava.discord.server.execution.AllottedTimeExceededException;
import org.togetherjava.discord.server.execution.JShellSessionManager;
import org.togetherjava.discord.server.execution.JShellWrapper;
import org.togetherjava.discord.server.execution.JShellWrapper.JShellResult;
import org.togetherjava.discord.server.io.input.InputSanitizerManager;
import org.togetherjava.discord.server.rendering.RendererManager;

public class CommandHandler extends ListenerAdapter {

  private static final Pattern CODE_BLOCK_EXTRACTOR_PATTERN = Pattern
      .compile("```(java)?\\s*([\\w\\W]+)```");

  private JShellSessionManager jShellSessionManager;
  private final String botPrefix;
  private RendererManager rendererManager;
  private boolean autoDeleteMessages;
  private Duration autoDeleteMessageDuration;
  private InputSanitizerManager inputSanitizerManager;
  private final int maxContextDisplayAmount;

  @SuppressWarnings("WeakerAccess")
  public CommandHandler(Config config) {
    this.jShellSessionManager = new JShellSessionManager(config);
    this.botPrefix = config.getString("prefix");
    this.rendererManager = new RendererManager();
    this.autoDeleteMessages = config.getBoolean("messages.auto_delete");
    this.autoDeleteMessageDuration = config.getDuration("messages.auto_delete.duration");
    this.inputSanitizerManager = new InputSanitizerManager();
    this.maxContextDisplayAmount = config.getInt("messages.max_context_display_amount");
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    String message = event.getMessage().getContentRaw();

    if (message.startsWith(botPrefix)) {
      String command = parseCommandFromMessage(message);
      String authorID = event.getAuthor().getId();

      JShellWrapper shell = jShellSessionManager.getSessionOrCreate(authorID);

      executeCommand(event.getAuthor(), shell, command, event.getTextChannel());
    }
  }

  private String parseCommandFromMessage(String messageContent) {
    String withoutPrefix = messageContent.substring(botPrefix.length());

    Matcher codeBlockMatcher = CODE_BLOCK_EXTRACTOR_PATTERN.matcher(withoutPrefix);

    if (codeBlockMatcher.find()) {
      return codeBlockMatcher.group(2);
    }

    return inputSanitizerManager.sanitize(withoutPrefix);
  }

  private void executeCommand(User user, JShellWrapper shell, String command,
      MessageChannel channel) {
    List<JShellResult> evalResults = shell.eval(command);

    List<JShellResult> toDisplay = evalResults.subList(
        Math.max(0, evalResults.size() - maxContextDisplayAmount),
        evalResults.size()
    );

    for (JShellResult result : toDisplay) {
      handleResult(user, result, shell, channel);
    }
  }

  private void handleResult(User user, JShellWrapper.JShellResult result, JShellWrapper shell,
      MessageChannel channel) {
    MessageBuilder messageBuilder = new MessageBuilder();
    EmbedBuilder embedBuilder;

    try {
      SnippetEvent snippetEvent = result.getEvents().get(0);

      embedBuilder = buildCommonEmbed(user, snippetEvent.snippet());

      rendererManager.renderJShellResult(embedBuilder, result);

      Iterable<Diag> diagonstics = shell.getSnippetDiagnostics(snippetEvent.snippet())::iterator;
      for (Diag diag : diagonstics) {
        rendererManager.renderObject(embedBuilder, diag);
      }

    } catch (UnsupportedOperationException | AllottedTimeExceededException e) {
      embedBuilder = buildCommonEmbed(user, null);
      rendererManager.renderObject(embedBuilder, e);
      messageBuilder.setEmbed(embedBuilder.build());
    }

    messageBuilder.setEmbed(embedBuilder.build());
    messageBuilder.sendTo(channel).submit().thenAccept(message -> {
      if (autoDeleteMessages) {
        message.delete().queueAfter(autoDeleteMessageDuration.toMillis(), TimeUnit.MILLISECONDS);
      }
    });
  }

  private EmbedBuilder buildCommonEmbed(User user, Snippet snippet) {
    EmbedBuilder embedBuilder = new EmbedBuilder()
        .setTitle(user.getName() + "'s Result");

    if (snippet != null) {
      embedBuilder.addField("Snippet-ID", "$" + snippet.id(), true);
    }

    return embedBuilder;
  }
}

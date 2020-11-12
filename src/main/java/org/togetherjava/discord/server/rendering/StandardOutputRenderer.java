package org.togetherjava.discord.server.rendering;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.togetherjava.discord.server.execution.JShellWrapper;

/**
 * A renderer for the standard output result.
 */
public class StandardOutputRenderer implements Renderer {

  @Override
  public boolean isApplicable(Object param) {
    return param instanceof JShellWrapper.JShellResult;
  }

  @Override
  public EmbedBuilder render(Object object, EmbedBuilder builder) {
    JShellWrapper.JShellResult result = (JShellWrapper.JShellResult) object;
    if (result.getStdOut().isEmpty()) {
      return builder;
    }
    String output;

    // Discord rejects all-whitespace fields so we need to guard them with a code block
    // Inline code swallows leading and trailing whitespaces, so it is sadly not up to the task
    if (result.getStdOut().chars().allMatch(Character::isWhitespace)) {
      final int fenceLength = "```\n```".length();
      String inner = RenderUtils
          .truncateAndSanitize(result.getStdOut(), MessageEmbed.VALUE_MAX_LENGTH - fenceLength);
      output = "```\n" + inner + "```";
    } else {
      output = RenderUtils.truncateAndSanitize(result.getStdOut(), MessageEmbed.VALUE_MAX_LENGTH);
    }

    return builder.addField("Output", output, true);
  }
}

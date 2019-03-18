package org.togetherjava.discord.server.rendering;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
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
    return builder
        .addField(
            "Output",
            RenderUtils.truncateAndSanitize(result.getStdOut(), MessageEmbed.VALUE_MAX_LENGTH),
            true
        );
  }
}

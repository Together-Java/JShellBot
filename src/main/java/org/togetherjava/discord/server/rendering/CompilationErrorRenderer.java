package org.togetherjava.discord.server.rendering;

import java.util.Locale;
import jdk.jshell.Diag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Renders error messages.
 */
public class CompilationErrorRenderer implements Renderer {

  @Override
  public boolean isApplicable(Object param) {
    return param instanceof Diag;
  }

  @Override
  public EmbedBuilder render(Object object, EmbedBuilder builder) {
    Diag diag = (Diag) object;
    return builder
        .addField(
            "Error message",
            RenderUtils
                .truncateAndSanitize(diag.getMessage(Locale.ROOT), MessageEmbed.VALUE_MAX_LENGTH),
            false
        );
  }
}

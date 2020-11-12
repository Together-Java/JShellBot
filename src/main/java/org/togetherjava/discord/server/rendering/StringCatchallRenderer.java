package org.togetherjava.discord.server.rendering;

import java.util.Objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * A renderer for results that just renders whatever hasn't been renderer yet as a string.
 */
public class StringCatchallRenderer implements Renderer {

  @Override
  public boolean isApplicable(Object param) {
    return !Objects.toString(param).isEmpty();
  }

  @Override
  public EmbedBuilder render(Object object, EmbedBuilder builder) {
    return builder.addField(
        "Result",
        RenderUtils.truncateAndSanitize(Objects.toString(object), MessageEmbed.VALUE_MAX_LENGTH),
        true
    );
  }
}

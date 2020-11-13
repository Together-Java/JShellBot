package org.togetherjava.discord.server.rendering;

import net.dv8tion.jda.api.EmbedBuilder;

/**
 * A renderer takes care of displaying some message in an embed.
 */
public interface Renderer {

  /**
   * Checks if this renderer can render the given object.
   *
   * @param param the object to check
   * @return true if this renderer can handle the passed object
   */
  boolean isApplicable(Object param);

  /**
   * Renders the given object to the {@link EmbedBuilder}.
   *
   * @param object the object to render
   * @param builder the {@link EmbedBuilder} to modify
   * @return the rendered {@link EmbedBuilder}
   */
  EmbedBuilder render(Object object, EmbedBuilder builder);
}

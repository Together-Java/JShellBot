package org.togetherjava.discord.server.rendering;


import java.awt.Color;
import jdk.jshell.Snippet;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Contains utility functions for rendering.
 */
class RenderUtils {

  static int NEWLINE_MAXIMUM = 10;

  private static final Color ERROR_COLOR = new Color(255, 99, 71);
  private static final Color SUCCESS_COLOR = new Color(118, 255, 0);
  private static final Color OVERWRITTEN_COLOR = SUCCESS_COLOR;
  private static final Color RECOVERABLE_COLOR = new Color(255, 181, 71);

  /**
   * Truncates the String to the max length and sanitizes it a bit.
   *
   * @param input the input string
   * @param maxLength the maximum length it can have
   * @return the processed string
   */
  static String truncateAndSanitize(String input, int maxLength) {
    StringBuilder result = new StringBuilder();

    int newLineCount = 0;
    for (int codePoint : input.codePoints().toArray()) {
      if (codePoint == '\n') {
        newLineCount++;
      }

      if (codePoint == '\n' && newLineCount > NEWLINE_MAXIMUM) {
        result.append("⏎");
      } else {
        result.append(Character.toChars(codePoint));
      }
    }

    return truncate(result.toString(), maxLength);
  }

  private static String truncate(String input, int maxLength) {
    if (input.length() <= maxLength) {
      return input;
    }
    return input.substring(0, maxLength);
  }

  /**
   * Applies the given color to the embed.
   *
   * @param status the status
   * @param builder the builder to apply it to
   */
  static void applyColor(Snippet.Status status, EmbedBuilder builder) {
    switch (status) {
      case VALID:
        builder.setColor(SUCCESS_COLOR);
        break;
      case OVERWRITTEN:
        builder.setColor(OVERWRITTEN_COLOR);
        break;
      case REJECTED:
      case DROPPED:
      case NONEXISTENT:
        builder.setColor(ERROR_COLOR);
        break;
      case RECOVERABLE_DEFINED:
      case RECOVERABLE_NOT_DEFINED:
        builder.setColor(RECOVERABLE_COLOR);
        break;
    }
  }
}

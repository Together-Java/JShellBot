package org.togetherjava.discord.server.io.input;

/**
 * Sanitizes input in some form to fix user errors.
 */
public interface InputSanitizer {

  /**
   * Sanitizes the input to JShell so that errors in it might be accounted for.
   *
   * @param input the input to sanitize
   * @return the resulting input
   */
  String sanitize(String input);
}

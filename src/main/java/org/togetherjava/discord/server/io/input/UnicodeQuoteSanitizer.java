package org.togetherjava.discord.server.io.input;

/**
 * An {@link InputSanitizer} that replaces unicode quotes (as inserted by word/phones) with regular
 * ones.
 */
public class UnicodeQuoteSanitizer implements InputSanitizer {

  @Override
  public String sanitize(String input) {
    return input
        .replace("“", "\"")
        .replace("”", "\"");
  }
}

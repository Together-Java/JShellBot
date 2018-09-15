package org.togetherjava.discord.server.io.input;

import java.util.ArrayList;
import java.util.List;

public class InputSanitizerManager {

  private List<InputSanitizer> sanitizers;

  public InputSanitizerManager() {
    this.sanitizers = new ArrayList<>();
    addDefaults();
  }

  private void addDefaults() {
    addSanitizer(new UnicodeQuoteSanitizer());
  }

  /**
   * Adds a new {@link InputSanitizer}
   *
   * @param sanitizer the sanitizer to add
   */
  public void addSanitizer(InputSanitizer sanitizer) {
    sanitizers.add(sanitizer);
  }

  /**
   * Sanitizes a given input using all registered {@link InputSanitizer}s.
   *
   * @param input the input to sanitize
   * @return the resulting input
   */
  public String sanitize(String input) {
    String result = input;
    for (InputSanitizer sanitizer : sanitizers) {
      result = sanitizer.sanitize(input);
    }
    return result;
  }
}

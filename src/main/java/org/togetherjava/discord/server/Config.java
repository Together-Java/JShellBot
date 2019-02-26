package org.togetherjava.discord.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Config {

  private Properties properties;

  /**
   * Reads the default config and populates this object with it
   *
   * @param configPath the path to the config to read. Null or a non-existent path to only use
   *     the defaults
   * @throws IOException if an error occurs reading the config
   */
  public Config(Path configPath) throws IOException {
    this(new Properties());

    if (configPath != null && Files.exists(configPath)) {
      loadFromStream(properties, Files.newInputStream(configPath, StandardOpenOption.READ));
    }
  }

  /**
   * Creates a config consisting of the given properties.
   *
   * @param properties the {@link Properties} to use
   */
  public Config(Properties properties) {
    this.properties = new Properties(properties);
  }

  private void loadFromStream(Properties properties, InputStream stream) throws IOException {
    // close the stream
    try (stream) {
      properties.load(stream);
    }
  }

  /**
   * Returns a property from the config as a String.
   *
   * @param key the key
   * @return the property or null if not found
   */
  public String getString(String key) {
    return properties.getProperty(key);
  }

  /**
   * Returns a property from the config as a String.
   *
   * @param key the key
   * @param defaultValue the default value to use when the key does not exist
   * @return the property or null if not found
   */
  public String getStringOrDefault(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  /**
   * Tries to parse an entry in ISO-8601 duration format.
   *
   * @param key the key to look up
   * @return the parsed duration or null if parsing was impossible.
   */
  public Duration getDuration(String key) {
    try {
      return Duration.parse(getString(key));
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  /**
   * Returns a property from the config splitting at {@code ","} and putting it in a list.
   *
   * @param key the key
   * @return the property or an empty list if not found
   */
  public List<String> getCommaSeparatedList(String key) {
    String value = getString(key);
    return value == null ? Collections.emptyList() : Arrays.asList(value.split(","));
  }

  /**
   * Tries to parse an entry as a boolean.
   *
   * @param key the key to look up
   * @return the boolean under the key or null if the key was not specified
   */
  public boolean getBoolean(String key) {
    String string = getString(key);

    if (string == null) {
      throw new RuntimeException("Expected a boolean in the config at path: '" + key + "'");
    }

    return Boolean.parseBoolean(string);
  }

  /**
   * Tries to parse an entry as an int.
   *
   * @param key the key to look up
   * @return the int under the key or null if the key was not specified
   */
  public int getInt(String key) {
    String string = getString(key);

    if (string == null) {
      throw new RuntimeException("Expected an int in the config at path: '" + key + "'");
    }

    return Integer.parseInt(string);
  }
}

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
     * @param configPath the path to the config to read. Null or a non-existent path to only use the defaults
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
        } catch (IOException e) {
            throw e;
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
}

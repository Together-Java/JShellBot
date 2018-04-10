package org.togetherjava.discord.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
        Properties defaults = new Properties();
        loadFromStream(defaults, getClass().getResourceAsStream("/bot.properties"));

        this.properties = new Properties(defaults);

        if (configPath != null && Files.exists(configPath)) {
            loadFromStream(properties, Files.newInputStream(configPath, StandardOpenOption.READ));
        }
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
}

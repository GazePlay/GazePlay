package net.gazeplay.commons.configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * The key/value pairs of GazePlay.properties.
 * <p>
 * Replaces the interface proxied by the Owner library (unmaintained since 2020), of which only these three operations
 * were used. Same behaviour, checked in the bytecode of Owner 1.0.12: the values are kept in a
 * {@link java.util.Properties}, a null value removes the key, and the file is written by {@link Properties#store}, so
 * the format of GazePlay.properties is unchanged.
 */
public class ApplicationConfig {

    private final Properties properties = new Properties();

    public ApplicationConfig(final Properties initialProperties) {
        properties.putAll(initialProperties);
    }

    public synchronized String getProperty(final String key) {
        return properties.getProperty(key);
    }

    public synchronized void setProperty(final String key, final String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }
    }

    public synchronized void store(final OutputStream out, final String comments) throws IOException {
        properties.store(out, comments);
    }
}

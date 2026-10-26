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

    /**
     * @return the value of a key, or null when the file has none
     */
    public synchronized String getProperty(final String key) {
        return properties.getProperty(key);
    }

    /**
     * Sets the value of a key, or removes the key when {@code value} is null. Nothing is written until {@link #store}.
     */
    public synchronized void setProperty(final String key, final String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }
    }

    /**
     * Writes all the pairs in the format of {@link Properties#store}, with {@code comments} as first line.
     */
    public synchronized void store(final OutputStream out, final String comments) throws IOException {
        properties.store(out, comments);
    }
}

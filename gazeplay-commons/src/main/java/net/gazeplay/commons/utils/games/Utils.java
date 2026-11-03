package net.gazeplay.commons.utils.games;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Enumeration;

/**
 * A few system facilities. The images of the user are in {@link GazePlayDirectories#getImagesDirectory()} (the "files"
 * folder of each profile, with its subfolders, was removed on 3/11/2026).
 * <p>
 * Created by schwab on 23/12/2016.
 */
@Slf4j
public class Utils {

    /**
     * Gets an InputStream to the provided resource.
     * @param resource Path to the resource
     * @return The InputStream of the resource
     */
    public static InputStream getInputStream(final String resource) {
        log.debug("Opening resource {}", resource);
        return ClassLoader.getSystemResourceAsStream(resource);
    }

    /**
     * @return the path with "/" instead of "\"
     */
    public static String convertWindowsPath(String path) {
        String windowsPath = path;
        windowsPath = windowsPath.replace("\\", "/");
        windowsPath = windowsPath.replaceAll("\\\\", "/");
        return windowsPath;
    }

    /**
     * @return true if the operating system is Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Logs all the system properties of the JVM (one line each), to help with the problems reported by users.
     */
    public static void logSystemProperties() {
        final Enumeration<?> propertyNames = System.getProperties().propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String element = (String) propertyNames.nextElement();
            log.info(String.format("%s: %s", element, System.getProperty(element)));
        }
    }

    /**
     *
     * @param tf textfield to limit the number of characters
     * @param maxLength the max number of characters allowed
     */
    public static void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (tf.getText().length() > maxLength) {
                    String s = tf.getText().substring(0, maxLength);
                    tf.setText(s);
                }
            }
        });
    }
}

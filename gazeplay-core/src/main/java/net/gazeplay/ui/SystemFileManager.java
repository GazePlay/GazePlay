package net.gazeplay.ui;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Opens the file manager of the system on the results, or a web page in its browser. It used to run
 * "cmd xdg-open,&lt;path&gt;" outside of Windows, which does not exist, so the buttons did nothing on Linux and macOS.
 */
@Slf4j
public final class SystemFileManager {

    private SystemFileManager() {
    }

    /**
     * Opens a folder.
     */
    public static void openFolder(final File folder) {
        start(openFolderCommand(System.getProperty("os.name"), folder));
    }

    /**
     * Shows a file, selected in its folder where the file manager can do it.
     */
    public static void showFile(final File file) {
        start(showFileCommand(System.getProperty("os.name"), file));
    }

    /**
     * @return the command opening a folder in the file manager of the system (Explorer, Finder, or xdg-open)
     */
    static List<String> openFolderCommand(final String osName, final File folder) {
        final String os = osName.toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return List.of("explorer.exe", folder.getAbsolutePath());
        } else if (os.contains("mac")) {
            return List.of("open", folder.getAbsolutePath());
        }
        return List.of("xdg-open", folder.getAbsolutePath());
    }

    /**
     * @return the command showing a file selected in the file manager of the system (Explorer, Finder, or its folder
     *   with xdg-open)
     */
    static List<String> showFileCommand(final String osName, final File file) {
        final String os = osName.toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return List.of("explorer.exe", "/select," + file.getAbsolutePath());
        } else if (os.contains("mac")) {
            return List.of("open", "-R", file.getAbsolutePath());
        }
        // xdg-open cannot select a file: its folder
        return List.of("xdg-open", file.getAbsoluteFile().getParent());
    }

    /**
     * Opens a web page in the default browser of the system.
     *
     * @return false when the command could not be run (the page is then surely not open); true does not prove that a
     *   browser showed it
     */
    public static boolean openWebPage(final String url) {
        return start(openWebPageCommand(System.getProperty("os.name"), url));
    }

    /**
     * @return the command opening a web page in the default browser of the system. On Windows, the handler of the
     *   links (url.dll) rather than "cmd /c start", which would read the characters of the address ("&amp;"...)
     */
    static List<String> openWebPageCommand(final String osName, final String url) {
        final String os = osName.toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return List.of("rundll32", "url.dll,FileProtocolHandler", url);
        } else if (os.contains("mac")) {
            return List.of("open", url);
        }
        return List.of("xdg-open", url);
    }

    /**
     * Runs a command without waiting for it; a failure is logged.
     *
     * @return whether the command could be run
     */
    private static boolean start(final List<String> command) {
        try {
            new ProcessBuilder(command).start();
            return true;
        } catch (IOException e) {
            log.warn("Cannot run {}", command, e);
            return false;
        }
    }
}

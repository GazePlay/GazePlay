package net.gazeplay.ui;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Opens the file manager of the system on the results. It used to run "cmd xdg-open,&lt;path&gt;" outside of Windows,
 * which does not exist, so the buttons did nothing on Linux and macOS.
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
     * Runs a command without waiting for it; a failure is logged.
     */
    private static void start(final List<String> command) {
        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            log.warn("Cannot run {}", command, e);
        }
    }
}

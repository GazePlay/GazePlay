package net.gazeplay.commons.utils.games;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Locations of the files of GazePlay: the GazePlay folder (Documents\GazePlay_Learning) and its subfolders, for the
 * default profile and for each user profile (profiles/&lt;user&gt;/...).
 */
public class GazePlayDirectories {

    /**
     * System property giving the GazePlay folder. Set by the launcher once the folder is found (read by the logging
     * configuration, given to the JVM of each game), and by the tests and the CI, so that they never use the folder
     * of the user: no folder is then looked for, nor moved.
     */
    public static final String GAZEPLAY_FOLDER_PROPERTY = "gazeplay.folder";

    /**
     * Name of the GazePlay folder in Documents (decision of the maintainer). Different from "Gazeplay", the folder of
     * Documents where old versions wrote their edition file, which is left as it is.
     */
    public static final String DOCUMENTS_FOLDER_NAME = "GazePlay_Learning";

    /**
     * System property replacing the Documents folder, for the tests of the move of the GazePlay folder only: the one
     * of Windows does not depend on user.home.
     */
    public static final String DOCUMENTS_FOLDER_PROPERTY = "gazeplay.documents";

    private static final Object LOCK = new Object();

    private static File resolvedGazePlayFolder;

    private static List<String> resolutionMessages = List.of();

    /**
     * @return Directory for GazePlay: a folder called GazePlay_Learning in the Documents folder of the user. It used to
     *   be ~/GazePlay: a folder found there is moved at the first call (see {@link GazePlayFolderMigration})
     */
    public static File getGazePlayFolder() {
        final String folder = System.getProperty(GAZEPLAY_FOLDER_PROPERTY);
        if (folder != null && !folder.isBlank()) {
            return new File(folder);
        }
        synchronized (LOCK) {
            if (resolvedGazePlayFolder == null) {
                final GazePlayFolderMigration.Result result =
                    GazePlayFolderMigration.resolve(getLegacyGazePlayFolder(), getDocumentsGazePlayFolder());
                resolvedGazePlayFolder = result.folder();
                resolutionMessages = List.copyOf(result.messages());
            }
            return resolvedGazePlayFolder;
        }
    }

    /**
     * Finds the GazePlay folder, moving it from its former location if needed, and gives it to the logging
     * configuration and to the JVMs of the games ({@link #GAZEPLAY_FOLDER_PROPERTY}). To be called before anything is
     * logged: the log file is in that folder.
     *
     * @return what was done to find the folder, to be logged
     */
    public static List<String> initGazePlayFolder() {
        final File folder = getGazePlayFolder();
        synchronized (LOCK) {
            System.setProperty(GAZEPLAY_FOLDER_PROPERTY, folder.getAbsolutePath());
            return resolutionMessages;
        }
    }

    /**
     * @return the former location of the GazePlay folder, in the home directory
     */
    public static File getLegacyGazePlayFolder() {
        return new File(System.getProperty("user.home"), "GazePlay");
    }

    /**
     * @return the GazePlay folder in the Documents folder of the user, or null if there is none
     */
    static File getDocumentsGazePlayFolder() {
        File documents = null;
        final String documentsForTests = System.getProperty(DOCUMENTS_FOLDER_PROPERTY);
        if (documentsForTests != null && !documentsForTests.isBlank()) {
            documents = new File(documentsForTests);
        } else if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            // The Documents folder known to Windows ("Mes documents"), also when it is moved, e.g. by OneDrive
            try {
                documents = FileSystemView.getFileSystemView().getDefaultDirectory();
            } catch (RuntimeException e) {
                documents = null;
            }
        }
        if (documents == null || !documents.isDirectory()) {
            documents = new File(System.getProperty("user.home"), "Documents");
        }
        return documents.isDirectory() ? new File(documents, DOCUMENTS_FOLDER_NAME) : null;
    }

    /**
     * Rewrites the paths of settings that were in the former location of the GazePlay folder, now moved.
     */
    public static void relocateMovedPaths(final Properties properties) {
        GazePlayFolderMigration.relocatePaths(properties, getLegacyGazePlayFolder(), getGazePlayFolder());
    }

    /**
     * @return the default files folder (FILE_DIR setting): the images and the shortcuts of the games
     */
    public static File getDefaultFileDirectoryDefaultValue() {
        return new File(getGazePlayFolder(), "files");
    }

    /**
     * @return the default folder of the shortcuts of the games created from the settings
     */
    public static File getShortcutDirectory() {
        return new File(GazePlayDirectories.getDefaultFileDirectoryDefaultValue(), "shortcut");
    }

    /**
     * @return the folder of the user profiles
     */
    public static File getProfilesDirectory() {
        return new File(getGazePlayFolder(), "profiles");
    }

    /**
     * @return the default files folder of a user profile
     */
    public static File getFileDirectoryUserValue(String user) {
        return new File(getUserProfileDirectory(user), "files");
    }

    /**
     * @return the folder of a user profile (its settings, statistics, files...)
     */
    public static File getUserProfileDirectory(String user) {
        return new File(getProfilesDirectory(), user);
    }

    /**
     * @return statistics directory for GazePlay : in the default directory of GazePlay, in a folder called statistics
     */

    public static File getStatsFolder() {
        return new File(getGazePlayFolder(), "statistics");
    }

    /**
     * @return data directory for GazePlay : in the default directory of GazePlay, in a folder called data
     */
    public static File getDataFolder() {
        return new File(getGazePlayFolder(), "data");
    }

    /**
     * @return the statistics folder of a user, or of the default profile when {@code user} is empty
     */
    public static File getUserStatsFolder(String user) {
        if (!user.equals("")) {
            return new File(getUserProfileDirectory(user), "statistics");
        } else {
            return getStatsFolder();
        }
    }

    /**
     * @return the data folder of a user, or of the default profile when {@code user} is empty
     */
    public static File getUserDataFolder(String user) {
        if (!user.equals("")) {
            return new File(getUserProfileDirectory(user), "data");
        } else {
            return getDataFolder();
        }
    }

}

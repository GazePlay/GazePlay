package net.gazeplay.commons.utils.games;

import java.io.File;

public class GazePlayDirectories {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");

    private static final String tempFolder = "temp";

    /**
     * @return Default directory for GazePlay : in user's home directory, in a folder called GazePlay
     */
    public static String getGazePlayFolder() {
        return System.getProperties().getProperty("user.home") + FILESEPARATOR + "GazePlay" + FILESEPARATOR;
    }

    public static String getFileDirectoryDefaultValue() {
        return getGazePlayFolder() + "files";
    }

    public static String getFileDirectoryUserValue(String user) {
        return getGazePlayFolder() + "profiles/" + user + FILESEPARATOR + "files";
    }

    public static File getUserProfileDirectory(String user) {
        return new File(new File(getGazePlayFolder(), "profiles"), user);
    }

    /**
     * @return Temp directory for GazePlay : in the default directory of GazePlay, a folder called Temp
     */
    public static String getTempFolder() {
        return getGazePlayFolder() + tempFolder + FILESEPARATOR;
    }

    /**
     * @return statistics directory for GazePlay : in the default directory of GazePlay, in a folder called statistics
     */

    public static String getStatsFolder() {
        return getGazePlayFolder() + "statistics" + FILESEPARATOR;
    }

    public static String getUserStatsFolder(String user) {
        if (!user.equals("")) {
            return getGazePlayFolder() + "profiles" + FILESEPARATOR + user + FILESEPARATOR + "statistics"
                + FILESEPARATOR;
        } else {
            return getStatsFolder();
        }
    }
}

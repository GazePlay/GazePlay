package net.gazeplay.commons.utils.games;

import java.io.File;

public class GazePlayDirectories {

    /**
     * @return Default directory for GazePlay : in user's home directory, in a folder called GazePlay
     */
    public static File getGazePlayFolder() {
        return new File(System.getProperties().getProperty("user.home"), "GazePlay");
    }

    public static File getDefaultFileDirectoryDefaultValue() {
        return new File(getGazePlayFolder(), "files");
    }

    public static File getVideosFilesDirectory() {
        return new File(GazePlayDirectories.getDefaultFileDirectoryDefaultValue(), "videos");
    }

    public static File getProfilesDirectory() {
        return new File(getGazePlayFolder(), "profiles");
    }

    public static File getFileDirectoryUserValue(String user) {
        return new File(getUserProfileDirectory(user), "files");
    }

    public static File getUserProfileDirectory(String user) {
        return new File(getProfilesDirectory(), user);
    }

    /**
     * @return Temp directory for GazePlay : in the default directory of GazePlay, a folder called Temp
     */
    public static File getTempFolder() {
        return new File(getGazePlayFolder(), "temp");
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

    public static File getUserStatsFolder(String user) {
        if (!user.equals("")) {
            return new File(getUserProfileDirectory(user), "statistics");
        } else {
            return getStatsFolder();
        }
    }

    public static File getUserDataFolder(String user) {
        if (!user.equals("")) {
            return new File(getUserProfileDirectory(user), "data");
        } else {
            return getDataFolder();
        }
    }

}

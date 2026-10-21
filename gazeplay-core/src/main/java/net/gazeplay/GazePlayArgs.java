package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

/**
 * Product edition: written by the launcher at startup, read by the user interface, including in the JVM launched for
 * each game.
 * <p>
 * Only the default edition, "gazeplay", remains: the AFSR and BERA (GazePlay Eval) editions were removed in October
 * 2026. A value they left in an edition file ("afsrGazeplay", "bera") is read as "gazeplay", so the BERA code kept in
 * the user interface and the Eval games (dormant, for a possible merge of the GazePlayLearning branch) cannot be
 * reached anymore.
 */
@Slf4j
public class GazePlayArgs {

    /**
     * System property overriding the location of the edition file. Used by the test suite so that it never reads
     * (or depends on) the edition file of the machine running the build.
     */
    public static final String ARGS_FILE_PROPERTY = "gazeplay.argsFile";

    static final String ARGS_FILE_NAME = "argsGazeplay.txt";

    static final String DEFAULT_EDITION = "gazeplay";

    /**
     * Editions that can still be used.
     */
    static final Set<String> SUPPORTED_EDITIONS = Set.of(DEFAULT_EDITION);

    /**
     * Location of the edition file: in the GazePlay folder of the user, like the other data of GazePlay.
     * <p>
     * It used to be the working directory on Linux and macOS (the edition was lost when GazePlay was launched from a
     * desktop shortcut, or could not be written when installed by root) and a hard-coded C:\Users\&lt;user.name&gt;
     * path on Windows; see {@link #getLegacyArgsFile()}.
     *
     * @return the file
     */
    public static File getArgsFile() {
        String overriddenPath = System.getProperty(ARGS_FILE_PROPERTY);
        if (overriddenPath != null && !overriddenPath.isBlank()) {
            return new File(overriddenPath);
        }
        return new File(GazePlayDirectories.getGazePlayFolder(), ARGS_FILE_NAME);
    }

    /**
     * Former location, still read when the new file does not exist: a JVM launched with
     * arguments (e.g. from a shortcut with --user) does not write the file.
     *
     * @return the file, or null if the OS is not recognized or the location is overridden
     */
    static File getLegacyArgsFile() {
        String overriddenPath = System.getProperty(ARGS_FILE_PROPERTY);
        if (overriddenPath != null && !overriddenPath.isBlank()) {
            return null;
        }
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nux") || os.contains("mac")) {
            return new File(ARGS_FILE_NAME);
        } else if (os.contains("win")) {
            String userName = System.getProperty("user.name");
            return new File("C:\\Users\\" + userName + "\\Documents\\Gazeplay\\" + ARGS_FILE_NAME);
        }
        return null;
    }

    /**
     * Edition to write at startup, from the command line of the launcher.
     * <ul>
     * <li>a launch with --game (JVM of a game started from the menu or by the replay, shortcut to a game) keeps the
     * edition written by the launch that started it: null;</li>
     * <li>any other launch is the default edition. It used to be written only without any argument, so a launch with
     * arguments (e.g. --user from a shortcut) kept the edition of the previous launch, possibly BERA with its two games
     * only.</li>
     * </ul>
     *
     * @return the edition to write, or null to keep the current file
     */
    static String editionToSave(final String[] args) {
        if (List.of(args).stream().anyMatch(arg -> arg.equals("--game") || arg.startsWith("--game="))) {
            return null;
        }
        return DEFAULT_EDITION;
    }

    public static String returnArgs() {
        return supportedEdition(readEdition(getArgsFile(), getLegacyArgsFile()));
    }

    /**
     * @return the edition if it can still be used, else the default edition (removed AFSR and BERA editions)
     */
    static String supportedEdition(final String edition) {
        if (SUPPORTED_EDITIONS.contains(edition)) {
            return edition;
        }
        if (!edition.isEmpty()) {
            log.info("Edition {} is no longer available: default edition", edition);
        }
        return DEFAULT_EDITION;
    }

    /**
     * @return the first line of the current file, else of the legacy one, else "" (default edition)
     */
    static String readEdition(final File argsFile, final File legacyArgsFile) {
        File file = argsFile;
        if (!file.isFile() && legacyArgsFile != null && legacyArgsFile.isFile()) {
            file = legacyArgsFile;
        }
        if (!file.isFile()) {
            log.warn("No edition file {}: default edition", argsFile);
            return "";
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            return lines.isEmpty() ? "" : lines.get(0);
        } catch (IOException e) {
            log.warn("Cannot read the edition file {}: default edition", file, e);
            return "";
        }
    }
}

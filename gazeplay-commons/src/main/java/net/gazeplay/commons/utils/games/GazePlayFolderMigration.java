package net.gazeplay.commons.utils.games;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Moves the GazePlay folder from its former location, ~/GazePlay, to Documents/GazePlay_Learning, where the users
 * look for their results (decision of the maintainer, October 2026).
 * <ul>
 * <li>no former folder: the folder in Documents is used (created if needed);</li>
 * <li>a former folder and none in Documents: it is moved (renamed, all its content at once);</li>
 * <li>both (e.g. the folder in Documents created by hand): the content of the former folder is moved into the one of
 * Documents. Only an edition file or log files can be in both: the ones already in Documents are
 * kept aside, renamed with {@link #SET_ASIDE_SUFFIX}. If a folder of data (profiles, statistics...) is in both, nothing
 * is moved: the former folder is used, and the two have to be merged by hand.</li>
 * </ul>
 * Nothing is deleted except the former folder once empty, nothing is overwritten. When a move fails (e.g. a file
 * locked by another program), what was moved is moved back and the former folder is used: the move is tried again at
 * the next start.
 * <p>
 * It runs before the logging starts, since the log file is in the GazePlay folder: it logs nothing, its messages are
 * returned to be logged by the launcher.
 */
final class GazePlayFolderMigration {

    static final String SET_ASIDE_SUFFIX = ".before-move";

    private static final String EDITION_FILE = "argsGazeplay.txt";

    private static final String LOG_FILE = "gazeplay.log";

    /**
     * @param folder   the GazePlay folder to use
     * @param messages what was done, to be logged
     */
    record Result(File folder, List<String> messages) {
    }

    private GazePlayFolderMigration() {
    }

    /**
     * @param legacy the former location (~/GazePlay)
     * @param target the new location (in Documents), or null if there is no Documents folder
     */
    static Result resolve(final File legacy, final File target) {
        final List<String> messages = new ArrayList<>();
        if (target == null) {
            messages.add("No Documents folder: the GazePlay folder stays in " + legacy);
            return new Result(legacy, messages);
        }
        if (isSameLocation(legacy, target)) {
            return new Result(target, messages);
        }
        if (!legacy.isDirectory()) {
            // New installation, or folder already moved
            if (!target.isDirectory() && !target.mkdirs()) {
                messages.add("Cannot create " + target + ": the GazePlay folder stays in " + legacy);
                return new Result(legacy, messages);
            }
            return new Result(target, messages);
        }
        if (!target.exists()) {
            final File parent = target.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                messages.add("Cannot create " + parent + ": the GazePlay folder stays in " + legacy);
                return new Result(legacy, messages);
            }
            try {
                Files.move(legacy.toPath(), target.toPath());
                messages.add("GazePlay folder moved from " + legacy + " to " + target);
                return new Result(target, messages);
            } catch (IOException | RuntimeException e) {
                messages.add("Cannot move " + legacy + " to " + target + " (" + e + "): the GazePlay folder stays in "
                    + legacy + ", the move will be tried again at the next start");
                return new Result(legacy, messages);
            }
        }
        if (!target.isDirectory()) {
            messages.add(target + " is not a folder: the GazePlay folder stays in " + legacy);
            return new Result(legacy, messages);
        }
        return merge(legacy, target, messages);
    }

    private static Result merge(final File legacy, final File target, final List<String> messages) {
        final File[] entries = legacy.listFiles();
        if (entries == null) {
            messages.add("Cannot list " + legacy + ": the GazePlay folder stays there");
            return new Result(legacy, messages);
        }

        // Checked before anything is moved
        final List<File> toSetAside = new ArrayList<>();
        final List<String> conflicts = new ArrayList<>();
        for (final File entry : entries) {
            final File destination = new File(target, entry.getName());
            if (destination.exists()) {
                if (isReplaceable(entry.getName()) && entry.isFile() && destination.isFile()) {
                    toSetAside.add(destination);
                } else {
                    conflicts.add(entry.getName());
                }
            }
        }
        if (!conflicts.isEmpty()) {
            messages.add(legacy + " and " + target + " both contain " + conflicts + ": nothing moved, the GazePlay "
                + "folder stays in " + legacy + " (the two folders have to be merged by hand)");
            return new Result(legacy, messages);
        }

        // Moves done, as {from, to}, undone in reverse order on failure
        final List<Path[]> moves = new ArrayList<>();
        try {
            for (final File file : toSetAside) {
                final Path setAside = freeName(file.toPath());
                Files.move(file.toPath(), setAside);
                moves.add(new Path[]{file.toPath(), setAside});
            }
            for (final File entry : entries) {
                final Path destination = new File(target, entry.getName()).toPath();
                Files.move(entry.toPath(), destination);
                moves.add(new Path[]{entry.toPath(), destination});
            }
        } catch (IOException | RuntimeException e) {
            messages.add("Cannot move the content of " + legacy + " to " + target + " (" + e + "): the GazePlay "
                + "folder stays in " + legacy + ", the move will be tried again at the next start");
            Collections.reverse(moves);
            for (final Path[] move : moves) {
                try {
                    Files.move(move[1], move[0]);
                } catch (IOException | RuntimeException undoError) {
                    messages.add("Cannot move " + move[1] + " back to " + move[0] + " (" + undoError + ")");
                }
            }
            return new Result(legacy, messages);
        }

        try {
            Files.delete(legacy.toPath());
        } catch (IOException | RuntimeException e) {
            messages.add("The former GazePlay folder " + legacy + " was not removed (" + e + ")");
        }
        messages.add("Content of the GazePlay folder moved from " + legacy + " to " + target
            + (toSetAside.isEmpty() ? "" : ", with " + toSetAside + " kept aside (" + SET_ASIDE_SUFFIX + ")"));
        return new Result(target, messages);
    }

    /**
     * Rewrites the paths of settings that are in the former folder, which has been moved (e.g. the files and videos
     * folders, saved as absolute paths).
     */
    static void relocatePaths(final Properties properties, final File legacy, final File current) {
        if (isSameLocation(legacy, current)) {
            return;
        }
        final String legacyPath = normalize(legacy.getAbsolutePath());
        for (final String key : properties.stringPropertyNames()) {
            final String value = properties.getProperty(key);
            final String normalized = normalize(value);
            if (normalized.regionMatches(isWindows(), 0, legacyPath, 0, legacyPath.length())
                && (normalized.length() == legacyPath.length()
                || normalized.charAt(legacyPath.length()) == File.separatorChar)) {
                properties.setProperty(key, current.getAbsolutePath() + normalized.substring(legacyPath.length()));
            }
        }
    }

    private static boolean isReplaceable(final String name) {
        return name.equals(EDITION_FILE) || name.startsWith(LOG_FILE);
    }

    private static Path freeName(final Path file) {
        Path candidate = file.resolveSibling(file.getFileName() + SET_ASIDE_SUFFIX);
        for (int i = 2; Files.exists(candidate); i++) {
            candidate = file.resolveSibling(file.getFileName() + SET_ASIDE_SUFFIX + i);
        }
        return candidate;
    }

    private static boolean isSameLocation(final File first, final File second) {
        final String firstPath = normalize(first.getAbsolutePath());
        final String secondPath = normalize(second.getAbsolutePath());
        return isWindows() ? firstPath.equalsIgnoreCase(secondPath) : firstPath.equals(secondPath);
    }

    private static String normalize(final String path) {
        String normalized = File.separatorChar == '\\' ? path.replace('/', '\\') : path;
        while (normalized.length() > 1 && normalized.endsWith(File.separator)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static boolean isWindows() {
        return File.separatorChar == '\\';
    }
}

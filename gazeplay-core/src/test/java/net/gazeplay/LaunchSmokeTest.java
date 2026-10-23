package net.gazeplay;

import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Launches the real application in a child JVM, without any window (Monocle) and with a temporary user.home, then
 * checks that it is still running after a while and that it logged no unexpected exception.
 * <p>
 * Run by the {@code smokeTest} Gradle task (excluded from {@code test}). It covers the assembly of the application
 * (command line, composition root, JavaFX start, game catalog) but not the rendering on a real screen, nor the eye
 * trackers.
 */
@Tag("smoke")
class LaunchSmokeTest {

    private static final long RUNNING_DELAY_SECONDS = 25;

    @ParameterizedTest
    @ValueSource(strings = {
        "--default-user",
        "--default-user --game WhereIsTheShape --variant DimensionDifficultyGameVariant:2:2:Easy"
    })
    void shouldStartAndKeepRunning(String arguments) throws IOException, InterruptedException {
        // Folder recreated by the smokeTest task at each run: a killed process can keep its files locked for a while
        // on Windows, which JUnit's @TempDir does not tolerate
        File home = new File(System.getProperty("smoke.home"), Integer.toHexString(arguments.hashCode()));
        assertTrue(home.mkdirs(), "Cannot create " + home);

        GazePlayProcess.Result result = GazePlayProcess.run(home, Arrays.asList(arguments.split(" ")),
            RUNNING_DELAY_SECONDS, false);

        assertFalse(result.exited(), "GazePlay stopped with exit code " + result.exitValue() + ":\n" + result.log());
        assertTrue(result.log().contains("Games found : "), "The game catalog was not loaded:\n" + result.log());
        List<String> unexpected = result.unexpectedErrors();
        assertTrue(unexpected.isEmpty(), "Unexpected errors:\n" + String.join("\n", unexpected));
    }

    /**
     * The GazePlay folder of a former version (~/GazePlay) is moved to Documents/GazePlay_Learning at the start, before
     * anything is logged: the log is written in the new folder only, and the former folder is gone. The Documents
     * folder is a test one (gazeplay.documents): the one of Windows does not depend on user.home.
     */
    @Test
    void shouldMoveTheFormerGazePlayFolderToDocuments() throws IOException, InterruptedException {
        File home = new File(System.getProperty("smoke.home"), "folder-move");
        File legacy = new File(home, "GazePlay");
        File results = new File(legacy, "statistics/WhereIsTheShape/2026-10-22");
        assertTrue(results.mkdirs(), "Cannot create " + results);
        Files.writeString(new File(results, "info-game.csv").toPath(), "results", StandardCharsets.UTF_8);
        File documents = new File(home, "Documents");
        assertTrue(documents.mkdirs(), "Cannot create " + documents);

        GazePlayProcess.Result result = GazePlayProcess.run(home, List.of("--default-user"), RUNNING_DELAY_SECONDS,
            true, List.of("-D" + GazePlayDirectories.DOCUMENTS_FOLDER_PROPERTY + "=" + documents.getAbsolutePath()));

        File target = new File(documents, GazePlayDirectories.DOCUMENTS_FOLDER_NAME);
        assertFalse(result.exited(), "GazePlay stopped with exit code " + result.exitValue() + ":\n" + result.log());
        assertTrue(result.log().contains("GazePlay folder moved from"), "No move:\n" + result.log());
        assertFalse(legacy.exists(), "The former folder is still there:\n" + result.log());
        assertEquals("results", Files.readString(
            new File(target, "statistics/WhereIsTheShape/2026-10-22/info-game.csv").toPath(), StandardCharsets.UTF_8));
        assertTrue(new File(target, "GazePlay.properties").isFile(), "Settings not moved");
        assertTrue(new File(target, "gazeplay.log").isFile(), "No log in the new folder");
        List<String> unexpected = result.unexpectedErrors();
        assertTrue(unexpected.isEmpty(), "Unexpected errors:\n" + String.join("\n", unexpected));
    }
}

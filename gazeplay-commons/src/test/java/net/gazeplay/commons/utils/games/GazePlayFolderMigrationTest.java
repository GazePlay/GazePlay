package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GazePlayFolderMigrationTest {

    @TempDir
    File dir;

    private File legacy() {
        return new File(new File(dir, "home"), "GazePlay");
    }

    private File target() {
        return new File(new File(new File(dir, "home"), "Documents"), "GazePlay_Learning");
    }

    private static void write(File file, String content) throws IOException {
        assertTrue(file.getParentFile().isDirectory() || file.getParentFile().mkdirs());
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    private static String read(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    @Test
    void shouldCreateTheFolderInDocumentsForANewInstallation() {
        GazePlayFolderMigration.Result result = GazePlayFolderMigration.resolve(legacy(), target());

        assertEquals(target(), result.folder());
        assertTrue(target().isDirectory());
        assertFalse(legacy().exists());
    }

    @Test
    void shouldMoveTheFormerFolderWithAllItsContent() throws IOException {
        write(new File(legacy(), "GazePlay.properties"), "LANGUAGE=fra");
        write(new File(legacy(), "statistics/WhereIsTheShape/2026-10-22/info-game.csv"), "results");

        GazePlayFolderMigration.Result result = GazePlayFolderMigration.resolve(legacy(), target());

        assertEquals(target(), result.folder());
        assertFalse(legacy().exists());
        assertEquals("LANGUAGE=fra", read(new File(target(), "GazePlay.properties")));
        assertEquals("results", read(new File(target(), "statistics/WhereIsTheShape/2026-10-22/info-game.csv")));
        assertEquals(1, result.messages().size());
    }

    @Test
    void shouldMoveTheContentIntoAnExistingFolderAndKeepItsFilesAside() throws IOException {
        write(new File(legacy(), "argsGazeplay.txt"), "gazeplay");
        write(new File(legacy(), "profiles/Alice/GazePlay.properties"), "profile");
        write(new File(target(), "argsGazeplay.txt"), "bera");
        write(new File(target(), "Emmanuel/notes.txt"), "kept");

        GazePlayFolderMigration.Result result = GazePlayFolderMigration.resolve(legacy(), target());

        assertEquals(target(), result.folder());
        assertFalse(legacy().exists());
        assertEquals("gazeplay", read(new File(target(), "argsGazeplay.txt")));
        assertEquals("bera", read(new File(target(), "argsGazeplay.txt" + GazePlayFolderMigration.SET_ASIDE_SUFFIX)));
        assertEquals("profile", read(new File(target(), "profiles/Alice/GazePlay.properties")));
        assertEquals("kept", read(new File(target(), "Emmanuel/notes.txt")));
    }

    @Test
    void shouldMoveNothingWhenBothFoldersHoldTheSameData() throws IOException {
        write(new File(legacy(), "statistics/a.csv"), "former");
        write(new File(legacy(), "music/song.mp3"), "music");
        write(new File(target(), "statistics/b.csv"), "new");

        GazePlayFolderMigration.Result result = GazePlayFolderMigration.resolve(legacy(), target());

        assertEquals(legacy(), result.folder());
        assertEquals("former", read(new File(legacy(), "statistics/a.csv")));
        assertEquals("music", read(new File(legacy(), "music/song.mp3")));
        assertEquals("new", read(new File(target(), "statistics/b.csv")));
        assertFalse(new File(target(), "music").exists());
        assertTrue(result.messages().get(0).contains("[statistics]"), result.messages().toString());
    }

    @Test
    void shouldKeepTheFormerFolderWithoutDocumentsFolder() throws IOException {
        write(new File(legacy(), "GazePlay.properties"), "LANGUAGE=fra");

        GazePlayFolderMigration.Result result = GazePlayFolderMigration.resolve(legacy(), null);

        assertEquals(legacy(), result.folder());
        assertTrue(new File(legacy(), "GazePlay.properties").isFile());
    }

    @Test
    void shouldUseTheFolderInDocumentsOnceMoved() throws IOException {
        write(new File(legacy(), "GazePlay.properties"), "LANGUAGE=fra");
        GazePlayFolderMigration.resolve(legacy(), target());

        GazePlayFolderMigration.Result result = GazePlayFolderMigration.resolve(legacy(), target());

        assertEquals(target(), result.folder());
        assertTrue(result.messages().isEmpty());
    }

    @Test
    void shouldRelocateThePathsSavedInTheFormerFolder() {
        Properties properties = new Properties();
        properties.setProperty("FILE_DIR", new File(legacy(), "files").getAbsolutePath());
        properties.setProperty("VIDEO_DIR", legacy().getAbsolutePath());
        properties.setProperty("MUSIC_FOLDER", new File(dir, "music").getAbsolutePath());
        properties.setProperty("OTHER", legacy().getAbsolutePath() + "2");
        properties.setProperty("LANGUAGE", "fra");

        GazePlayFolderMigration.relocatePaths(properties, legacy(), target());

        assertEquals(new File(target(), "files").getAbsolutePath(), properties.getProperty("FILE_DIR"));
        assertEquals(target().getAbsolutePath(), properties.getProperty("VIDEO_DIR"));
        assertEquals(new File(dir, "music").getAbsolutePath(), properties.getProperty("MUSIC_FOLDER"));
        assertEquals(legacy().getAbsolutePath() + "2", properties.getProperty("OTHER"));
        assertEquals("fra", properties.getProperty("LANGUAGE"));
    }

    @Test
    void shouldRelocateNothingWhenTheFolderDidNotMove() {
        Properties properties = new Properties();
        properties.setProperty("FILE_DIR", new File(legacy(), "files").getAbsolutePath());

        GazePlayFolderMigration.relocatePaths(properties, legacy(), legacy());

        assertEquals(new File(legacy(), "files").getAbsolutePath(), properties.getProperty("FILE_DIR"));
    }
}

package net.gazeplay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GazePlayArgsTest {

    private static File write(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void shouldStoreTheEditionInTheGazePlayFolderOfTheUser() {
        String overridden = System.clearProperty(GazePlayArgs.ARGS_FILE_PROPERTY);
        try {
            // user.home is a temporary folder during tests (see build.gradle)
            assertEquals(new File(new File(System.getProperty("user.home"), "GazePlay"), "argsGazeplay.txt"),
                GazePlayArgs.getArgsFile());
        } finally {
            System.setProperty(GazePlayArgs.ARGS_FILE_PROPERTY, overridden);
        }
    }

    @Test
    void shouldNotReadTheLegacyLocationWhenOverridden() {
        assertNull(GazePlayArgs.getLegacyArgsFile());
    }

    @Test
    void shouldReadTheFirstLineOfTheEditionFile(@TempDir File dir) throws IOException {
        File current = write(new File(dir, "current.txt"), "bera\n");

        assertEquals("bera", GazePlayArgs.readEdition(current, null));
    }

    @Test
    void shouldPreferTheCurrentFileToTheLegacyOne(@TempDir File dir) throws IOException {
        File current = write(new File(dir, "current.txt"), "gazeplay");
        File legacy = write(new File(dir, "legacy.txt"), "bera");

        assertEquals("gazeplay", GazePlayArgs.readEdition(current, legacy));
    }

    /**
     * After an upgrade, a JVM launched with arguments (e.g. a shortcut with --user) does not write the file: the
     * edition written by the former version must still be found.
     */
    @Test
    void shouldFallBackToTheLegacyFile(@TempDir File dir) throws IOException {
        File legacy = write(new File(dir, "legacy.txt"), "bera");

        assertEquals("bera", GazePlayArgs.readEdition(new File(dir, "missing.txt"), legacy));
    }

    @Test
    void shouldUseTheDefaultEditionWithoutFile(@TempDir File dir) throws IOException {
        assertEquals("", GazePlayArgs.readEdition(new File(dir, "missing.txt"), new File(dir, "missing-too.txt")));
        assertEquals("", GazePlayArgs.readEdition(write(new File(dir, "empty.txt"), ""), null));
    }

    @Test
    void shouldSaveTheDefaultEditionWithoutArgument() {
        assertEquals("gazeplay", GazePlayArgs.editionToSave(new String[]{}));
    }

    /**
     * The AFSR and BERA editions were removed: a value they left in an edition file gives the default edition, so the
     * dormant BERA code cannot be reached.
     */
    @Test
    void shouldReadTheRemovedEditionsAsTheDefaultOne() {
        assertEquals("gazeplay", GazePlayArgs.supportedEdition("gazeplay"));
        assertEquals("gazeplay", GazePlayArgs.supportedEdition("bera"));
        assertEquals("gazeplay", GazePlayArgs.supportedEdition("afsrGazeplay"));
        assertEquals("gazeplay", GazePlayArgs.supportedEdition(""));
    }

    /**
     * Regression test: a launch with arguments but without edition (e.g. a shortcut with --user) used to keep the
     * edition of the previous launch, possibly BERA and its two games only.
     */
    @Test
    void shouldSaveTheDefaultEditionForALaunchWithOtherArguments() {
        assertEquals("gazeplay", GazePlayArgs.editionToSave(new String[]{"--user", "Bob"}));
        assertEquals("gazeplay", GazePlayArgs.editionToSave(new String[]{"--default-user"}));
    }

    /**
     * The JVM of a game (started from the menu or by the replay) reads the edition written by the launch that started
     * it: it must not overwrite it.
     */
    @Test
    void shouldKeepTheEditionForTheJvmOfAGame() {
        assertNull(GazePlayArgs.editionToSave(new String[]{"--user", "Bob", "--game", "Ninja", "--variant", "x"}));
        assertNull(GazePlayArgs.editionToSave(new String[]{"--default-user", "--game=Ninja"}));
    }
}

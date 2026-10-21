package net.gazeplay.gameslocator;

import net.gazeplay.GazePlayArgs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The catalog of the games is used whatever the edition file says: the BERA edition, which listed the evaluations
 * (evals-catalog.txt, kept with the dormant Eval code), and the AFSR edition were removed in October 2026.
 */
class CatalogBasedGamesLocatorTest {

    @TempDir
    File dir;

    private String previousArgsFile;

    @BeforeEach
    void saveArgsFile() {
        previousArgsFile = System.getProperty(GazePlayArgs.ARGS_FILE_PROPERTY);
    }

    @AfterEach
    void restoreArgsFile() {
        if (previousArgsFile == null) {
            System.clearProperty(GazePlayArgs.ARGS_FILE_PROPERTY);
        } else {
            System.setProperty(GazePlayArgs.ARGS_FILE_PROPERTY, previousArgsFile);
        }
    }

    private int catalogSizeFor(String edition) throws IOException {
        File argsFile = new File(dir, edition + ".txt");
        Files.writeString(argsFile.toPath(), edition, StandardCharsets.UTF_8);
        System.setProperty(GazePlayArgs.ARGS_FILE_PROPERTY, argsFile.getAbsolutePath());
        return new CatalogBasedGamesLocator().findGameSpecSourceClasses().size();
    }

    @Test
    void shouldListTheGamesWhateverTheEditionFileSays() throws IOException {
        int games = catalogSizeFor("gazeplay");

        assertTrue(games > 2);
        assertEquals(games, catalogSizeFor("bera"));
        assertEquals(games, catalogSizeFor("afsrGazeplay"));
    }
}

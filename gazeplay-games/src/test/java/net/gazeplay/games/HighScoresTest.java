package net.gazeplay.games;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighScoresTest {

    @TempDir
    File dir;

    /**
     * Regression test: the folder of the file (e.g. statistics/<profile>/space-game) did not exist before statistics
     * were saved for the game, and the score could not be saved.
     */
    @Test
    void shouldCreateTheFolderAndTheFile() throws IOException {
        File file = new File(new File(dir, "space-game"), "highscore.dat");

        assertEquals(120, HighScores.addAndGetBest(file, 120));
        assertEquals("120:", Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    /**
     * Persistent format, unchanged: the 3 best scores in ascending order, each followed by ':'.
     */
    @Test
    void shouldKeepTheThreeBestScoresInAscendingOrder() throws IOException {
        File file = new File(dir, "highscores.dat");
        Files.writeString(file.toPath(), "100:300:500:", StandardCharsets.UTF_8);

        assertEquals(500, HighScores.addAndGetBest(file, 200));
        assertEquals("200:300:500:", Files.readString(file.toPath(), StandardCharsets.UTF_8));
        assertEquals(700, HighScores.addAndGetBest(file, 700));
        assertEquals("300:500:700:", Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }
}

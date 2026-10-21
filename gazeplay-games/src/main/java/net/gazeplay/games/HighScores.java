package net.gazeplay.games;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * High scores of a game, kept in a file of the statistics folder of the profile: the 3 best scores in ascending order,
 * each followed by ':' (e.g. "120:340:560:"). Shared by Biboule Jump and Space Game, which each had a copy of it.
 */
@Slf4j
public final class HighScores {

    static final int KEPT = 3;

    private HighScores() {
    }

    /**
     * Adds a score to the file and returns the best score.
     * <p>
     * The folder of the file is created if needed: it used to exist only once statistics had been saved for the game,
     * and the score could not be saved before. The file used to stay open after its reading.
     *
     * @return the best score, the given score itself if the file cannot be read or written
     */
    public static int addAndGetBest(final File file, final int score) {
        final File folder = file.getParentFile();
        if (folder != null && !folder.isDirectory() && !folder.mkdirs()) {
            log.warn("Cannot create the folder of the high scores {}", folder);
        }
        try {
            List<Integer> highscores = new ArrayList<>();
            if (!file.createNewFile()) {
                try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
                    scanner.useDelimiter(":");
                    while (scanner.hasNextInt()) {
                        highscores.add(scanner.nextInt());
                    }
                }
            }
            highscores.add(score);

            Collections.sort(highscores);
            if (highscores.size() > KEPT) {
                highscores = new ArrayList<>(highscores.subList(highscores.size() - KEPT, highscores.size()));
            }

            try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                for (final int highscore : highscores) {
                    writer.write(highscore + ":");
                }
            }

            return highscores.get(highscores.size() - 1);
        } catch (final IOException ex) {
            log.warn("Cannot read or write the high scores {}", file, ex);
        }
        return score;
    }
}

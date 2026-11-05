package net.gazeplay;

import net.gazeplay.cli.GameSelectionOptions;
import net.gazeplay.cli.ReusableOptions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Esc no longer leaves the full screen in the JVM of a game, where it opens the menu. The JVM of the menus keeps it: the
 * pages of the mode and of the profiles have no full screen button, and the options always hold a
 * {@link GameSelectionOptions}, even without a game.
 */
class GazePlayFxAppGameJvmTest {

    private static ReusableOptions parse(String... arguments) {
        ReusableOptions options = new ReusableOptions();
        new CommandLine(options).parseArgs(arguments);
        return options;
    }

    @Test
    void shouldRecognizeTheJvmOfAGame() {
        assertTrue(GazePlayFxApp.startsAGame(parse("--game", "Biboule")));
        assertTrue(GazePlayFxApp.startsAGame(parse("--random-game")));
    }

    @Test
    void shouldRecognizeTheJvmOfTheMenus() {
        assertFalse(GazePlayFxApp.startsAGame(null));
        assertFalse(GazePlayFxApp.startsAGame(parse()));
        assertFalse(GazePlayFxApp.startsAGame(parse("--default-user")));
    }

}

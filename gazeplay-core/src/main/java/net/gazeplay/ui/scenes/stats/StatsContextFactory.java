package net.gazeplay.ui.scenes.stats;

import javafx.scene.layout.BorderPane;
import lombok.NonNull;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Creates the statistics screen of a game. It has no button going on with the game any more (maintainer's request of
 * 16/11/2026, done on 17/11/2026): one goes back to the menu, and launches the game again from it.
 */
public class StatsContextFactory {

    /**
     * @return the statistics screen
     */
    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats
    ) {
        return new StatsContext(gazePlay, new BorderPane(), stats);
    }
}

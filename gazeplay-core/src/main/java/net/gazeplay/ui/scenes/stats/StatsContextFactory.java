package net.gazeplay.ui.scenes.stats;

import javafx.scene.layout.BorderPane;
import lombok.NonNull;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Creates the statistics screen of a game.
 */
public class StatsContextFactory {

    /**
     * @return the statistics screen, without the button going on with the game
     */
    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats
    ) {
        return StatsContextFactory.newInstance(gazePlay, stats, null);
    }

    /**
     * @return the statistics screen, with a button going on with the game (null for none)
     */
    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats,
        CustomButton continueButton
    ) {
        BorderPane root = new BorderPane();
        return new StatsContext(gazePlay, root, stats, continueButton);
    }
}

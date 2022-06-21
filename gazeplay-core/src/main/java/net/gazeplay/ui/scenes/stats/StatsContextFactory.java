package net.gazeplay.ui.scenes.stats;

import javafx.scene.layout.BorderPane;
import lombok.NonNull;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.stats.Stats;

public class StatsContextFactory {

    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats
    ) {
        return StatsContextFactory.newInstance(gazePlay, stats, null, false);
    }

    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats,
        CustomButton continueButton
    ) {
        BorderPane root = new BorderPane();
        return new StatsContext(gazePlay, root, stats, continueButton, false);
    }

    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats,
        boolean inReplayMode
    ) {
        return StatsContextFactory.newInstance(gazePlay, stats, null, inReplayMode);
    }

    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats,
        CustomButton continueButton,
        boolean inReplayMode
    ) {
        BorderPane root = new BorderPane();
        return new StatsContext(gazePlay, root, stats, continueButton, inReplayMode);
    }
}

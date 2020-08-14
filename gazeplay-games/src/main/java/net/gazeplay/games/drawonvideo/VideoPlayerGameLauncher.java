package net.gazeplay.games.drawonvideo;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.stats.ExplorationGamesStats;

import java.util.LinkedList;

public class VideoPlayerGameLauncher implements GameSpec.GameLauncher<ExplorationGamesStats, GameSpec.StringGameVariant> {
    @Override
    public ExplorationGamesStats createNewStats(Scene scene) {
        return new ExplorationGamesStats(scene, "Video Player with Feedback");
    }

    @Override
    public ExplorationGamesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new ExplorationGamesStats(scene, "Video Player with Feedback", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.StringGameVariant gameVariant,
                                       ExplorationGamesStats stats) {
        return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.StringGameVariant gameVariant,
                                       ExplorationGamesStats stats, double gameSeed) {
        return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue(), gameSeed);
    }
}

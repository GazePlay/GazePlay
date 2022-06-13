package net.gazeplay.games.drawonvideo;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.StringGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ExplorationGamesStats;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoPlayerGameLauncher implements IGameLauncher<ExplorationGamesStats, StringGameVariant> {
    @Override
    public ExplorationGamesStats createNewStats(Scene scene) {
        return new ExplorationGamesStats(scene, "Video Player with Feedback");
    }

    @Override
    public ExplorationGamesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        return new ExplorationGamesStats(scene, "Video Player with Feedback", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, StringGameVariant gameVariant,
                                       ExplorationGamesStats stats) {
        return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, StringGameVariant gameVariant,
                                    ExplorationGamesStats stats, double gameSeed) {
        return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue(), gameSeed);
    }
}

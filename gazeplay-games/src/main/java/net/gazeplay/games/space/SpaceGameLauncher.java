package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class SpaceGameLauncher implements IGameLauncher<SpaceGameStats, IGameVariant> {

    @Override
    public SpaceGameStats createNewStats(Scene scene) {
        return new SpaceGameStats(scene);
    }

    @Override
    public SpaceGameStats createSavedStats(Scene scene,
                                           int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                           LifeCycle lifeCycle,
                                           RoundsDurationReport roundsDurationReport,
                                           List<List<FixationPoint>> fixationSequence,
                                           List<CoordinatesTracker> movementHistory,
                                           int[][] heatMap,
                                           List<AreaOfInterest> aoiList,
                                           SavedStatsInfo savedStatsInfo
    ) {
        return new SpaceGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, SpaceGameStats stats) {
        return new SpaceGame(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, SpaceGameStats stats, double gameSeed) {
        return new SpaceGame(gameContext, stats, gameSeed);
    }
}

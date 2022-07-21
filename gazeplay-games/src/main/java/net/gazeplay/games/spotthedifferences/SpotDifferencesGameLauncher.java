package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class SpotDifferencesGameLauncher implements IGameLauncher<SpotTheDifferencesStats, IGameVariant> {

    @Override
    public SpotTheDifferencesStats createNewStats(Scene scene) {
        return new SpotTheDifferencesStats(scene);
    }

    @Override
    public SpotTheDifferencesStats createSavedStats(Scene scene,
                                                    int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                                    LifeCycle lifeCycle,
                                                    RoundsDurationReport roundsDurationReport,
                                                    List<List<FixationPoint>> fixationSequence,
                                                    List<CoordinatesTracker> movementHistory,
                                                    int[][] heatMap,
                                                    List<AreaOfInterest> aoiList,
                                                    SavedStatsInfo savedStatsInfo
    ) {
        return new SpotTheDifferencesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, SpotTheDifferencesStats stats) {
        return new SpotTheDifferences(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, SpotTheDifferencesStats stats, double gameSeed) {
        return new SpotTheDifferences(gameContext, stats);
    }
}

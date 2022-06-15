package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class BottleGameLauncher implements IGameLauncher<BottleGameStats, IntStringGameVariant> {

    @Override
    public BottleGameStats createNewStats(Scene scene) {
        return new BottleGameStats(scene);
    }

    @Override
    public BottleGame createNewGame(IGameContext gameContext, IntStringGameVariant gameVariant, BottleGameStats stats) {
        return new BottleGame(gameContext, stats, gameVariant.getNumber(), gameVariant.getStringValue());
    }

    @Override
    public BottleGameStats createSavedStats(Scene scene,
                                            int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                            LifeCycle lifeCycle,
                                            RoundsDurationReport roundsDurationReport,
                                            List<List<FixationPoint>> fixationSequence,
                                            List<CoordinatesTracker> movementHistory,
                                            double[][] heatMap,
                                            List<AreaOfInterest> AOIList,
                                            SavedStatsInfo savedStatsInfo
    ) {
        return new BottleGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public BottleGame replayGame(IGameContext gameContext, IntStringGameVariant gameVariant, BottleGameStats stats, double gameSeed) {
        return new BottleGame(gameContext, stats, gameVariant.getNumber(), gameVariant.getStringValue(), gameSeed);
    }
}

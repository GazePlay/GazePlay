package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class GooseGameLauncher implements IGameLauncher<GooseGameStats, IntGameVariant> {

    @Override
    public GooseGameStats createNewStats(Scene scene) {
        return new GooseGameStats(scene, "goosegame");
    }

    @Override
    public GooseGameStats createSavedStats(Scene scene,
                                           int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                           LifeCycle lifeCycle,
                                           RoundsDurationReport roundsDurationReport,
                                           List<List<FixationPoint>> fixationSequence,
                                           List<CoordinatesTracker> movementHistory,
                                           int[][] heatMap,
                                           List<AreaOfInterest> aoiList,
                                           SavedStatsInfo savedStatsInfo
    ) {
        return new GooseGameStats(scene, "goosegame", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant, GooseGameStats stats) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntGameVariant gameVariant, GooseGameStats stats, double gameSeed) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber(), gameSeed);
    }
}

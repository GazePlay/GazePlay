package net.gazeplay.games.literacy;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class LettersGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new LettersGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  double[][] heatMap,
                                  List<AreaOfInterest> AOIList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new LettersGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, DimensionGameVariant gameVariant, Stats stats) {
        return new Letters(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, DimensionGameVariant gameVariant, Stats stats, double gameSeed) {
        return new Letters(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats, gameSeed);
    }
}

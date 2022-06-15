package net.gazeplay.games.horses;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class HorsesGameLauncher implements IGameLauncher<Stats, IntGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "horses");
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
        return new Stats(scene, "horses", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant, Stats stats) {
        return new Horses(gameContext, stats, 0, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntGameVariant gameVariant, Stats stats, double gameSeed) {
        return new Horses(gameContext, stats, 0, gameVariant.getNumber(), gameSeed);
    }

}

package net.gazeplay.games.creampie;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class CreampieGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new CreampieStats(scene);
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
        return new CreampieStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new CreamPie(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new CreamPie(gameContext, stats, gameSeed);
    }
}

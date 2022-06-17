package net.gazeplay.games.cakes;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class CakesGameLauncher implements IGameLauncher<Stats, EnumGameVariant<CakeGameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new CakeStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  double[][] heatMap,
                                  List<AreaOfInterest> aoiList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new CakeStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<CakeGameVariant> gameVariant, Stats stats) {
        return new CakeFactory(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<CakeGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new CakeFactory(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }
}

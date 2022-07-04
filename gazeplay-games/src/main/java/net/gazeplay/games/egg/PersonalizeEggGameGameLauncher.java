package net.gazeplay.games.egg;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class PersonalizeEggGameGameLauncher implements IGameLauncher<Stats, IntGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new EggGameStats(scene, "personalize");
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  int[][] heatMap,
                                  List<AreaOfInterest> aoiList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new EggGameStats(scene, "personalize", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant, Stats stats) {
        return new EggGame(gameContext, stats, gameVariant.getNumber(), "personalize");
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntGameVariant gameVariant, Stats stats, double gameSeed) {
        return new EggGame(gameContext, stats, gameVariant.getNumber(), "personalize", gameSeed);
    }
}

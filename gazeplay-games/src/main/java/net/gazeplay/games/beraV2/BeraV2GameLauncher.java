package net.gazeplay.games.beraV2;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class BeraV2GameLauncher implements IGameLauncher<Stats, EnumGameVariant<BeraV2GameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new BeraV2GameStats(scene);
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
        return new BeraV2GameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<BeraV2GameVariant> gameVariant, Stats stats) {
        return new BeraV2(false, gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<BeraV2GameVariant> gameVariant, Stats stats, double gameSeed) {
        return new BeraV2(false, gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }
}

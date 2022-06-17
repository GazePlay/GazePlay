package net.gazeplay.games.follow;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class FollowGameLauncher implements IGameLauncher<Stats, EnumGameVariant<FollowGameVariant>> {

    private FollowGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {
        gameStat = new FollowGamesStats(scene);
        return gameStat;
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
        gameStat = new FollowGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<FollowGameVariant> gameVariant, Stats stats) {
        return new Follow(gameContext, gameStat, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<FollowGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new Follow(gameContext, gameStat, gameVariant.getEnumValue());
    }
}

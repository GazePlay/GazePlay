package net.gazeplay.games.follow;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class FollowGameLauncher implements IGameLauncher<Stats, EnumGameVariant<FollowGameVariant>> {

    private FollowGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {
        gameStat = new FollowGamesStats(scene);
        return gameStat;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        gameStat = new FollowGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<FollowGameVariant> gameVariant,
                                       Stats stats) {
        return new Follow(gameContext, gameStat, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<FollowGameVariant> gameVariant,
                                    Stats stats, double gameSeed) {
        return new Follow(gameContext, gameStat, gameVariant.getEnumValue());
    }
}

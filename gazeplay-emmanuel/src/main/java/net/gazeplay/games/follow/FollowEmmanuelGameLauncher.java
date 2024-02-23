package net.gazeplay.games.follow;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class FollowEmmanuelGameLauncher implements IGameLauncher<Stats, EnumGameVariant<FollowEmmanuelGameVariant>> {

    private FollowEmmanuelGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {
        gameStat = new FollowEmmanuelGamesStats(scene);
        return gameStat;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        gameStat = new FollowEmmanuelGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<FollowEmmanuelGameVariant> gameVariant,
                                       Stats stats) {
        return new FollowEmmanuel(gameContext, gameStat, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<FollowEmmanuelGameVariant> gameVariant,
                                    Stats stats, double gameSeed) {
        return new FollowEmmanuel(gameContext, gameStat, gameVariant.getEnumValue());
    }
}

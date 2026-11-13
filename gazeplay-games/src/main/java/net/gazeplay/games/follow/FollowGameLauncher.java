package net.gazeplay.games.follow;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Suivre le chemin" (name code Follow) and its statistics (folder "Follow", as before), for a new game or for the
 * replay of a saved one (see {@code IGameLauncher}). Without a variant of the review (the former ones), the coins, at
 * the normal level, for one map.
 */
public class FollowGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new FollowGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new FollowGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Follow create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                         final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new Follow(gameContext, stats, FollowMode.of(grouped.getGroup()), FollowLevel.of(grouped.getValue()),
                FollowEnd.of(grouped.getSubGroup()), gameSeed);
        }
        return new Follow(gameContext, stats, FollowMode.COINS, FollowLevel.NORMAL, FollowEnd.MAPS_1, gameSeed);
    }
}

package net.gazeplay.games.moles;

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
 * Creates the moles game and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant (or with one of before the review of 4/11/2026): the moles, normal level, hit
 * at once, without end.
 */
public class WhacAMoleGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new MoleStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new MoleStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return game(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return game(gameContext, gameVariant, stats, gameSeed);
    }

    private static Moles game(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                              final double gameSeed) {
        return new Moles(gameContext, stats, targetOf(gameVariant), levelOf(gameVariant), hitOf(gameVariant),
            endOf(gameVariant), gameSeed);
    }

    static MolesTarget targetOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? MolesTarget.of(grouped.getGroup())
            : MolesTarget.MOLES;
    }

    static MolesLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? MolesLevel.of(grouped.getSubGroup())
            : MolesLevel.NORMAL;
    }

    static MolesEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? MolesEnd.of(grouped.getValue()) : MolesEnd.ENDLESS;
    }

    static MolesHit hitOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped && !grouped.getExtras().isEmpty()
            ? MolesHit.of(grouped.getExtras().get(0)) : MolesHit.AT_ONCE;
    }
}

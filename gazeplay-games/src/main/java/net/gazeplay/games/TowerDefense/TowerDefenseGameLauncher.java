package net.gazeplay.games.TowerDefense;

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
 * Creates the game "TowerDefense" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}; the saved statistics used to be missing, so a replay could not start). Without a variant (or
 * with a map of before the review of 3/11/2026): waves on demand, normal level, without end.
 */
public class TowerDefenseGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new TowerDefenseStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new TowerDefense(gameContext, stats, wavesOf(gameVariant), levelOf(gameVariant), endOf(gameVariant),
            -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new TowerDefense(gameContext, stats, wavesOf(gameVariant), levelOf(gameVariant), endOf(gameVariant),
            gameSeed);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new TowerDefenseStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    static TowerDefenseWaves wavesOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? TowerDefenseWaves.of(grouped.getGroup())
            : TowerDefenseWaves.ON_DEMAND;
    }

    static TowerDefenseLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? TowerDefenseLevel.of(grouped.getSubGroup())
            : TowerDefenseLevel.NORMAL;
    }

    static TowerDefenseEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? TowerDefenseEnd.of(grouped.getValue())
            : TowerDefenseEnd.ENDLESS;
    }
}

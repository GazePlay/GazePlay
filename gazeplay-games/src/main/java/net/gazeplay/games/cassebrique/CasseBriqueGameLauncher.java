package net.gazeplay.games.cassebrique;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "CasseBrique" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant (or with one of before the review of 3/11/2026): the block, normal level,
 * drawn background.
 */
public class CasseBriqueGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CasseBriqueStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new CasseBriqueStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return withLives(new CasseBrique(gameContext, stats, mapOf(gameVariant), levelOf(gameVariant),
            backgroundOf(gameVariant), -1), gameVariant);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return withLives(new CasseBrique(gameContext, stats, mapOf(gameVariant), levelOf(gameVariant),
            backgroundOf(gameVariant), gameSeed), gameVariant);
    }

    /**
     * @return the game, with the hearts or not as chosen in the variant (17/11/2026)
     */
    static CasseBrique withLives(final CasseBrique game, final IGameVariant gameVariant) {
        game.setWithLives(LivesVariant.withLives(gameVariant));
        return game;
    }

    static CasseBriqueMap mapOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? CasseBriqueMap.of(grouped.getValue())
            : CasseBriqueMap.BLOC;
    }

    static CasseBriqueLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? CasseBriqueLevel.of(grouped.getSubGroup())
            : CasseBriqueLevel.NORMAL;
    }

    static CasseBriqueBackground backgroundOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? CasseBriqueBackground.of(grouped.getGroup())
            : CasseBriqueBackground.DRAWN;
    }
}

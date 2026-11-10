package net.gazeplay.games.magicpotions;

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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Magic Potions" (name code Potions) and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A game of before the review of 10/11/2026 (it had no variant) is played with the recipe,
 * without end.
 */
public class PotionsGameLauncher implements IGameLauncher<MagicPotionsStats, IGameVariant> {

    @Override
    public MagicPotionsStats createNewStats(final Scene scene) {
        return new MagicPotionsStats(scene);
    }

    @Override
    public MagicPotionsStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach,
                                              int nbUnCountedGoalsReached,
                                              ArrayList<LinkedList<FixationPoint>> fixationSequence,
                                              LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport,
                                              SavedStatsInfo savedStatsInfo) {
        return new MagicPotionsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence,
            lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final MagicPotionsStats stats) {
        return new MagicPotions(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                    final MagicPotionsStats stats, final double gameSeed) {
        return new MagicPotions(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static PotionsLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? PotionsLevel.of(grouped.getSubGroup())
            : PotionsLevel.WITH_RECIPE;
    }

    static PotionsEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? PotionsEnd.of(grouped.getValue())
            : PotionsEnd.ENDLESS;
    }
}

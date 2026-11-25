package net.gazeplay.games.magicpotions;

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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Recettes magiques" (name code Potions) and its statistics, for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). Without a variant of the game: recipes of 2 ingredients, without end.
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
        return withOptions(new MagicPotions(gameContext, stats, sizeOf(gameVariant), endOf(gameVariant), -1),
            gameVariant);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                    final MagicPotionsStats stats, final double gameSeed) {
        return withOptions(new MagicPotions(gameContext, stats, sizeOf(gameVariant), endOf(gameVariant), gameSeed),
            gameVariant);
    }

    /**
     * @return the game, with the hearts or not and the recipe hidden or not as chosen in the variant
     */
    static MagicPotions withOptions(final MagicPotions game, final IGameVariant gameVariant) {
        game.setWithLives(LivesVariant.withLives(gameVariant));
        game.setHiddenRecipe(gameVariant instanceof GroupedGameVariant grouped
            && PotionsGameVariantGenerator.HIDDEN.equals(grouped.getGroup()));
        return game;
    }

    static RecipeSize sizeOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? RecipeSize.of(grouped.getValue())
            : RecipeSize.TWO;
    }

    static PotionsEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? PotionsEnd.of(grouped.getSubGroup())
            : PotionsEnd.ENDLESS;
    }
}

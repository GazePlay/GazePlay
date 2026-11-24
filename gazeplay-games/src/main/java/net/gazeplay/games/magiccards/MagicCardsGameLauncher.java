package net.gazeplay.games.magiccards;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
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
 * Creates "Cartes magiques" (name code MagicCards) and its statistics (folder "MagicCards", as before), for a new game or
 * for the replay of a saved one (see {@code IGameLauncher}). The grids of before the review (2 x 2, 2 x 3, 3 x 2, 3 x 3)
 * are read as 4, 6, 6 and 9 cards, in the magic (the only mode since 17/11/2026), with the animals, without end
 * (maintainer's choice of 12/11/2026).
 */
public class MagicCardsGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new MagicCardsGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new MagicCardsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static MagicCards create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                             final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final var extras = grouped.getExtras();
            final MagicCards game = withLives(new MagicCards(gameContext, stats, MagicCardsMode.of(grouped.getGroup()),
                MagicCardsPictures.of(grouped.getSubGroup()), MagicCardsCount.of(grouped.getValue()),
                MagicCardsEnd.of(extras.isEmpty() ? null : extras.get(0)), gameSeed), gameVariant);
            // The cards to find (24/11/2026); half of the cards for a variant of before
            game.setFind(MagicCardsFind.of(extras));
            return game;
        }
        MagicCardsCount count = MagicCardsCount.FOUR;
        if (gameVariant instanceof DimensionGameVariant grid) {
            count = MagicCardsCount.ofCards(grid.getWidth() * grid.getHeight());
        }
        return withLives(new MagicCards(gameContext, stats, MagicCardsMode.MAGIC, MagicCardsPictures.ANIMALS, count,
            MagicCardsEnd.ENDLESS, gameSeed), gameVariant);
    }

    /**
     * @return the game, with the hearts or not as chosen in the variant (16/11/2026)
     */
    private static MagicCards withLives(final MagicCards game, final IGameVariant gameVariant) {
        game.setWithLives(LivesVariant.withLives(gameVariant));
        return game;
    }
}

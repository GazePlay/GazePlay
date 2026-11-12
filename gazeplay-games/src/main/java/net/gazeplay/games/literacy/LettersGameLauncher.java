package net.gazeplay.games.literacy;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Creates "Identifier les lettres" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review of 12/11/2026 (a grid, "DimensionGameVariant"): 2 x 2 easy,
 * 2 x 3 normal, 3 x 3 hard, in capitals with the animals, without end; without a variant, the easy level.
 */
public class LettersGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new LettersGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new LettersGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Letters create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                          final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final List<String> extras = grouped.getExtras();
            return new Letters(gameContext, stats, LettersLevel.of(grouped.getValue()),
                LettersCase.of(grouped.getSubGroup()), LettersPictures.of(grouped.getGroup()),
                LettersEnd.of(extras.isEmpty() ? null : extras.get(0)), gameSeed);
        }
        LettersLevel level = LettersLevel.EASY;
        if (gameVariant instanceof DimensionGameVariant grid) {
            final int cards = grid.getWidth() * grid.getHeight();
            level = cards >= 9 ? LettersLevel.HARD : cards >= 6 ? LettersLevel.NORMAL : LettersLevel.EASY;
        }
        return new Letters(gameContext, stats, level, LettersCase.CAPITALS, LettersPictures.ANIMALS, LettersEnd.ENDLESS,
            gameSeed);
    }
}

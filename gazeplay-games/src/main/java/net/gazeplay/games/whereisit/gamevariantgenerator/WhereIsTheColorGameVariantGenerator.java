package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.games.whereisit.WhereIsItVariants;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the game "WhereIsTheColor" proposed in the menu: each theme and size of grid with each end (5,
 * 10 or 15 screens, or without end, 16/11/2026; see {@link WhereIsItVariants}).
 */
public class WhereIsTheColorGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return WhereIsItVariants.withEnds(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, Difficulty.EASY.toString()),
            new DimensionDifficultyGameVariant(2, 2, Difficulty.EASY.toString()),
            new DimensionDifficultyGameVariant(2, 3, Difficulty.EASY.toString()),
            new DimensionDifficultyGameVariant(3, 2, Difficulty.EASY.toString()),
            new DimensionDifficultyGameVariant(1, 2, Difficulty.NORMAL.toString()),
            new DimensionDifficultyGameVariant(2, 2, Difficulty.NORMAL.toString()),
            new DimensionDifficultyGameVariant(2, 3, Difficulty.NORMAL.toString()),
            new DimensionDifficultyGameVariant(3, 2, Difficulty.NORMAL.toString()),
            new DimensionDifficultyGameVariant(3, 3, Difficulty.NORMAL.toString()),
            new DimensionDifficultyGameVariant(1, 2, Difficulty.HARD.toString()),
            new DimensionDifficultyGameVariant(2, 2, Difficulty.HARD.toString()),
            new DimensionDifficultyGameVariant(2, 3, Difficulty.HARD.toString()),
            new DimensionDifficultyGameVariant(3, 2, Difficulty.HARD.toString()),
            new DimensionDifficultyGameVariant(3, 3, Difficulty.HARD.toString())
        ), "WhereIsTheColor");
    }
}

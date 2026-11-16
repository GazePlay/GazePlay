package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.games.whereisit.WhereIsItVariants;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the game "WhereIsTheFlag" proposed in the menu: each theme and size of grid with each end (5,
 * 10 or 15 screens, or without end, 16/11/2026; see {@link WhereIsItVariants}).
 */
public class WhereIsTheFlagGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return WhereIsItVariants.withEnds(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, "MostFamous"),
            new DimensionDifficultyGameVariant(2, 2, "MostFamous"),
            new DimensionDifficultyGameVariant(2, 3, "MostFamous"),
            new DimensionDifficultyGameVariant(3, 2, "MostFamous"),
            new DimensionDifficultyGameVariant(3, 3, "MostFamous"),
            new DimensionDifficultyGameVariant(1, 2, "Africa"),
            new DimensionDifficultyGameVariant(2, 2, "Africa"),
            new DimensionDifficultyGameVariant(2, 3, "Africa"),
            new DimensionDifficultyGameVariant(3, 2, "Africa"),
            new DimensionDifficultyGameVariant(3, 3, "Africa"),
            new DimensionDifficultyGameVariant(1, 2, "America"),
            new DimensionDifficultyGameVariant(2, 2, "America"),
            new DimensionDifficultyGameVariant(2, 3, "America"),
            new DimensionDifficultyGameVariant(3, 2, "America"),
            new DimensionDifficultyGameVariant(3, 3, "America"),
            new DimensionDifficultyGameVariant(1, 2, "Asia"),
            new DimensionDifficultyGameVariant(2, 2, "Asia"),
            new DimensionDifficultyGameVariant(2, 3, "Asia"),
            new DimensionDifficultyGameVariant(3, 2, "Asia"),
            new DimensionDifficultyGameVariant(3, 3, "Asia"),
            new DimensionDifficultyGameVariant(1, 2, "Europe"),
            new DimensionDifficultyGameVariant(2, 2, "Europe"),
            new DimensionDifficultyGameVariant(2, 3, "Europe"),
            new DimensionDifficultyGameVariant(3, 2, "Europe"),
            new DimensionDifficultyGameVariant(3, 3, "Europe"),
            new DimensionDifficultyGameVariant(1, 2, "AllFlags"),
            new DimensionDifficultyGameVariant(2, 2, "AllFlags"),
            new DimensionDifficultyGameVariant(2, 3, "AllFlags"),
            new DimensionDifficultyGameVariant(3, 2, "AllFlags"),
            new DimensionDifficultyGameVariant(3, 3, "AllFlags")
        ), "WhereIsTheFlag");
    }
}

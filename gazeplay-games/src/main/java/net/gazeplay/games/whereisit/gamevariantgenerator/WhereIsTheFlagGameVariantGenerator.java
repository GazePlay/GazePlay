package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class WhereIsTheFlagGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
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
        ));
    }
}

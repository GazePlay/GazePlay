package net.gazeplay.games.whereisit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class WhereIsTheColorGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, "easy"),
            new DimensionDifficultyGameVariant(2, 2, "easy"),
            new DimensionDifficultyGameVariant(2, 3, "easy"),
            new DimensionDifficultyGameVariant(3, 2, "easy"),
            new DimensionDifficultyGameVariant(1, 2, "normal"),
            new DimensionDifficultyGameVariant(2, 2, "normal"),
            new DimensionDifficultyGameVariant(2, 3, "normal"),
            new DimensionDifficultyGameVariant(3, 2, "normal"),
            new DimensionDifficultyGameVariant(3, 3, "normal")
        ));
    }
}

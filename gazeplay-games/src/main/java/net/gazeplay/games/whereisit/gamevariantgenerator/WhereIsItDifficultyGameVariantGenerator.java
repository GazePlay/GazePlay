package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class WhereIsItDifficultyGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, "Normal"),
            new DimensionDifficultyGameVariant(2, 2, "Normal"),
            new DimensionDifficultyGameVariant(2, 3, "Normal"),
            new DimensionDifficultyGameVariant(3, 2, "Normal"),
            new DimensionDifficultyGameVariant(3, 3, "Normal"),
            new DimensionDifficultyGameVariant(1, 2, "Easy"),
            new DimensionDifficultyGameVariant(2, 2, "Easy"),
            new DimensionDifficultyGameVariant(2, 3, "Easy"),
            new DimensionDifficultyGameVariant(3, 2, "Easy")
        ));
    }
}

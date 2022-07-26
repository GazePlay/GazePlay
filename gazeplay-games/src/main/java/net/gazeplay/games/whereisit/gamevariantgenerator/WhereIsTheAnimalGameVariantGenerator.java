package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class WhereIsTheAnimalGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, "AllAnimals"),
            new DimensionDifficultyGameVariant(2, 2, "AllAnimals"),
            new DimensionDifficultyGameVariant(2, 3, "AllAnimals"),
            new DimensionDifficultyGameVariant(3, 2, "AllAnimals"),
            new DimensionDifficultyGameVariant(3, 3, "AllAnimals"),
            new DimensionDifficultyGameVariant(1, 2, "Dynamic")
        ));
    }
}

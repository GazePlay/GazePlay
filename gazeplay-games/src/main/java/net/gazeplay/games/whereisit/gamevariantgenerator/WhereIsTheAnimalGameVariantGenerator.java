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
            new DimensionDifficultyGameVariant(1, 2, "Farm"),
            new DimensionDifficultyGameVariant(2, 2, "Farm"),
            new DimensionDifficultyGameVariant(2, 3, "Farm"),
            new DimensionDifficultyGameVariant(3, 2, "Farm"),
            new DimensionDifficultyGameVariant(3, 3, "Farm"),
            new DimensionDifficultyGameVariant(1, 2, "Forest"),
            new DimensionDifficultyGameVariant(2, 2, "Forest"),
            new DimensionDifficultyGameVariant(2, 3, "Forest"),
            new DimensionDifficultyGameVariant(3, 2, "Forest"),
            new DimensionDifficultyGameVariant(3, 3, "Forest"),
            new DimensionDifficultyGameVariant(1, 2, "Savanna"),
            new DimensionDifficultyGameVariant(2, 2, "Savanna"),
            new DimensionDifficultyGameVariant(2, 3, "Savanna"),
            new DimensionDifficultyGameVariant(3, 2, "Savanna"),
            new DimensionDifficultyGameVariant(3, 3, "Savanna"),
            new DimensionDifficultyGameVariant(1, 2, "Birds"),
            new DimensionDifficultyGameVariant(2, 2, "Birds"),
            new DimensionDifficultyGameVariant(2, 3, "Birds"),
            new DimensionDifficultyGameVariant(3, 2, "Birds"),
            new DimensionDifficultyGameVariant(3, 3, "Birds"),
            new DimensionDifficultyGameVariant(1, 2, "Maritime"),
            new DimensionDifficultyGameVariant(2, 2, "Maritime"),
            new DimensionDifficultyGameVariant(2, 3, "Maritime"),
            new DimensionDifficultyGameVariant(3, 2, "Maritime"),
            new DimensionDifficultyGameVariant(3, 3, "Maritime"),
            new DimensionDifficultyGameVariant(1, 2, "AllAnimals"),
            new DimensionDifficultyGameVariant(2, 2, "AllAnimals"),
            new DimensionDifficultyGameVariant(2, 3, "AllAnimals"),
            new DimensionDifficultyGameVariant(3, 2, "AllAnimals"),
            new DimensionDifficultyGameVariant(3, 3, "AllAnimals"),
            new DimensionDifficultyGameVariant(1, 2, "Dynamic")
        ));
    }
}

package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class WhereIsTheSoundGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, "Animals"),
            new DimensionDifficultyGameVariant(2, 2, "Animals"),
            new DimensionDifficultyGameVariant(2, 3, "Animals"),
            new DimensionDifficultyGameVariant(3, 2, "Animals"),
            new DimensionDifficultyGameVariant(3, 3, "Animals"),
            new DimensionDifficultyGameVariant(1, 2, "Instruments"),
            new DimensionDifficultyGameVariant(2, 2, "Instruments"),
            new DimensionDifficultyGameVariant(2, 3, "Instruments"),
            new DimensionDifficultyGameVariant(3, 2, "Instruments"),
            new DimensionDifficultyGameVariant(3, 3, "Instruments"),
            new DimensionDifficultyGameVariant(1, 2, "AllSounds"),
            new DimensionDifficultyGameVariant(2, 2, "AllSounds"),
            new DimensionDifficultyGameVariant(2, 3, "AllSounds"),
            new DimensionDifficultyGameVariant(3, 2, "AllSounds"),
            new DimensionDifficultyGameVariant(3, 3, "AllSounds")
        ));
    }
}

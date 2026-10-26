package net.gazeplay.games.memory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the games "Memory", "MemoryLetters", "MemoryNumbers", "OpenMemory", "OpenMemoryLetters" and
 * "OpenMemoryNumbers" proposed in the menu.
 */
public class MemoryGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionDifficultyGameVariant(2, 2, "Normal"),
            new DimensionDifficultyGameVariant(2, 3, "Normal"),
            new DimensionDifficultyGameVariant(3, 2, "Normal"),
            new DimensionDifficultyGameVariant(3, 4, "Normal"),
            new DimensionDifficultyGameVariant(4, 3, "Normal"),
            new DimensionDifficultyGameVariant(2, 2, "Dynamic")
        ));
    }
}

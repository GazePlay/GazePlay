package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class WhereIsTheLetterGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionDifficultyGameVariant(1, 2, "Vowels"),
            new DimensionDifficultyGameVariant(2, 2, "Vowels"),
            new DimensionDifficultyGameVariant(2, 3, "Vowels"),
            new DimensionDifficultyGameVariant(3, 2, "Vowels"),
            new DimensionDifficultyGameVariant(1, 2, "Consonants"),
            new DimensionDifficultyGameVariant(2, 2, "Consonants"),
            new DimensionDifficultyGameVariant(2, 3, "Consonants"),
            new DimensionDifficultyGameVariant(3, 2, "Consonants"),
            new DimensionDifficultyGameVariant(3, 3, "Consonants"),
            new DimensionDifficultyGameVariant(1, 2, "AllLetters"),
            new DimensionDifficultyGameVariant(2, 2, "AllLetters"),
            new DimensionDifficultyGameVariant(2, 3, "AllLetters"),
            new DimensionDifficultyGameVariant(3, 2, "AllLetters"),
            new DimensionDifficultyGameVariant(3, 3, "AllLetters")
        ));
    }
}

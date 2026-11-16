package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.games.whereisit.WhereIsItVariants;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the game "WhereIsTheLetter" proposed in the menu: each theme and size of grid with each end (5,
 * 10 or 15 screens, or without end, 16/11/2026; see {@link WhereIsItVariants}).
 */
public class WhereIsTheLetterGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return WhereIsItVariants.withEnds(Lists.newArrayList(
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
        ), "WhereIsTheLetter");
    }
}

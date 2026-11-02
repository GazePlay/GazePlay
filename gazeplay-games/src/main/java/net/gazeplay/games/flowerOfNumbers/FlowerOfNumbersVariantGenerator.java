package net.gazeplay.games.flowerOfNumbers;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives the variants of "FlowerOfNumbers" (maintainer's decisions of 2/11/2026): the largest number of the heart, 10 to
 * 50 (buttons), and the mode, guided first (switches at the bottom, see {@link FlowerMode}). One group only: the
 * window of the variants shows no switch for it.
 */
public class FlowerOfNumbersVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "FlowerOfNumbers";

    /**
     * The largest numbers of the heart, one variant each (key of translation "FlowerUpTo<n>").
     */
    static final List<Integer> LARGEST_NUMBERS = List.of(10, 20, 30, 40, 50);

    static final String UP_TO = "FlowerUpTo";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final FlowerMode mode : FlowerMode.values()) {
            for (final int largest : LARGEST_NUMBERS) {
                variants.add(new GroupedGameVariant(GROUP, mode.getKey(), UP_TO + largest, null));
            }
        }
        return variants;
    }
}

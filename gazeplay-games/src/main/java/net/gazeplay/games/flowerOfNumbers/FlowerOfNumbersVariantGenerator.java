package net.gazeplay.games.flowerOfNumbers;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives the variants of "FlowerOfNumbers" (maintainer's decisions of 2/11/2026): the largest number of the heart, 10 to
 * 50 (buttons), and at the bottom the mode, guided first (see {@link FlowerMode}), and the hearts (see
 * {@link LivesVariant}, 17/11/2026: with hearts, a value going over the number, or over the part of its petal when
 * guided, costs one). One group only: the window of the variants shows no switch for it.
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
            for (final String lives : LivesVariant.options(true)) {
                for (final int largest : LARGEST_NUMBERS) {
                    variants.add(new GroupedGameVariant(GROUP, mode.getKey(), UP_TO + largest, null, lives));
                }
            }
        }
        return variants;
    }
}

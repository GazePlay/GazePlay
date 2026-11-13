package net.gazeplay.games.bottle;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Bouteille" (maintainer's choices of 13/11/2026): the mode at the top (see {@link BottleMode}),
 * the number of bottles as buttons (see {@link BottleCount}), and at the bottom the end (see {@link BottleEnd}) and the
 * size of the bottles (see {@link BottleSize}).
 */
public class BottleGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final BottleMode mode : BottleMode.values()) {
            for (final BottleEnd end : BottleEnd.values()) {
                for (final BottleSize size : BottleSize.values()) {
                    for (final BottleCount count : BottleCount.values()) {
                        variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), count.getKey(), null,
                            size.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}

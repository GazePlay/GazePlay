package net.gazeplay.games.magicpotions;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Magic Potions" (maintainer's choices of 10/11/2026): when it ends (buttons, see
 * {@link PotionsEnd}), and at the bottom the level (see {@link PotionsLevel}). One group only: the window of the
 * variants shows no switch for it. (Before the review: no variant.)
 */
public class PotionsGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "Potions";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final PotionsLevel level : PotionsLevel.values()) {
            for (final PotionsEnd end : PotionsEnd.values()) {
                variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}

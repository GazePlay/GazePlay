package net.gazeplay.games.cups;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Gobelets Pass Pass" (maintainer's choices of 10/11/2026): when it ends (buttons, see
 * {@link CupsEnd}), and at the bottom the level (see {@link CupsLevel}). One group only: the window of the variants
 * shows no switch for it. (Before the review: 3 or 5 cups, under a title in English.)
 */
public class CupsBallsGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "CupsBalls";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final CupsLevel level : CupsLevel.values()) {
            for (final CupsEnd end : CupsEnd.values()) {
                variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}

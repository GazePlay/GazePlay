package net.gazeplay.games.space;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Space Game" (maintainer's rules of 3/11/2026): when it ends (buttons, see {@link SpaceEnd}),
 * and at the bottom the level (see {@link SpaceLevel}) and the hearts (see {@link LivesVariant}, maintainer's choice of
 * 17/11/2026: without hearts, the game cannot be lost). One group only: the window of the variants shows no switch for
 * it.
 */
public class SpaceGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "SpaceGame";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final SpaceLevel level : SpaceLevel.values()) {
            for (final String lives : LivesVariant.options(true)) {
                for (final SpaceEnd end : SpaceEnd.values()) {
                    variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null, lives));
                }
            }
        }
        return variants;
    }
}

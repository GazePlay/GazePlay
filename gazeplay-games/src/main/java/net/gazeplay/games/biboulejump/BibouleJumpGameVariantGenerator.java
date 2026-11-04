package net.gazeplay.games.biboulejump;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of Biboule Jump (maintainer's choices of 4/11/2026): when it ends (buttons, see
 * {@link BibouleJumpEnd}), and at the bottom the level (see {@link BibouleJumpLevel}). One group only: the window of the
 * variants shows no switch for it.
 */
public class BibouleJumpGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "BibJump";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final BibouleJumpLevel level : BibouleJumpLevel.values()) {
            for (final BibouleJumpEnd end : BibouleJumpEnd.values()) {
                variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}

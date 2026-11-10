package net.gazeplay.games.simon;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Répète après moi" (maintainer's choices of 10/11/2026): when it ends (buttons, see
 * {@link SimonEnd}), and at the bottom the mode (see {@link SimonMode}). One group only: the window of the variants
 * shows no switch for it. (Before the review: easy, normal, hard, "Simon copies you", multiplayer.)
 */
public class SimonGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "Simon";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final SimonMode mode : SimonMode.values()) {
            for (final SimonEnd end : SimonEnd.values()) {
                variants.add(new GroupedGameVariant(GROUP, mode.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}

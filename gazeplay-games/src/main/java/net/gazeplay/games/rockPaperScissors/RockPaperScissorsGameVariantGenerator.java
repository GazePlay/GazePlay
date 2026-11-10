package net.gazeplay.games.rockPaperScissors;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Pierre papier ciseaux" (maintainer's choices of 10/11/2026): when it ends (buttons, see
 * {@link RockPaperScissorsEnd}, those of the mode), and at the bottom the mode (see {@link RockPaperScissorsMode}).
 * One group only: the window of the variants shows no switch for it. (Before the review: "BO3" to "BO9", and 3, 5 or
 * 10 rounds with the sign shown.)
 */
public class RockPaperScissorsGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "RockPaperScissors";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final RockPaperScissorsMode mode : RockPaperScissorsMode.values()) {
            for (final RockPaperScissorsEnd end : RockPaperScissorsEnd.values()) {
                if (end.isFor(mode)) {
                    variants.add(new GroupedGameVariant(GROUP, mode.getKey(), end.getKey(), null));
                }
            }
        }
        return variants;
    }
}

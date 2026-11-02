package net.gazeplay.games.sprintFinish;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Sprint Finish" (maintainer's rules of 2/11/2026): the difficulty, easy, normal or hard (5, 10
 * or 15 maps to win, buttons), and how it is played (switches at the bottom, see {@link SprintMode}). One group only:
 * the window of the variants shows no switch for it.
 */
public class SprintFinishVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "SprintFinish";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final SprintMode mode : SprintMode.values()) {
            for (final SprintDifficulty difficulty : SprintDifficulty.values()) {
                variants.add(new GroupedGameVariant(GROUP, mode.getKey(), difficulty.getKey(), null));
            }
        }
        return variants;
    }
}

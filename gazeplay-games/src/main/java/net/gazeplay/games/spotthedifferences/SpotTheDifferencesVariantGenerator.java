package net.gazeplay.games.spotthedifferences;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives the variants of "SpotDifference" (maintainer's request of 2/11/2026): the level (buttons), and whether a ring
 * shows the fixation on a difference (switches at the bottom, hidden first: the game as reviewed on 30/10/2026). There
 * is one group only, so the window of the variants shows no switch for it.
 */
public class SpotTheDifferencesVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "SpotDifference";
    static final String RING_HIDDEN = "SpotDifferencesRingHidden";
    static final String RING_SHOWN = "SpotDifferencesRingShown";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final String ring : List.of(RING_HIDDEN, RING_SHOWN)) {
            for (final SpotTheDifferencesVariant level : SpotTheDifferencesVariant.values()) {
                variants.add(new GroupedGameVariant(GROUP, ring, level.getLabel(), null));
            }
        }
        return variants;
    }
}

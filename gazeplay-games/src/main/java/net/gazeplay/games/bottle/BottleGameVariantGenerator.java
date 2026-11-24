package net.gazeplay.games.bottle;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Bouteille" (maintainer's choices of 13/11/2026): the mode at the top (see {@link BottleMode}:
 * "Tout casser" again since 24/11/2026, then "Détruire les cibles"), the number of bottles as buttons (see
 * {@link BottleCount}), and at the bottom the end (see {@link BottleEnd}), the size of the bottles (see
 * {@link BottleSize}) and the hearts, with every end, without end too (17/11/2026); no hearts to break them all, where
 * no bottle is a mistake.
 */
public class BottleGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final BottleMode mode : BottleMode.values()) {
            for (final BottleEnd end : BottleEnd.values()) {
                for (final BottleSize size : BottleSize.values()) {
                    for (final String lives : LivesVariant.options(mode == BottleMode.COLOR)) {
                        // To break them all, as soon as looked at (first, so chosen by default) or after a fixation;
                        // the targets always after a fixation (maintainer's decision of 24/11/2026: as soon as
                        // looked at, too hard to move the gaze to the right bottles). Every variant has the switch,
                        // with a single value for the targets (no switch shown)
                        for (final BottleThrow way : mode == BottleMode.ALL ? BottleThrow.values()
                            : new BottleThrow[]{BottleThrow.FIXATION}) {
                            for (final BottleCount count : BottleCount.values()) {
                                variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), count.getKey(), null,
                                    size.getKey(), lives, way.getKey()));
                            }
                        }
                    }
                }
            }
        }
        return variants;
    }
}

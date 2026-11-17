package net.gazeplay.games.dottodot;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Point par point" (maintainer's choices of 12/11/2026): the level as buttons (see
 * {@link DotLevel}), and at the bottom what the dots show (see {@link DotLabels}), the end (see {@link DotEnd}) and the
 * hearts (see {@link LivesVariant}), with any end, without end too (maintainer's choice of 17/11/2026: as many choices
 * as possible for the users).
 */
public class DotToDotGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final DotLabels labels : DotLabels.values()) {
            for (final DotEnd end : DotEnd.values()) {
                for (final String lives : LivesVariant.options(true)) {
                    for (final DotLevel level : DotLevel.values()) {
                        variants.add(new GroupedGameVariant(labels.getKey(), end.getKey(), level.getKey(), null, lives));
                    }
                }
            }
        }
        return variants;
    }
}

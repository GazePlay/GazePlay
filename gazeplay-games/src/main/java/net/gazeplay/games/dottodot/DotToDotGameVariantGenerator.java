package net.gazeplay.games.dottodot;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Point par point" (maintainer's choices of 12/11/2026): the level as buttons (see
 * {@link DotLevel}), and at the bottom what the dots show (see {@link DotLabels}) and the end (see {@link DotEnd}).
 */
public class DotToDotGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final DotLabels labels : DotLabels.values()) {
            for (final DotEnd end : DotEnd.values()) {
                for (final DotLevel level : DotLevel.values()) {
                    variants.add(new GroupedGameVariant(labels.getKey(), end.getKey(), level.getKey(), null));
                }
            }
        }
        return variants;
    }
}

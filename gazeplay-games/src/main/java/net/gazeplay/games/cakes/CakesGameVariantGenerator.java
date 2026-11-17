package net.gazeplay.games.cakes;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "La Fabrique à gâteaux" (maintainer's choices of 12/11/2026): the mode at the top (see
 * {@link CakesMode}), the level as buttons (see {@link CakesLevel}: easy, normal or hard to copy the model, a single
 * value in the free mode), and at the bottom the end (see {@link CakesEnd}) and, to copy the model, the hearts (see
 * {@link LivesVariant}), with every end, without end too (maintainer's choice of 17/11/2026).
 */
public class CakesGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final CakesMode mode : CakesMode.values()) {
            for (final CakesEnd end : CakesEnd.values()) {
                if (mode == CakesMode.FREE) {
                    variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), CakesLevel.FREE.getKey(), null,
                        LivesVariant.WITHOUT));
                    continue;
                }
                for (final String lives : LivesVariant.options(mode == CakesMode.COPY)) {
                    for (final CakesLevel level : new CakesLevel[]{CakesLevel.EASY, CakesLevel.NORMAL,
                        CakesLevel.HARD}) {
                        variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), level.getKey(), null, lives));
                    }
                }
            }
        }
        return variants;
    }
}

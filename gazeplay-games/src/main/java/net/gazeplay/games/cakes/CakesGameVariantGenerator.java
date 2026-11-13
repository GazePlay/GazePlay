package net.gazeplay.games.cakes;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "La Fabrique à gâteaux" (maintainer's choices of 12/11/2026): the mode at the top (see
 * {@link CakesMode}), the level as buttons (see {@link CakesLevel}: easy, normal or hard to copy the model, a single
 * value in the free mode), and at the bottom the end (see {@link CakesEnd}).
 */
public class CakesGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final CakesMode mode : CakesMode.values()) {
            for (final CakesEnd end : CakesEnd.values()) {
                if (mode == CakesMode.FREE) {
                    variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), CakesLevel.FREE.getKey(), null));
                    continue;
                }
                for (final CakesLevel level : new CakesLevel[]{CakesLevel.EASY, CakesLevel.NORMAL, CakesLevel.HARD}) {
                    variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), level.getKey(), null));
                }
            }
        }
        return variants;
    }
}

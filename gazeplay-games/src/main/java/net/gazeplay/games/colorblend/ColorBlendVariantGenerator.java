package net.gazeplay.games.colorblend;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Mélanger les couleurs" (maintainer's choices of 13/11/2026): the mode (see
 * {@link ColorBlendMode}, a single one since 17/11/2026, so no switch), the level as buttons (see
 * {@link ColorBlendLevel}), and at the bottom the end (see {@link ColorBlendEnd}) and the hearts, with every end,
 * without end too (17/11/2026).
 */
public class ColorBlendVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final ColorBlendEnd end : ColorBlendEnd.values()) {
            for (final ColorBlendLevel level : ColorBlendLevel.values()) {
                for (final String lives : LivesVariant.options(true)) {
                    variants.add(new GroupedGameVariant(ColorBlendMode.CHALLENGE.getKey(), end.getKey(),
                        level.getKey(), null, lives));
                }
            }
        }
        return variants;
    }
}

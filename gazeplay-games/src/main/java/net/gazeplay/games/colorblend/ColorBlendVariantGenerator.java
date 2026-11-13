package net.gazeplay.games.colorblend;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Mélanger les couleurs" (maintainer's choices of 13/11/2026): the mode at the top (see
 * {@link ColorBlendMode}), the level as buttons (see {@link ColorBlendLevel}), and at the bottom the end of the
 * challenge (see {@link ColorBlendEnd}); the free mode has a single variant, without end.
 */
public class ColorBlendVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final ColorBlendEnd end : ColorBlendEnd.values()) {
            for (final ColorBlendLevel level : new ColorBlendLevel[]{ColorBlendLevel.EASY, ColorBlendLevel.NORMAL,
                ColorBlendLevel.HARD}) {
                variants.add(new GroupedGameVariant(ColorBlendMode.CHALLENGE.getKey(), end.getKey(), level.getKey(),
                    null));
            }
        }
        variants.add(new GroupedGameVariant(ColorBlendMode.FREE.getKey(), ColorBlendEnd.ENDLESS.getKey(),
            ColorBlendLevel.FREE.getKey(), null));
        return variants;
    }
}

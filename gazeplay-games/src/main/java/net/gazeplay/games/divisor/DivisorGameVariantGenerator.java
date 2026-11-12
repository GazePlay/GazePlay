package net.gazeplay.games.divisor;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Diviseur" (maintainer's choices of 12/11/2026): the level (buttons, see {@link DivisorLevel}),
 * and at the bottom what the targets show (see {@link DivisorTheme}; "My images" plays with the images of the user) and
 * how they burst (see {@link DivisorTouch}).
 */
public class DivisorGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final DivisorTheme theme : DivisorTheme.values()) {
            for (final DivisorTouch touch : DivisorTouch.values()) {
                for (final DivisorLevel level : DivisorLevel.values()) {
                    variants.add(new GroupedGameVariant(theme.getKey(), touch.getKey(), level.getKey(),
                        theme == DivisorTheme.MY_IMAGES ? DivisorTheme.USER_IMAGES_FOLDER : null));
                }
            }
        }
        return variants;
    }
}

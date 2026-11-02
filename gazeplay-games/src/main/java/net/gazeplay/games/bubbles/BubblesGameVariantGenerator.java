package net.gazeplay.games.bubbles;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Bubbles" (maintainer's decisions of 2/11/2026): where the bubbles go (buttons), and at the
 * bottom how they burst (see {@link BubblesBurst}) and what they show (see {@link BubblesContent}; "My images" plays
 * with the images of the user, see {@code UserImagesVariant}).
 */
public class BubblesGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final BubblesBurst burst : BubblesBurst.values()) {
            for (final BubblesContent content : BubblesContent.values()) {
                for (final BubblesGameVariant direction : BubblesGameVariant.values()) {
                    variants.add(new GroupedGameVariant(burst.getKey(), content.getKey(), direction.getDirection(),
                        content == BubblesContent.MY_IMAGES ? BubblesContent.USER_IMAGES_FOLDER : null));
                }
            }
        }
        return variants;
    }
}

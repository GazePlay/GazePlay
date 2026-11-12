package net.gazeplay.games.scratchcard;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Cartes à gratter" (maintainer's choices of 12/11/2026): the pictures at the top (see
 * {@link ScratchCardPictures}; "My images" plays with the images of the user), the size of the scraper as buttons (see
 * {@link ScratchCardBrush}), and at the bottom the end (see {@link ScratchCardEnd}).
 */
public class ScratchCardGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final ScratchCardPictures pictures : ScratchCardPictures.values()) {
            for (final ScratchCardEnd end : ScratchCardEnd.values()) {
                for (final ScratchCardBrush brush : ScratchCardBrush.values()) {
                    variants.add(new GroupedGameVariant(pictures.getKey(), end.getKey(), brush.getKey(),
                        pictures == ScratchCardPictures.MY_IMAGES ? ScratchCardPictures.USER_IMAGES_FOLDER : null));
                }
            }
        }
        return variants;
    }
}

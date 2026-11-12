package net.gazeplay.games.magiccards;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Cartes magiques" (maintainer's choices of 12/11/2026): the mode at the top (see
 * {@link MagicCardsMode}), the number of cards as buttons (see {@link MagicCardsCount}), and at the bottom the pictures
 * (see {@link MagicCardsPictures}; "My images" plays with the images of the user) and the end (see
 * {@link MagicCardsEnd}).
 */
public class MagicCardsGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final MagicCardsMode mode : MagicCardsMode.values()) {
            for (final MagicCardsPictures pictures : MagicCardsPictures.values()) {
                for (final MagicCardsEnd end : MagicCardsEnd.values()) {
                    for (final MagicCardsCount count : MagicCardsCount.values()) {
                        variants.add(new GroupedGameVariant(mode.getKey(), pictures.getKey(), count.getKey(),
                            pictures == MagicCardsPictures.MY_IMAGES ? MagicCardsPictures.USER_IMAGES_FOLDER : null,
                            end.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}

package net.gazeplay.games.magiccards;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Cartes magiques" (maintainer's choices of 12/11/2026): the mode (see {@link MagicCardsMode}, a
 * single one since 17/11/2026, so no switch), the number of cards as buttons (see {@link MagicCardsCount}), and at the
 * bottom the pictures (see {@link MagicCardsPictures}; "My images" plays with the images of the user), the end (see
 * {@link MagicCardsEnd}) and the hearts (see {@link LivesVariant}), with every end, without end too (17/11/2026).
 */
public class MagicCardsGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final MagicCardsMode mode : MagicCardsMode.values()) {
            for (final MagicCardsPictures pictures : MagicCardsPictures.values()) {
                for (final MagicCardsEnd end : MagicCardsEnd.values()) {
                    for (final String lives : LivesVariant.options(true)) {
                        for (final MagicCardsCount count : MagicCardsCount.values()) {
                            variants.add(new GroupedGameVariant(mode.getKey(), pictures.getKey(), count.getKey(),
                                pictures == MagicCardsPictures.MY_IMAGES ? MagicCardsPictures.USER_IMAGES_FOLDER
                                    : null, end.getKey(), lives));
                        }
                    }
                }
            }
        }
        return variants;
    }
}

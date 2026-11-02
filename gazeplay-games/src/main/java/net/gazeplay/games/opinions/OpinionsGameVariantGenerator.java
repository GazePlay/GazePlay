package net.gazeplay.games.opinions;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Opinions" proposed in the menu: only the images of the user (see
 * {@link OpinionsGameVariant}).
 */
public class OpinionsGameVariantGenerator extends EnumGameVariantGenerator<OpinionsGameVariant> {

    public OpinionsGameVariantGenerator() {
        super(new OpinionsGameVariant[]{OpinionsGameVariant.MY_IMAGES}, OpinionsGameVariant::getLabel);
    }
}

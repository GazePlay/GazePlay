package net.gazeplay.games.opinions;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;
/**
 * Gives the variants of the game "Opinions" proposed in the menu.
 */
public class OpinionsGameVariantGenerator extends EnumGameVariantGenerator<OpinionsGameVariant> {

    public OpinionsGameVariantGenerator() {
        super(OpinionsGameVariant.values(), OpinionsGameVariant::getLabel);
    }
}

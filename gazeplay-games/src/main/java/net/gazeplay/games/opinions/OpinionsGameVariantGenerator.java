package net.gazeplay.games.opinions;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;
import net.gazeplay.games.cakes.CakeGameVariant;

public class OpinionsGameVariantGenerator extends EnumGameVariantGenerator<OpinionsGameVariant> {

    public OpinionsGameVariantGenerator() {
        super(OpinionsGameVariant.values(), OpinionsGameVariant::getLabel);
    }
}

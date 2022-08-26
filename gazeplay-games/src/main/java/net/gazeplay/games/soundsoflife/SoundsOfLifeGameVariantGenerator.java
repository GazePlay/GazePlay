package net.gazeplay.games.soundsoflife;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class SoundsOfLifeGameVariantGenerator extends EnumGameVariantGenerator<SoundsOfLifeGameVariant> {

    public SoundsOfLifeGameVariantGenerator() {
        super(SoundsOfLifeGameVariant.values(), SoundsOfLifeGameVariant::getLabel);
    }
}

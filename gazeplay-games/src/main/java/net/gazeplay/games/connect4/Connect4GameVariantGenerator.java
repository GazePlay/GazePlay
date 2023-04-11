package net.gazeplay.games.connect4;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class Connect4GameVariantGenerator extends EnumGameVariantGenerator<Connect4GameVariant> {

    public Connect4GameVariantGenerator() {
        super(Connect4GameVariant.values(), Connect4GameVariant::getLabel);
    }

}

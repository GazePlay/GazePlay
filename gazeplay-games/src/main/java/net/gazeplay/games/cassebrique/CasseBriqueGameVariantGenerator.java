package net.gazeplay.games.cassebrique;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "CasseBrique" proposed in the menu.
 */
public class CasseBriqueGameVariantGenerator extends EnumGameVariantGenerator<CasseBriqueGameVariant> {

    public CasseBriqueGameVariantGenerator() {
        super(CasseBriqueGameVariant.values(), CasseBriqueGameVariant::getLabel);
    }
}

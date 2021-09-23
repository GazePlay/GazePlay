package net.gazeplay.games.cassebrique;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class CasseBriqueGameVariantGenerator extends EnumGameVariantGenerator<CasseBriqueGameVariant> {

    public CasseBriqueGameVariantGenerator() {
        super(CasseBriqueGameVariant.values(), CasseBriqueGameVariant::getLabel);
    }
}

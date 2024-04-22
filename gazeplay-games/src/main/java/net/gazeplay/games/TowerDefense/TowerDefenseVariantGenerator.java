package net.gazeplay.games.TowerDefense;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class TowerDefenseVariantGenerator extends EnumGameVariantGenerator<TowerDefenseVariant> {

    public TowerDefenseVariantGenerator() {
        super(TowerDefenseVariant.values(),TowerDefenseVariant::getLabel);
    }

}

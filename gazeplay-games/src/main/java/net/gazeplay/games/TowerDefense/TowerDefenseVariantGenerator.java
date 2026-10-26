package net.gazeplay.games.TowerDefense;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants (maps) of the game "TowerDefense" proposed in the menu.
 */
public class TowerDefenseVariantGenerator extends EnumGameVariantGenerator<TowerDefenseVariant> {

    public TowerDefenseVariantGenerator() {
        super(TowerDefenseVariant.values(),TowerDefenseVariant::getLabel);
    }

}

package net.gazeplay.games.labyrinth;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Labyrinth" proposed in the menu.
 */
public class LabyrinthGameVariantGenerator extends EnumGameVariantGenerator<LabyrinthGameVariant> {

    public LabyrinthGameVariantGenerator() {
        super(LabyrinthGameVariant.values(), LabyrinthGameVariant::getLabel);
    }
}

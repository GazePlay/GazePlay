package net.gazeplay.games.labyrinth;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class LabyrinthGameVariantGenerator extends EnumGameVariantGenerator<LabyrinthGameVariant> {

    public LabyrinthGameVariantGenerator() {
        super(LabyrinthGameVariant.values(), LabyrinthGameVariant::getLabel);
    }
}

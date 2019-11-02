package net.gazeplay.games.labyrinth;

import net.gazeplay.GameSpec;

public class LabyrinthGameVariantGenerator extends GameSpec.EnumGameVariantGenerator<LabyrinthGameVariant> {

    public LabyrinthGameVariantGenerator() {
        super(LabyrinthGameVariant.values(), LabyrinthGameVariant::getLabel);
    }

}

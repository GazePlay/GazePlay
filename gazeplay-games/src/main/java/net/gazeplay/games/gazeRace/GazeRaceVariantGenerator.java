package net.gazeplay.games.gazeRace;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class GazeRaceVariantGenerator extends EnumGameVariantGenerator<GazeRaceVariant> {
    public GazeRaceVariantGenerator() {
        super(GazeRaceVariant.values(), GazeRaceVariant::getLabel);
    }
}

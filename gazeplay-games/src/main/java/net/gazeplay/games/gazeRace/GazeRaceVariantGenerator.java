package net.gazeplay.games.gazeRace;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "GazeRace" proposed in the menu.
 */
public class GazeRaceVariantGenerator extends EnumGameVariantGenerator<GazeRaceVariant> {
    public GazeRaceVariantGenerator() {
        super(GazeRaceVariant.values(), GazeRaceVariant::getLabel);
    }
}

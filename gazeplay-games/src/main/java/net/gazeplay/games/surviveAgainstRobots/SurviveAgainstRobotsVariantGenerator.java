package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "SurviveAgainstRobots" proposed in the menu.
 */
public class SurviveAgainstRobotsVariantGenerator extends EnumGameVariantGenerator<SurviveAgainstRobotsVariant> {
    public SurviveAgainstRobotsVariantGenerator() {
        super(SurviveAgainstRobotsVariant.values(), SurviveAgainstRobotsVariant::getLabel);
    }

}

package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "SurviveAgainstRobotsMouse" proposed in the menu.
 */
public class SurviveAgainstRobotsMouseVariantGenerator extends EnumGameVariantGenerator<SurviveAgainstRobotsMouseVariant> {
    public SurviveAgainstRobotsMouseVariantGenerator() {
        super(SurviveAgainstRobotsMouseVariant.values(), SurviveAgainstRobotsMouseVariant::getLabel);
    }
}

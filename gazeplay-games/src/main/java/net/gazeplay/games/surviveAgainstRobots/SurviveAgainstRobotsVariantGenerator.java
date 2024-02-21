package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class SurviveAgainstRobotsVariantGenerator extends EnumGameVariantGenerator<SurviveAgainstRobotsVariant> {
    public SurviveAgainstRobotsVariantGenerator() {
        super(SurviveAgainstRobotsVariant.values(), SurviveAgainstRobotsVariant::getLabel);
    }

}

package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class SurviveAgainstRobotsMouseVariantGenerator extends EnumGameVariantGenerator<SurviveAgainstRobotsMouseVariant> {
    public SurviveAgainstRobotsMouseVariantGenerator() {
        super(SurviveAgainstRobotsMouseVariant.values(), SurviveAgainstRobotsMouseVariant::getLabel);
    }
}

package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Survive Against Robots" (maintainer's rules of 3/11/2026, one card instead of two): when it
 * ends (buttons, see {@link SurviveEnd}), and at the bottom the level (see {@link SurviveLevel}) and how the Biboule
 * shoots (see {@link SurviveShot}).
 */
public class SurviveAgainstRobotsVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final SurviveLevel level : SurviveLevel.values()) {
            for (final SurviveShot shot : SurviveShot.values()) {
                for (final SurviveEnd end : SurviveEnd.values()) {
                    variants.add(new GroupedGameVariant(level.getKey(), shot.getKey(), end.getKey(), null));
                }
            }
        }
        return variants;
    }
}

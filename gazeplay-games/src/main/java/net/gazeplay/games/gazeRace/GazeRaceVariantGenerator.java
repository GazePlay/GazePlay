package net.gazeplay.games.gazeRace;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of GazeRace (maintainer's choices of 9/11/2026): when it ends (buttons, see {@link GazeRaceEnd}), and
 * at the bottom the way of the road (see {@link GazeRaceVariant}) and the level (see {@link GazeRaceLevel}).
 */
public class GazeRaceVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final GazeRaceVariant road : GazeRaceVariant.values()) {
            for (final GazeRaceLevel level : GazeRaceLevel.values()) {
                for (final GazeRaceEnd end : GazeRaceEnd.values()) {
                    variants.add(new GroupedGameVariant(road.getKey(), level.getKey(), end.getKey(), null));
                }
            }
        }
        return variants;
    }
}

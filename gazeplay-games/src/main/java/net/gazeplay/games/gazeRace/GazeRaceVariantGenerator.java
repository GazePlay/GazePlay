package net.gazeplay.games.gazeRace;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of GazeRace (maintainer's choices of 9/11/2026): when it ends (buttons, see {@link GazeRaceEnd}), and
 * at the bottom the way of the road (see {@link GazeRaceVariant}), the level (see {@link GazeRaceLevel}) and the hearts
 * (see {@link LivesVariant}, maintainer's choice of 17/11/2026: without hearts, the first hit loses the game).
 */
public class GazeRaceVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final GazeRaceVariant road : GazeRaceVariant.values()) {
            for (final GazeRaceLevel level : GazeRaceLevel.values()) {
                for (final String lives : LivesVariant.options(true)) {
                    for (final GazeRaceEnd end : GazeRaceEnd.values()) {
                        variants.add(new GroupedGameVariant(road.getKey(), level.getKey(), end.getKey(), null, lives));
                    }
                }
            }
        }
        return variants;
    }
}

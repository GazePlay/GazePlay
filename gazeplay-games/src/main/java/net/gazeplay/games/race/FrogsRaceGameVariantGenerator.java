package net.gazeplay.games.race;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "FrogsRace" (26/11/2026): the speed of the other frogs (buttons, see {@link RaceVariant}), and
 * at the bottom how a bug is caught (see {@link RaceCatch}). One group only: the window of the variants shows no switch
 * for it.
 */
public class FrogsRaceGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "FrogsRace";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final RaceCatch way : RaceCatch.values()) {
            for (final RaceVariant speed : RaceVariant.values()) {
                variants.add(new GroupedGameVariant(GROUP, way.getKey(), speed.getLabel(), null));
            }
        }
        return variants;
    }
}

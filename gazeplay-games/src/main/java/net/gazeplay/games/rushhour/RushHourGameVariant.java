package net.gazeplay.games.rushhour;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Embouteillage" (maintainer's choices of 9/11/2026): when it ends (buttons, see
 * {@link RushHourEnd}), and at the bottom the level (see {@link RushHourLevel}). One group only: the window of the
 * variants shows no switch for it.
 */
public class RushHourGameVariant implements IGameVariantGenerator {

    static final String GROUP = "RushHour";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final RushHourLevel level : RushHourLevel.values()) {
            for (final RushHourEnd end : RushHourEnd.values()) {
                variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}

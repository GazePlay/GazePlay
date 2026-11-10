package net.gazeplay.games.trainSwitches;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Aiguillages" (maintainer's choices of 10/11/2026): when it ends (buttons, see
 * {@link TrainSwitchesEnd}), and at the bottom the level (see {@link TrainSwitchesLevel}) and the rhythm of the trains
 * (see {@link TrainSwitchesRhythm}). One group only: the window of the variants shows no switch for it. (Before the
 * review: 12 variants, and a number of trains typed in the window of the variants.)
 */
public class TrainSwitchesGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "TrainSwitches";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final TrainSwitchesLevel level : TrainSwitchesLevel.values()) {
            for (final TrainSwitchesRhythm rhythm : TrainSwitchesRhythm.values()) {
                for (final TrainSwitchesEnd end : TrainSwitchesEnd.values()) {
                    variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null, rhythm.getKey()));
                }
            }
        }
        return variants;
    }
}

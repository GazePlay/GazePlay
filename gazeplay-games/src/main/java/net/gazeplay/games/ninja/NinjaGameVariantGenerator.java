package net.gazeplay.games.ninja;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Ninja" (maintainer's choices of 4/11/2026): the way of the target (buttons, see
 * {@link NinjaTrajectory}), and at the bottom what is cut (see {@link NinjaTarget}; "My images" plays with the images of
 * the user), the speed (see {@link NinjaSpeed}), how it is cut (see {@link NinjaCut}) and the end (see {@link NinjaEnd}).
 */
public class NinjaGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final NinjaTarget target : NinjaTarget.values()) {
            for (final NinjaSpeed speed : NinjaSpeed.values()) {
                for (final NinjaCut cut : NinjaCut.values()) {
                    for (final NinjaEnd end : NinjaEnd.values()) {
                        for (final NinjaTrajectory trajectory : NinjaTrajectory.values()) {
                            variants.add(new GroupedGameVariant(target.getKey(), speed.getKey(), trajectory.getKey(),
                                target == NinjaTarget.MY_IMAGES ? NinjaTarget.USER_IMAGES_FOLDER : null,
                                cut.getKey(), end.getKey()));
                        }
                    }
                }
            }
        }
        return variants;
    }
}

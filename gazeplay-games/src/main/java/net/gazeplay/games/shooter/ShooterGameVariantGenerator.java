package net.gazeplay.games.shooter;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of the shooting game (maintainer's decisions of 2/11/2026): when it ends (buttons, see
 * {@link ShooterEnd}), and at the bottom how the targets are shot (see {@link ShooterShot}) and the scene (see
 * {@link ShooterTheme}).
 */
public class ShooterGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final ShooterShot shot : ShooterShot.values()) {
            for (final ShooterTheme theme : ShooterTheme.values()) {
                for (final ShooterEnd end : ShooterEnd.values()) {
                    variants.add(new GroupedGameVariant(shot.getKey(), theme.getKey(), end.getKey(), null));
                }
            }
        }
        return variants;
    }
}

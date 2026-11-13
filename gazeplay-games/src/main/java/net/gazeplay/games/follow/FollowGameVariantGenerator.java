package net.gazeplay.games.follow;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Suivre le chemin" (13/11/2026): the game at the top (see {@link FollowMode}), the level as
 * buttons (see {@link FollowLevel}), and the end at the bottom (see {@link FollowEnd}).
 */
public class FollowGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final FollowMode mode : FollowMode.values()) {
            for (final FollowEnd end : FollowEnd.values()) {
                for (final FollowLevel level : FollowLevel.values()) {
                    variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), level.getKey(), null));
                }
            }
        }
        return variants;
    }
}

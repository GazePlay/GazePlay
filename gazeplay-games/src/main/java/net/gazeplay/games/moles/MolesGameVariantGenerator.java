package net.gazeplay.games.moles;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of the moles game (maintainer's choices of 4/11/2026): the end (buttons, see {@link MolesEnd}), and
 * at the bottom what comes out (see {@link MolesTarget}; "My images" plays with the images of the user), the level (see
 * {@link MolesLevel}) and how it is hit (see {@link MolesHit}).
 */
public class MolesGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final MolesTarget target : MolesTarget.values()) {
            for (final MolesLevel level : MolesLevel.values()) {
                for (final MolesHit hit : MolesHit.values()) {
                    for (final MolesEnd end : MolesEnd.values()) {
                        variants.add(new GroupedGameVariant(target.getKey(), level.getKey(), end.getKey(),
                            target == MolesTarget.MY_IMAGES ? MolesTarget.USER_IMAGES_FOLDER : null, hit.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}

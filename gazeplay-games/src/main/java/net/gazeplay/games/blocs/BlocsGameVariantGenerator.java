package net.gazeplay.games.blocs;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Blocs" (maintainer's choices of 13/11/2026): how a block vanishes at the top (see
 * {@link BlocsMode}), the level as buttons (see {@link BlocsLevel}), and at the bottom the end (see {@link BlocsEnd})
 * and the pictures (see {@link BlocsPictures}; "My images" plays with the images of the user).
 */
public class BlocsGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final BlocsMode mode : BlocsMode.values()) {
            for (final BlocsEnd end : BlocsEnd.values()) {
                for (final BlocsPictures pictures : BlocsPictures.values()) {
                    for (final BlocsLevel level : BlocsLevel.values()) {
                        variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), level.getKey(),
                            pictures == BlocsPictures.MY_IMAGES ? BlocsPictures.USER_IMAGES_FOLDER : null,
                            pictures.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}

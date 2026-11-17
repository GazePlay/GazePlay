package net.gazeplay.games.ladder;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Échelle" (13/11/2026): the mode (see {@link LadderMode}, a single one since 17/11/2026, so no
 * switch), the level as buttons (see {@link LadderLevel}), and at the bottom the end (see {@link LadderEnd}), the
 * pictures (see {@link LadderPictures}; "My images" plays with the images of the user) and the hearts (see
 * {@link LivesVariant}), with every end, without end too (17/11/2026).
 */
public class LadderGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final LadderMode mode : LadderMode.values()) {
            for (final LadderEnd end : LadderEnd.values()) {
                for (final LadderPictures pictures : LadderPictures.values()) {
                    for (final String lives : LivesVariant.options(true)) {
                        for (final LadderLevel level : LadderLevel.values()) {
                            variants.add(new GroupedGameVariant(mode.getKey(), end.getKey(), level.getKey(),
                                pictures == LadderPictures.MY_IMAGES ? LadderPictures.USER_IMAGES_FOLDER : null,
                                pictures.getKey(), lives));
                        }
                    }
                }
            }
        }
        return variants;
    }
}

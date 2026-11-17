package net.gazeplay.games.cassebrique;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Casse-brique" (maintainer's rules of 3/11/2026): the drawing of the bricks (buttons, see
 * {@link CasseBriqueMap}), and at the bottom what is behind them (see {@link CasseBriqueBackground}; "My images" plays
 * with the images of the user), the level (see {@link CasseBriqueLevel}) and the hearts (see {@link LivesVariant},
 * maintainer's choice of 17/11/2026).
 */
public class CasseBriqueGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final CasseBriqueBackground background : CasseBriqueBackground.values()) {
            for (final CasseBriqueLevel level : CasseBriqueLevel.values()) {
                for (final String lives : LivesVariant.options(true)) {
                    for (final CasseBriqueMap map : CasseBriqueMap.values()) {
                        variants.add(new GroupedGameVariant(background.getKey(), level.getKey(), map.getKey(),
                            background == CasseBriqueBackground.MY_IMAGES ? CasseBriqueBackground.USER_IMAGES_FOLDER
                                : null, lives));
                    }
                }
            }
        }
        return variants;
    }
}

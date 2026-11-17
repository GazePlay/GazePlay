package net.gazeplay.games.order;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Ordre" (maintainer's choices of 10/11/2026): when it ends (buttons, see {@link OrderEnd}), and
 * at the bottom the level (see {@link OrderLevel}) and the hearts (see {@link LivesVariant}, with every end,
 * maintainer's choice of 17/11/2026). One group only: the window of the variants shows no switch for it. (Before the
 * review: 3, 5 or 7 targets, under a title in English.)
 */
public class OrdersGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "Order";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final OrderLevel level : OrderLevel.values()) {
            for (final String lives : LivesVariant.options(true)) {
                for (final OrderEnd end : OrderEnd.values()) {
                    variants.add(new GroupedGameVariant(GROUP, level.getKey(), end.getKey(), null, lives));
                }
            }
        }
        return variants;
    }
}

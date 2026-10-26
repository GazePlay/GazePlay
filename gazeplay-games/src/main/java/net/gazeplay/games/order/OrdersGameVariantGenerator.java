package net.gazeplay.games.order;

import net.gazeplay.commons.gamevariants.generators.IntListVariantGenerator;

/**
 * Gives the variants of the game "Order" proposed in the menu.
 */
public class OrdersGameVariantGenerator extends IntListVariantGenerator {

    public OrdersGameVariantGenerator() {
        super("Choose number of targets", 3, 5, 7);
    }

}

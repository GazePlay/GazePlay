package net.gazeplay.games.order;

import net.gazeplay.GameSpec;

public class OrdersGameVariantGenerator extends GameSpec.IntListVariantGenerator {

    public OrdersGameVariantGenerator() {
        super("Choose number of targets", 3, 5, 7);
    }

}

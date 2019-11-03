package net.gazeplay.games.order;

import net.gazeplay.GameSpec;

public class OrdersGameVariantGenerator extends GameSpec.IntListVariantGenerator {

    private static final String TARGETS = "targets";

    public OrdersGameVariantGenerator() {
        super(TARGETS, 3, 5, 7);
    }

}

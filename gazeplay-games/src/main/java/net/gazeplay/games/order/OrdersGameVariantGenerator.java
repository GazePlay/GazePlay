package net.gazeplay.games.order;

import net.gazeplay.commons.gamevariants.generators.IntListVariantGenerator;

public class OrdersGameVariantGenerator extends IntListVariantGenerator {

    public OrdersGameVariantGenerator() {
        super("Choose number of targets", 3, 5, 7);
    }

}

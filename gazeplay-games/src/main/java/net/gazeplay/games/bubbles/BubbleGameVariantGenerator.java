package net.gazeplay.games.bubbles;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class BubbleGameVariantGenerator extends EnumGameVariantGenerator<BubblesGameVariant> {

    public BubbleGameVariantGenerator() {
        super(BubblesGameVariant.values(), BubblesGameVariant::getDirection);
    }
}

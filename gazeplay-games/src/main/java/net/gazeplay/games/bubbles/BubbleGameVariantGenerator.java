package net.gazeplay.games.bubbles;

import net.gazeplay.GameSpec;

public class BubbleGameVariantGenerator extends GameSpec.EnumGameVariantGenerator<BubblesGameVariant> {
    public BubbleGameVariantGenerator() {
        super(BubblesGameVariant.values(), BubblesGameVariant::getDirection);
    }
}

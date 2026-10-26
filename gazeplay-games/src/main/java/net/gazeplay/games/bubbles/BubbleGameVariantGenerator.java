package net.gazeplay.games.bubbles;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the games "ColoredBubbles" and "PortraitBubbles" proposed in the menu.
 */
public class BubbleGameVariantGenerator extends EnumGameVariantGenerator<BubblesGameVariant> {

    public BubbleGameVariantGenerator() {
        super(BubblesGameVariant.values(), BubblesGameVariant::getDirection);
    }
}

package net.gazeplay.games.magicpotions;

import javafx.scene.paint.Color;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

public enum PotionMix {
    RED_Potion("Red", Color.RED),
    YELLOW_Potion("Yellow", Color.YELLOW),
    BLUE_Potion("Blue", Color.BLUE),
    ORANGE_Potion("Orange", Color.ORANGE),
    GREEN_Potion("Green", Color.GREEN),
    PURPLE_Potion("Purple", Color.PURPLE),
    BLACK_Potion("Black", Color.BLACK);

    @Getter
    private final String colorName;

    @Getter
    private final Color color;

    PotionMix(final String name, final Color color) {
        this.colorName = name;
        this.color = color;
    }

    public static PotionMix getRandomPotionRequest(ReplayablePseudoRandom randomGenerator) {
        return values()[randomGenerator.nextInt(values().length)];
    }
}

package net.gazeplay.commons.utils;

import javafx.scene.paint.Color;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

/**
 * Gives random colors (confetti of the win animation).
 */
public class RandomColorGenerator {

    @Getter
    private static final RandomColorGenerator instance = new RandomColorGenerator();

    private final ReplayablePseudoRandom random = new ReplayablePseudoRandom();

    /**
     * @return an opaque color drawn at random
     */
    public Color randomColor() {
        return Color.rgb(randomShort(), randomShort(), randomShort());
    }

    private int randomShort() {
        return random.nextInt(255);
    }

}

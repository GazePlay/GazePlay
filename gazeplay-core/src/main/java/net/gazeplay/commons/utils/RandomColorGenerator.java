package net.gazeplay.commons.utils;

import javafx.scene.paint.Color;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

public class RandomColorGenerator {

    @Getter
    private static final RandomColorGenerator instance = new RandomColorGenerator();

    private final ReplayablePseudoRandom random = new ReplayablePseudoRandom();

    public Color randomColor() {
        return Color.rgb(randomShort(), randomShort(), randomShort());
    }

    private int randomShort() {
        return random.nextInt(255);
    }

}

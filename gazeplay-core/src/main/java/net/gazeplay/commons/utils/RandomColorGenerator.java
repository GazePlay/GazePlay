package net.gazeplay.commons.utils;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.Random;

public class RandomColorGenerator {

    @Getter
    private static final RandomColorGenerator instance = new RandomColorGenerator();

    private final Random random = new Random();

    public Color randomColor() {
        return Color.rgb(randomShort(), randomShort(), randomShort());
    }

    private int randomShort() {
        return random.nextInt(255);
    }

}

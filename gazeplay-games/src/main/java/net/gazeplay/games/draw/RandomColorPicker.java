package net.gazeplay.games.draw;

import javafx.scene.paint.Color;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

public class RandomColorPicker implements ColorPicker {

    private final ReplayablePseudoRandom randomColorPicker;

    public RandomColorPicker(ReplayablePseudoRandom randomGenerator) {
        randomColorPicker = randomGenerator;
    }

    @Override
    public Color pickColor() {
        return pickRandomColor();
    }

    private Color pickRandomColor() {
        int r = randomColorPicker.nextInt(255);
        int g = randomColorPicker.nextInt(255);
        int b = randomColorPicker.nextInt(255);
        return Color.rgb(r, g, b);
    }
}

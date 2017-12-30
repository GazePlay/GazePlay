package net.gazeplay.games.draw;

import javafx.scene.paint.Color;

import java.util.Random;

public class RandomColorPicker implements ColorPicker {

    private final Random randomColorPicker = new Random();

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

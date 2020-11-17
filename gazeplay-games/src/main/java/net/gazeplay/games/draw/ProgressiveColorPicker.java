package net.gazeplay.games.draw;

import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

@Slf4j
public class ProgressiveColorPicker implements ColorPicker {

    private final RandomColorPicker randomColorPicker;

    private Color currentColor;

    private static final int MAX_COLOR_VALUE = 255;

    /**
     * should be a prime number ?
     */
    private static final int INCREMENT = 13;

    public ProgressiveColorPicker(ReplayablePseudoRandom randomGenerator) {
        randomColorPicker = new RandomColorPicker(randomGenerator);
        currentColor = randomColorPicker.pickColor();
    }

    @Override
    public Color pickColor() {
        double r = currentColor.getRed() * MAX_COLOR_VALUE;
        double g = currentColor.getGreen() * MAX_COLOR_VALUE;
        double b = currentColor.getBlue() * MAX_COLOR_VALUE;

        log.debug("color = {} {} {}", r, g, b);

        r += INCREMENT;
        if (r > MAX_COLOR_VALUE) {
            r = 0;
            g += INCREMENT;
        }
        if (g > MAX_COLOR_VALUE) {
            g = 0;
            b += INCREMENT;
        }
        if (b > MAX_COLOR_VALUE) {
            b = 0;
        }
        Color color = Color.rgb((int) r, (int) g, (int) b);
        log.debug("color = {}", color);
        currentColor = color;
        return color;
    }
}

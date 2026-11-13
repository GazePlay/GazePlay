package net.gazeplay.games.bottle;

import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * The colors of the bottles of "Bouteille", each with a symbol on its label (round, star, heart, triangle), so that
 * they are told apart also with a color blindness.
 */
@Getter
public enum BottleColor {
    RED("BottleBreakRed", Color.rgb(215, 55, 60)),
    BLUE("BottleBreakBlue", Color.rgb(55, 110, 215)),
    GREEN("BottleBreakGreen", Color.rgb(45, 160, 85)),
    YELLOW("BottleBreakYellow", Color.rgb(240, 190, 30));

    /**
     * Key of the sentence asking to break the bottles of this color.
     */
    private final String key;

    private final Color color;

    BottleColor(final String key, final Color color) {
        this.key = key;
        this.color = color;
    }
}

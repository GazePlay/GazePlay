package net.gazeplay.games.magicpotions;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

/**
 * The potions a client of "Magic Potions" can ask for, and their recipes with the three potions of the table: red,
 * yellow and blue alone; orange (red and yellow), green (yellow and blue), purple (red and blue); brown (the three,
 * as with paints; it was black before the review of 10/11/2026).
 */
@Getter
public enum PotionMix {
    RED("PotionsRed", Color.rgb(226, 58, 58), Primary.RED),
    YELLOW("PotionsYellow", Color.rgb(250, 206, 48), Primary.YELLOW),
    BLUE("PotionsBlue", Color.rgb(56, 118, 222), Primary.BLUE),
    ORANGE("PotionsOrange", Color.rgb(246, 140, 38), Primary.RED, Primary.YELLOW),
    GREEN("PotionsGreen", Color.rgb(58, 168, 78), Primary.YELLOW, Primary.BLUE),
    PURPLE("PotionsPurple", Color.rgb(150, 78, 200), Primary.RED, Primary.BLUE),
    BROWN("PotionsBrown", Color.rgb(140, 92, 52), Primary.RED, Primary.YELLOW, Primary.BLUE);

    /**
     * The three potions on the table.
     */
    public enum Primary {
        RED, YELLOW, BLUE
    }

    /**
     * Key of translation of the name of the color.
     */
    private final String key;
    private final Color color;
    private final Set<Primary> recipe;

    PotionMix(final String key, final Color color, final Primary first, final Primary... others) {
        this.key = key;
        this.color = color;
        this.recipe = EnumSet.of(first, others);
    }

    /**
     * @return the potion made by a mixture of potions of the table, null for an empty one
     */
    public static PotionMix of(final Set<Primary> mixture) {
        for (final PotionMix mix : values()) {
            if (mix.recipe.equals(mixture)) {
                return mix;
            }
        }
        return null;
    }
}

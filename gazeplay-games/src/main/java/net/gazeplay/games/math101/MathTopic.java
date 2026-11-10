package net.gazeplay.games.math101;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.List;

/**
 * What "Apprends les Maths" asks (maintainer's choices of 10/11/2026; before, 5 games of the menu): additions,
 * subtractions, both, multiplications, divisions, both, or all the operations. The additions and subtractions are
 * played up to 10, 20 or 100, the multiplications and divisions by tables, all the operations at three levels (see
 * {@link MathLevel}).
 */
@Getter
public enum MathTopic {
    ADDITION("MathAddition", Family.ADD, MathOperation.PLUS),
    SUBTRACTION("MathSubtraction", Family.ADD, MathOperation.MINUS),
    ADD_SUB("MathAddSub", Family.ADD, MathOperation.PLUS, MathOperation.MINUS),
    MULTIPLICATION("MathMultiplication", Family.MULTIPLY, MathOperation.MULTIPLY),
    DIVISION("MathDivision", Family.MULTIPLY, MathOperation.DIVID),
    MULT_DIV("MathMultDiv", Family.MULTIPLY, MathOperation.MULTIPLY, MathOperation.DIVID),
    ALL("MathAll", Family.ALL, MathOperation.values());

    private enum Family { ADD, MULTIPLY, ALL }

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    private final Family family;

    private final List<MathOperation> operations;

    MathTopic(final String key, final Family family, final MathOperation... operations) {
        this.key = key;
        this.family = family;
        this.operations = List.of(operations);
    }

    /**
     * @return the levels of this topic, in the order of the buttons of the variants
     */
    public List<MathLevel> levels() {
        return switch (family) {
            case ADD -> List.of(MathLevel.UP_TO_10, MathLevel.UP_TO_20, MathLevel.UP_TO_100);
            case MULTIPLY -> List.of(MathLevel.TABLE_2, MathLevel.TABLE_3, MathLevel.TABLE_4, MathLevel.TABLE_5,
                MathLevel.TABLE_6, MathLevel.TABLE_7, MathLevel.TABLE_8, MathLevel.TABLE_9, MathLevel.TABLE_10,
                MathLevel.TABLES_2_TO_5, MathLevel.TABLES_ALL);
            case ALL -> List.of(MathLevel.ALL_EASY, MathLevel.ALL_MEDIUM, MathLevel.ALL_HARD);
        };
    }

    /**
     * @return the color of the background: blue for the additions and subtractions, green for the multiplications and
     * divisions, pink for all the operations (the colors of the former games)
     */
    public Color background() {
        return switch (family) {
            case ADD -> Color.rgb(226, 232, 255);
            case MULTIPLY -> Color.rgb(224, 246, 228);
            case ALL -> Color.rgb(255, 230, 232);
        };
    }

    /**
     * @return the topic of a key, the additions for an unknown one
     */
    public static MathTopic of(final String key) {
        for (final MathTopic topic : values()) {
            if (topic.key.equals(key)) {
                return topic;
            }
        }
        return ADDITION;
    }
}

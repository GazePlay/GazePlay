package net.gazeplay.components;

import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * A {@link DiceRoller} shown in its own 3D sub-scene, placed in the game as an ordinary 2D node, its origin at the
 * center of the die (as the die itself before).
 * <p>
 * Tester's feedback of 24/11/2026 (point 58): the 3D die, put directly in the 2D scene of the game, came in front of
 * the menu Esc when the mouse moved over it (JavaFX redraws only the part of the screen that changes, and a 3D node
 * there was not kept behind what is over it). Drawn in a {@link SubScene}, the way JavaFX mixes 3D and 2D, the die is
 * an image in the order of the nodes, under the menu. Used by Dice, Goose game and Horses.
 */
public class DiceView extends Group {

    /**
     * Room around the die, which shows up to its diagonal when it turns (√3 ≈ 1.73 times its side).
     */
    static final double ROOM = 1.8;

    @Getter
    private final DiceRoller die;

    /**
     * The side of the square sub-scene, larger than the die.
     */
    @Getter
    private final double size;

    /**
     * @param die  the die, its mesh centered on (0, 0, 0)
     * @param side the side of the die
     */
    public DiceView(final DiceRoller die, final double side) {
        this.die = die;
        this.size = side * ROOM;
        die.setTranslateX(size / 2);
        die.setTranslateY(size / 2);
        final SubScene scene = new SubScene(new Group(die), size, size, true, SceneAntialiasing.DISABLED);
        scene.setFill(Color.TRANSPARENT);
        // The origin of this group at the center of the die
        scene.setLayoutX(-size / 2);
        scene.setLayoutY(-size / 2);
        getChildren().add(scene);
    }
}

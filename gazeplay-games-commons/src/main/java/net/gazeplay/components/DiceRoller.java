package net.gazeplay.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.soundsmanager.SoundManager;
import java.util.ArrayList;

/**
 * A 3D die, textured with the six faces of data/common/images/dice.png, which rolls and tells its result.
 */
@Slf4j
public class DiceRoller extends MeshView {

    private final ReplayablePseudoRandom random;
    private final ArrayList<Rotate> rotations;
    private final SoundManager soundManager;

    /**
     * @param dieWidth        half of the side of the die
     * @param soundManager    plays the sound of the roll
     * @param randomGenerator the random generator of the game, for the replay
     */
    public DiceRoller(final float dieWidth, SoundManager soundManager, ReplayablePseudoRandom randomGenerator) {
        super();
        this.soundManager = soundManager;
        random = randomGenerator;
        rotations = new ArrayList<>();

        final TriangleMesh mesh = new TriangleMesh();
        final float[] points = {-dieWidth, dieWidth, -dieWidth, dieWidth, dieWidth, -dieWidth, dieWidth, -dieWidth,
            -dieWidth, -dieWidth, -dieWidth, -dieWidth, -dieWidth, dieWidth, dieWidth, dieWidth, dieWidth, dieWidth,
            dieWidth, -dieWidth, dieWidth, -dieWidth, -dieWidth, dieWidth};
        mesh.getPoints().addAll(points);
        final float[] texCoords = {0.333f, 0, 0.666f, 0, 0, 0.333f, 0.333f, 0.333f, 0.666f, 0.333f, 1, 0.333f, 0, 0.666f,
            0.333f, 0.666f, 0.666f, 0.666f, 1, 0.666f, 0.333f, 1, 0.666f, 1, 1, 1};
        mesh.getTexCoords().addAll(texCoords);
        final int[] faces = {
            // front
            0, 3, 1, 4, 2, 8, 0, 3, 2, 8, 3, 7,
            // right
            1, 4, 5, 5, 6, 9, 1, 4, 6, 9, 2, 8,
            // up
            4, 0, 5, 1, 1, 4, 4, 0, 1, 4, 0, 3,
            // left
            4, 2, 0, 3, 3, 7, 4, 2, 3, 7, 7, 6,
            // back
            5, 8, 4, 9, 7, 12, 5, 8, 7, 12, 6, 11,
            // down
            3, 7, 2, 8, 6, 11, 3, 7, 6, 11, 7, 10};
        mesh.getFaces().addAll(faces);

        setMesh(mesh);

        final PhongMaterial mat = new PhongMaterial();
        mat.setSelfIlluminationMap(new Image("data/common/images/dice.png"));
        mat.setDiffuseColor(Color.BLACK);
        mat.setSpecularMap(new Image("data/common/images/dice.png"));
        setMaterial(mat);

        rotations.add(new Rotate(0, Rotate.X_AXIS));
        rotations.add(new Rotate(0, Rotate.Y_AXIS));
        getTransforms().addAll(rotations.get(0), rotations.get(1));
    }

    /**
     * Rolls the die: it turns for one second, then settles on a face, which is the result.
     *
     * @param onFinishedEventHandler called when the die has settled, may be null
     * @return the face shown at the end of the animation, from 1 to 6 (known before the animation ends)
     */
    public int roll(final EventHandler<ActionEvent> onFinishedEventHandler) {
        // Resetting angles to 0
        rotations.get(0).setAngle(0);
        rotations.get(1).setAngle(0);

        // The face first, each with a chance of 1 in 6 (30/10/2026: the face used to come from random angles rounded
        // to a quarter turn, which gave 3 and 4 a chance of 1 in 4 each, and the other faces 1 in 8), then quarter turns
        // showing it
        final int face = 1 + random.nextInt(6);
        final int[] quarterTurns = quarterTurnsShowing(face, random.nextInt(4));

        // 2 full turns, the quarter turns, and a little more or less, on each axis; then the die squares up
        final int angleX = 720 + quarterTurns[0] * 90;
        final int angleY = 720 + quarterTurns[1] * 90;
        final int jitterX = random.nextInt(81) - 40;
        final int jitterY = random.nextInt(81) - 40;

        final Timeline rollTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
            new KeyValue(rotations.get(0).angleProperty(), angleX + jitterX, Interpolator.SPLINE(0.2, 0.6, 0.4, 0.9)),
            new KeyValue(rotations.get(1).angleProperty(), angleY + jitterY, Interpolator.SPLINE(0.2, 0.6, 0.4, 0.9))));

        final Timeline squareUpTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
            new KeyValue(rotations.get(0).angleProperty(), angleX, Interpolator.EASE_OUT),
            new KeyValue(rotations.get(1).angleProperty(), angleY, Interpolator.EASE_OUT)));

        rollTimeline.setOnFinished(e -> squareUpTimeline.play());

        if (onFinishedEventHandler != null) {
            squareUpTimeline.setOnFinished(onFinishedEventHandler);
        }

        rollTimeline.play();
        soundManager.add("data/common/sounds/diceRollSound.wav");
        return face;
    }

    /**
     * @return the face shown by the die turned by these angles (multiples of 90 degrees, from 0 to 270) around X then Y
     */
    static int faceShown(final int angleX, final int angleY) {
        if (angleX == 90) {
            return 3;
        } else if (angleX == 270) {
            return 4;
        } else if (angleX == angleY) {
            return 1;
        } else if ((angleX == 180 && angleY == 0) || (angleX == 0 && angleY == 180)) {
            return 6;
        } else if ((angleX == 0 && angleY == 270) || (angleX == 180 && angleY == 90)) {
            return 2;
        } else {
            return 5;
        }
    }

    /**
     * @param face    the face to show, from 1 to 6
     * @param variety a number from 0 to 3 choosing among the ways of showing it
     * @return the quarter turns (from 0 to 3) around X and around Y showing the face (see {@link #faceShown})
     */
    static int[] quarterTurnsShowing(final int face, final int variety) {
        final int half = (variety % 2) * 2;
        return switch (face) {
            case 1 -> new int[]{half, half};
            case 2 -> new int[]{half, 3 - half};
            case 3 -> new int[]{1, variety};
            case 4 -> new int[]{3, variety};
            case 5 -> new int[]{half, 1 + half};
            case 6 -> new int[]{half, 2 - half};
            default -> throw new IllegalArgumentException("No face " + face);
        };
    }
}

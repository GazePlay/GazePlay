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
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class DiceRoller extends MeshView {

    private final Random random;
    private final ArrayList<Rotate> rotations;

    public DiceRoller(final float dieWidth) {
        super();
        random = new Random();
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

    public int roll(final EventHandler<ActionEvent> onFinishedEventHandler) {
        // Resetting angles to 0
        rotations.get(0).setAngle(0);
        rotations.get(1).setAngle(0);

        // Between 2 and 3 turns plus 1 partial turn
        int angleX = (2 + random.nextInt(1)) * 360 + random.nextInt(360);
        int angleY = (2 + random.nextInt(1)) * 360 + random.nextInt(360);

        final Timeline rollTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
            new KeyValue(rotations.get(0).angleProperty(), angleX, Interpolator.SPLINE(0.2, 0.6, 0.4, 0.9)),
            new KeyValue(rotations.get(1).angleProperty(), angleY, Interpolator.SPLINE(0.2, 0.6, 0.4, 0.9))));

        final int modX = angleX % 90;
        final int modY = angleY % 90;
        angleX = angleX - modX + (modX > 45 ? 1 : 0) * 90;
        angleY = angleY - modY + (modY > 45 ? 1 : 0) * 90;

        final Timeline squareUpTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
            new KeyValue(rotations.get(0).angleProperty(), angleX, Interpolator.EASE_OUT),
            new KeyValue(rotations.get(1).angleProperty(), angleY, Interpolator.EASE_OUT)));

        rollTimeline.setOnFinished(e -> squareUpTimeline.play());

        if (onFinishedEventHandler != null) {
            squareUpTimeline.setOnFinished(onFinishedEventHandler);
        }

        // Compute outcome of roll
        final int finalX = angleX % 360;
        final int finalY = angleY % 360;

        rollTimeline.play();
        try {
            ForegroundSoundsUtils.playSound("data/common/sounds/diceRollSound.wav");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (finalX == 90) {
            return 3;
        } else if (finalX == 270) {
            return 4;
        } else if (finalX == finalY) {
            return 1;
        } else if ((finalX == 180 && finalY == 0) || (finalX == 0 && finalY == 180)) {
            return 6;
        } else if ((finalX == 0 && finalY == 270) || (finalX == 180 && finalY == 90)) {
            return 2;
        } else {
            return 5;
        }
    }
}

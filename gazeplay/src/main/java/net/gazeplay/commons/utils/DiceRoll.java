package net.gazeplay.commons.utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Slf4j
public class DiceRoll extends GridPane {

    private Random random;
    private HashMap<MeshView, ArrayList<Rotate>> rotations;

    public DiceRoll(GameContext gc, int nbDice) {
        super();
        random = new Random();
        rotations = new HashMap<>();

        Dimension2D dimensions = gc.getGamePanelDimensionProvider().getDimension2D();

        setMinSize(dimensions.getWidth(), dimensions.getHeight());
        setHgap((dimensions.getWidth() / nbDice) * 0.4);
        setAlignment(Pos.CENTER);

        float dieWidth = (float) ((dimensions.getWidth() / nbDice) * 0.6) / 2;
        if (dieWidth > dimensions.getHeight() / 4) {
            dieWidth = (float) (dimensions.getHeight() / 4);
        }

        for (int i = 0; i < nbDice; i++) {
            TriangleMesh mesh = new TriangleMesh();
            float[] points = {-dieWidth, dieWidth, -dieWidth, dieWidth, dieWidth, -dieWidth, dieWidth, -dieWidth,
                    -dieWidth, -dieWidth, -dieWidth, -dieWidth, -dieWidth, dieWidth, dieWidth, dieWidth, dieWidth,
                    dieWidth, dieWidth, -dieWidth, dieWidth, -dieWidth, -dieWidth, dieWidth};
            mesh.getPoints().addAll(points);
            float[] texCoords = {0.333f, 0, 0.666f, 0, 0, 0.333f, 0.333f, 0.333f, 0.666f, 0.333f, 1, 0.333f, 0, 0.666f,
                    0.333f, 0.666f, 0.666f, 0.666f, 1, 0.666f, 0.333f, 1, 0.666f, 1, 1, 1};
            mesh.getTexCoords().addAll(texCoords);
            int[] faces = {
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
                    3, 7, 2, 8, 6, 11, 3, 7, 6, 11, 7, 10 };
            mesh.getFaces().addAll(faces);

            MeshView die = new MeshView(mesh);
            PhongMaterial mat = new PhongMaterial();
            mat.setSelfIlluminationMap(new Image("data/common/images/dice.png"));
            mat.setDiffuseMap(new Image("data/common/images/dice.png"));
            mat.setSpecularMap(new Image("data/common/images/dice.png"));
            die.setMaterial(mat);
            add(die, i, 0);

            ArrayList<Rotate> rotates = new ArrayList<>();
            rotates.add(new Rotate(0, Rotate.X_AXIS));
            rotates.add(new Rotate(0, Rotate.Y_AXIS));
            die.getTransforms().addAll(rotates.get(0), rotates.get(1));
            rotations.put(die, rotates);
        }
    }

    public int[] roll(EventHandler<ActionEvent> onFinishedEventHandler) {
        int[] outcomes = new int[getChildren().size()];
        int i = 0;
        for (Node node : getChildren()) {
            MeshView die = (MeshView) node;
            ArrayList<Rotate> rotates = rotations.get(die);

            // Resetting angles to 0
            rotates.get(0).setAngle(0);
            rotates.get(1).setAngle(0);

            // Between 5 and 10 turns plus 1 partial turn
            int angleX = (5 + random.nextInt(5)) * 360 + random.nextInt(360);
            int angleY = (5 + random.nextInt(5)) * 360 + random.nextInt(360);

            Timeline rollTimeline = new Timeline(new KeyFrame(Duration.seconds(3),
                    new KeyValue(rotates.get(0).angleProperty(), angleX, Interpolator.EASE_OUT),
                    new KeyValue(rotates.get(1).angleProperty(), angleY, Interpolator.EASE_OUT)));

            int modX = angleX % 90;
            int modY = angleY % 90;
            angleX = angleX - modX + (modX > 45 ? 1 : 0) * 90;
            angleY = angleY - modY + (modY > 45 ? 1 : 0) * 90;

            Timeline squareUpTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                    new KeyValue(rotates.get(0).angleProperty(), angleX, Interpolator.EASE_OUT),
                    new KeyValue(rotates.get(1).angleProperty(), angleY, Interpolator.EASE_OUT)));

            rollTimeline.setOnFinished(e -> {
                squareUpTimeline.play();
            });

            if (i == 0) {
                squareUpTimeline.setOnFinished(onFinishedEventHandler);
            }

            // Compute outcome of roll
            int finalX = angleX % 360;
            int finalY = angleY % 360;

            if (finalX == 90) {
                outcomes[i] = 3;
            } else if (finalX == 270) {
                outcomes[i] = 4;
            } else if (finalX == finalY) {
                outcomes[i] = 1;
            } else if ((finalX == 180 && finalY == 0) || (finalX == 0 && finalY == 180)) {
                outcomes[i] = 6;
            } else if ((finalX == 0 && finalY == 270) || (finalX == 180 && finalY == 90)) {
                outcomes[i] = 2;
            } else {
                outcomes[i] = 5;
            }

            rollTimeline.play();
            i++;
        }

        return outcomes;
    }
}
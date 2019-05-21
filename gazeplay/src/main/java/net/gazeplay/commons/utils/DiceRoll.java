package net.gazeplay.commons.utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.RotateEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;

import java.util.Random;

@Slf4j
public class DiceRoll extends GridPane {

    private static final int NBFACES = 6;
    private GameContext gc;
    private int nbDice;
    Random random;

    public DiceRoll(GameContext gc, int nbDice) {
        super();
        this.gc = gc;
        this.nbDice = nbDice;
        random = new Random();

        Dimension2D dimensions = gc.getGamePanelDimensionProvider().getDimension2D();

        setMinSize(dimensions.getWidth(), dimensions.getHeight());
        setHgap((dimensions.getWidth() / nbDice) * 0.4);
        setAlignment(Pos.CENTER);

        float dieWidth = (float)((dimensions.getWidth() / nbDice) * 0.6)/2;

        for(int i = 0; i < nbDice; i++){
            TriangleMesh mesh = new TriangleMesh();
            float points[] = {
                -dieWidth, dieWidth, -dieWidth,
                dieWidth, dieWidth, -dieWidth,
                dieWidth, -dieWidth, -dieWidth,
                -dieWidth, -dieWidth, -dieWidth,
                -dieWidth, dieWidth, dieWidth,
                dieWidth, dieWidth, dieWidth,
                dieWidth, -dieWidth, dieWidth,
                -dieWidth, -dieWidth, dieWidth
            };
            mesh.getPoints().addAll(points);
            float texCoords[] = {
                    0.333f, 0,
                    0.666f, 0,
                    0, 0.333f,
                    0.333f, 0.333f,
                    0.666f, 0.333f,
                    1, 0.333f,
                    0, 0.666f,
                    0.333f, 0.666f,
                    0.666f, 0.666f,
                    1, 0.666f,
                    0.333f, 1,
                    0.666f, 1,
                    1, 1
            };
            mesh.getTexCoords().addAll(texCoords);
            int faces[] = {
                //front
                0, 3, 1, 4, 2, 8,
                0, 3, 2, 8, 3, 7,
                //right
                1, 4, 5, 5, 6, 9,
                1, 4, 6, 9, 2, 8,
                //up
                4, 0, 5, 1, 1, 4,
                4, 0, 1, 4, 0, 3,
                //left
                4, 2, 0, 3, 3, 7,
                4, 2, 3, 7, 7, 6,
                //back
                5, 8, 4, 9, 7, 12,
                5, 8, 7, 12, 6, 11,
                //down
                3, 7, 2, 8, 6, 11,
                3, 7, 6, 11, 7, 10
            };
            mesh.getFaces().addAll(faces);

            MeshView die = new MeshView(mesh);
            PhongMaterial mat = new PhongMaterial();
            mat.setSelfIlluminationMap(new Image("data/common/images/dice.png"));
            mat.setDiffuseMap(new Image("data/common/images/dice.png"));
            mat.setSpecularMap(new Image("data/common/images/dice.png"));
            die.setMaterial(mat);
            add(die, i, 0);
        }
    }

    public void roll(){
        for(Node node : getChildren()) {
            MeshView die = (MeshView)node;
            die.setRotate(die.getRotate() - die.getRotate()%90);
            int choice = random.nextInt(2);
            die.setRotationAxis(new Point3D(choice == 1? 1 : 0, choice == 0? 1 : 0, 0));
            Timeline timeline = null;
            double rotationValue = die.getRotate();
            for(int i = 0; i < 4; i ++){
                Timeline lastTM = timeline;
                rotationValue +=  90 * ( 10 + random.nextInt(10));
                timeline = new Timeline(
                        new KeyFrame(Duration.seconds(1 + random.nextDouble()),
                                new KeyValue(die.rotateProperty(), rotationValue, lastTM == null? Interpolator.EASE_OUT : Interpolator.LINEAR))
                );
                if(lastTM != null) {
                    timeline.setOnFinished(e -> {
                        int c = random.nextInt(2);
                        die.setRotationAxis(new Point3D(c == 1 ? 1 : 0, c == 0 ? 1 : 0, 0));
                        lastTM.play();
                    });
                }
            }
            timeline.play();
    }
}
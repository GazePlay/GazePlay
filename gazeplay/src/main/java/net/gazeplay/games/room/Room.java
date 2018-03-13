package net.gazeplay.games.room;

import javafx.scene.AmbientLight;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

/* Things to implement :
    1. Add a gazeeventlistener
    2. Draw and add room walls
*/

public class Room implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;

    private PerspectiveCamera camera;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;

    // The long size of a side of the cube (this is the length and width of each side)
    private static final double SIZE_LONG = 2400;
    // The short size of a side of the cube (this is the thickness of each side)
    private static final double SIZE_SHORT = 0.0001;
    // The position of the side compared to the origin (length / 2)
    private static final double POSITION = SIZE_LONG / 2;

    public Room(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        Box front = new Box(SIZE_LONG, SIZE_SHORT, SIZE_LONG);
        front.setMaterial(new PhongMaterial(Color.DARKOLIVEGREEN));
        front.setTranslateY(POSITION);
        gameContext.getChildren().add(front);

        Box back = new Box(SIZE_LONG, SIZE_SHORT, SIZE_LONG);
        back.setMaterial(new PhongMaterial(Color.GOLD));
        back.setTranslateY(-POSITION);
        gameContext.getChildren().add(back);

        Box top = new Box(SIZE_LONG, SIZE_LONG, SIZE_SHORT);
        top.setMaterial(new PhongMaterial(Color.HOTPINK));
        top.setTranslateZ(POSITION);
        gameContext.getChildren().add(top);

        Box bottom = new Box(SIZE_LONG, SIZE_LONG, SIZE_SHORT);
        bottom.setMaterial(new PhongMaterial(Color.DARKGREEN));
        bottom.setTranslateZ(-POSITION);
        gameContext.getChildren().add(bottom);

        Box left = new Box(SIZE_SHORT, SIZE_LONG, SIZE_LONG);
        left.setMaterial(new PhongMaterial(Color.CRIMSON));
        left.setTranslateX(-POSITION);
        gameContext.getChildren().add(left);

        Box right = new Box(SIZE_SHORT, SIZE_LONG, SIZE_LONG);
        right.setMaterial(new PhongMaterial(Color.AQUAMARINE));
        right.setTranslateX(POSITION);
        gameContext.getChildren().add(right);

        camera = new PerspectiveCamera(true);
        camera.setVerticalFieldOfView(false);

        camera.setNearClip(0.1);
        camera.setFarClip(100000.0);
        camera.getTransforms().addAll(rotateX, rotateY, new Translate(0, 0, 0));

        PointLight light = new PointLight(Color.GAINSBORO);
        gameContext.getChildren().add(light);
        gameContext.getChildren().add(new AmbientLight(Color.WHITE));
        gameContext.getScene().setCamera(camera);

        gameContext.getScene().setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        gameContext.getScene().setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 1.0;

            if (me.isPrimaryButtonDown()) {
                rotateX.setAngle(rotateX.getAngle() - (mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED));
                rotateY.setAngle(rotateY.getAngle() + (mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED));
            }
        });
    }

    @Override
    public void dispose() {

    }
}
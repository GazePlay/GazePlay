package net.gazeplay.games.room;

import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

/* Things to implement :
    1. Add a gazeeventlistener
    2. Draw and add room image walls
*/

@Slf4j
public class Room implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;

    private PerspectiveCamera camera;

    private javafx.geometry.Dimension2D dimension2D;
    private final Rotate rotateX;
    private final Rotate rotateY;

    private static double xLength;
    private static double yLength;

    // The positions of the sides compared to the origin
    private double positionCamera;

    Image arrowImNorthWest;
    Rectangle rectangleArrowNorthWest;

    Image arrowImNorth;
    Rectangle rectangleArrowNorth;

    Image arrowImNorthEast;
    Rectangle rectangleArrowNorthEast;

    Image arrowImWest;
    Rectangle rectangleArrowWest;

    Image arrowImEast;
    Rectangle rectangleArrowEast;

    Image arrowImSouthWest;
    Rectangle rectangleArrowSouthWest;

    Image arrowImSouth;
    Rectangle rectangleArrowSouth;

    Image arrowImSouthEast;
    Rectangle rectangleArrowSouthEast;

    double imageWidth;
    double imageHeight;

    public Room(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        imageWidth = dimension2D.getWidth() / 7;
        imageHeight = dimension2D.getHeight() / 7;

        arrowImNorthWest = new Image("data/room/arrowNorthWest.png", imageWidth, imageHeight, true, true);
        rectangleArrowNorthWest = new Rectangle(arrowImNorthWest.getWidth(), arrowImNorthWest.getHeight());
        rectangleArrowNorthWest.setFill(new ImagePattern(arrowImNorthWest));

        arrowImNorth = new Image("data/room/arrowNorth.png", imageWidth, imageHeight, true, true);
        rectangleArrowNorth = new Rectangle(arrowImNorth.getWidth(), arrowImNorth.getHeight());
        rectangleArrowNorth.setFill(new ImagePattern(arrowImNorth));

        arrowImNorthEast = new Image("data/room/arrowNorthEast.png", imageWidth, imageHeight, true, true);
        rectangleArrowNorthEast = new Rectangle(arrowImNorthEast.getWidth(), arrowImNorthEast.getHeight());
        rectangleArrowNorthEast.setFill(new ImagePattern(arrowImNorthEast));

        arrowImWest = new Image("data/room/arrowWest.png", imageWidth, imageHeight, true, true);
        rectangleArrowWest = new Rectangle(arrowImWest.getWidth(), arrowImWest.getHeight());
        rectangleArrowWest.setFill(new ImagePattern(arrowImWest));

        arrowImEast = new Image("data/room/arrowEast.png", imageWidth, imageHeight, true, true);
        rectangleArrowEast = new Rectangle(arrowImEast.getWidth(), arrowImEast.getHeight());
        rectangleArrowEast.setFill(new ImagePattern(arrowImEast));

        arrowImSouthWest = new Image("data/room/arrowSouthWest.png", imageWidth, imageHeight, true, true);
        rectangleArrowSouthWest = new Rectangle(arrowImSouthWest.getWidth(), arrowImSouthWest.getHeight());
        rectangleArrowSouthWest.setFill(new ImagePattern(arrowImSouthWest));

        arrowImSouth = new Image("data/room/arrowSouth.png", imageWidth, imageHeight, true, true);
        rectangleArrowSouth = new Rectangle(arrowImSouth.getWidth(), arrowImSouth.getHeight());
        rectangleArrowSouth.setFill(new ImagePattern(arrowImSouth));

        arrowImSouthEast = new Image("data/room/arrowSouthEast.png", imageWidth, imageHeight, true, true);
        rectangleArrowSouthEast = new Rectangle(arrowImSouthEast.getWidth(), arrowImSouthEast.getHeight());
        rectangleArrowSouthEast.setFill(new ImagePattern(arrowImSouthEast));

        rotateX = new Rotate(0, Rotate.X_AXIS);
        rotateY = new Rotate(0, Rotate.Y_AXIS);
        xLength = dimension2D.getWidth();
        yLength = dimension2D.getHeight();
        positionCamera = -xLength / 2;
    }

    @Override
    public void launch() {
        Group objects = new Group();

        Wall top = new Wall("Y", -1, "top", xLength, yLength);
        objects.getChildren().add(top.getItem());

        Wall bottom = new Wall("Y", 1, "bottom", xLength, yLength);
        objects.getChildren().add(bottom.getItem());

        Wall back = new Wall("Z", -1, "back", xLength, yLength);
        objects.getChildren().add(back.getItem());

        Wall front = new Wall("Z", 1, "front", xLength, yLength);
        objects.getChildren().add(front.getItem());

        Wall left = new Wall("X", -1, "left", xLength, yLength);
        objects.getChildren().add(left.getItem());

        Wall right = new Wall("X", 1, "right", xLength, yLength);
        objects.getChildren().add(right.getItem());

        SubScene subScene = new SubScene(objects,
                dimension2D.getWidth() - arrowImWest.getWidth() - arrowImEast.getWidth(),
                dimension2D.getHeight() - arrowImNorth.getHeight() - arrowImSouth.getHeight());

        camera = new PerspectiveCamera(true);
        camera.setVerticalFieldOfView(false);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.getTransforms().addAll(rotateX, rotateY, new Translate(0, 0, positionCamera));

        PointLight light = new PointLight(Color.GAINSBORO);
        objects.getChildren().add(light);
        objects.getChildren().add(new AmbientLight(Color.WHITE));
        subScene.setCamera(camera);

        BorderPane root = new BorderPane(subScene);

        BorderPane topB = new BorderPane();
        topB.setRight(rectangleArrowNorthEast);
        topB.setCenter(rectangleArrowNorth);
        topB.setLeft(rectangleArrowNorthWest);
        root.setTop(topB);

        BorderPane bottomB = new BorderPane();
        bottomB.setRight(rectangleArrowSouthEast);
        bottomB.setCenter(rectangleArrowSouth);
        bottomB.setLeft(rectangleArrowSouthWest);
        root.setBottom(bottomB);

        root.setLeft(rectangleArrowWest);
        root.setRight(rectangleArrowEast);
        BorderPane.setAlignment(rectangleArrowWest, Pos.CENTER);
        BorderPane.setAlignment(rectangleArrowEast, Pos.CENTER);

        gameContext.getChildren().add(root);

        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowNorthWest);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowNorth);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowNorthEast);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowWest);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowEast);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowSouthWest);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowSouth);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowSouthEast);

        rectangleArrowNorthWest.setOnMouseMoved((event) -> {
            rotateX.setAngle(rotateX.getAngle() + 0.25);
            rotateY.setAngle(rotateY.getAngle() - 0.25);
        });

        rectangleArrowNorth.setOnMouseMoved((event) -> {
            rotateX.setAngle(rotateX.getAngle() + 0.5);
        });

        rectangleArrowNorthEast.setOnMouseMoved((event) -> {
            rotateX.setAngle(rotateX.getAngle() + 0.25);
            rotateY.setAngle(rotateY.getAngle() + 0.25);
        });

        rectangleArrowWest.setOnMouseMoved((event) -> {
            rotateY.setAngle(rotateY.getAngle() - 0.5);
        });

        rectangleArrowEast.setOnMouseMoved((event) -> {
            rotateY.setAngle(rotateY.getAngle() + 0.5);
        });

        rectangleArrowSouthWest.setOnMouseMoved((event) -> {
            rotateX.setAngle(rotateX.getAngle() - 0.25);
            rotateY.setAngle(rotateY.getAngle() - 0.25);
        });

        rectangleArrowSouth.setOnMouseMoved((event) -> {
            rotateX.setAngle(rotateX.getAngle() - 0.5);
        });

        rectangleArrowSouthEast.setOnMouseMoved((event) -> {
            rotateX.setAngle(rotateX.getAngle() - 0.25);
            rotateY.setAngle(rotateY.getAngle() + 0.25);
        });

        rectangleArrowNorthWest.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateX.setAngle(rotateX.getAngle() + 0.25);
            rotateY.setAngle(rotateY.getAngle() - 0.25);
        });

        rectangleArrowNorth.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateX.setAngle(rotateX.getAngle() + 0.5);
        });

        rectangleArrowNorthEast.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateX.setAngle(rotateX.getAngle() + 0.25);
            rotateY.setAngle(rotateY.getAngle() + 0.25);
        });

        rectangleArrowWest.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateY.setAngle(rotateY.getAngle() - 0.5);
        });

        rectangleArrowEast.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateY.setAngle(rotateY.getAngle() + 0.5);
        });

        rectangleArrowSouthWest.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateX.setAngle(rotateX.getAngle() - 0.25);
            rotateY.setAngle(rotateY.getAngle() - 0.25);
        });

        rectangleArrowSouth.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateX.setAngle(rotateX.getAngle() - 0.5);
        });

        rectangleArrowSouthEast.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateX.setAngle(rotateX.getAngle() - 0.25);
            rotateY.setAngle(rotateY.getAngle() + 0.25);
        });
    }

    @Override
    public void dispose() {

    }

}
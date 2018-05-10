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
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

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

    Image arrowImNorth;
    Rectangle rectangleArrowNorth;

    Image arrowImWest;
    Rectangle rectangleArrowWest;

    Image arrowImEast;
    Rectangle rectangleArrowEast;

    Image arrowImSouth;
    Rectangle rectangleArrowSouth;

    double imageWidth;
    double imageHeight;

    public Room(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        imageWidth = dimension2D.getWidth() / 7;
        imageHeight = dimension2D.getHeight() / 7;

        arrowImNorth = new Image("data/room/arrowNorth.png", imageWidth, imageHeight, true, true);
        rectangleArrowNorth = new Rectangle(arrowImNorth.getWidth(), arrowImNorth.getHeight());
        rectangleArrowNorth.setFill(new ImagePattern(arrowImNorth));

        arrowImWest = new Image("data/room/arrowWest.png", imageWidth, imageHeight, true, true);
        rectangleArrowWest = new Rectangle(arrowImWest.getWidth(), arrowImWest.getHeight());
        rectangleArrowWest.setFill(new ImagePattern(arrowImWest));

        arrowImEast = new Image("data/room/arrowEast.png", imageWidth, imageHeight, true, true);
        rectangleArrowEast = new Rectangle(arrowImEast.getWidth(), arrowImEast.getHeight());
        rectangleArrowEast.setFill(new ImagePattern(arrowImEast));

        arrowImSouth = new Image("data/room/arrowSouth.png", imageWidth, imageHeight, true, true);
        rectangleArrowSouth = new Rectangle(arrowImSouth.getWidth(), arrowImSouth.getHeight());
        rectangleArrowSouth.setFill(new ImagePattern(arrowImSouth));

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
        topB.setCenter(rectangleArrowNorth);
        root.setTop(topB);

        BorderPane bottomB = new BorderPane();
        bottomB.setCenter(rectangleArrowSouth);
        root.setBottom(bottomB);

        root.setLeft(rectangleArrowWest);
        root.setRight(rectangleArrowEast);
        BorderPane.setAlignment(rectangleArrowWest, Pos.CENTER);
        BorderPane.setAlignment(rectangleArrowEast, Pos.CENTER);

        gameContext.getChildren().add(root);

        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowNorth);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowWest);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowEast);
        gameContext.getGazeDeviceManager().addEventFilter(rectangleArrowSouth);

        int[] positivemultiplier = new int[] { 1 };

        rectangleArrowNorth.setOnMouseMoved((event) -> {
            if (rotateY.getAngle() == 0) {
                if (rotateX.getAngle() < 22.5) {
                    rotateX.setAngle(rotateX.getAngle() + positivemultiplier[0] * 0.25);
                }
            } else if (rotateX.getAngle() < 31) {
                rotateX.setAngle(rotateX.getAngle() + positivemultiplier[0] * 0.25);
            }
            // check for y
        });
        rectangleArrowNorth.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            if (rotateY.getAngle() == 0) {
                if (rotateX.getAngle() < 22.5) {
                    rotateX.setAngle(rotateX.getAngle() + positivemultiplier[0] * 0.25);
                }
            } else if (rotateX.getAngle() < 31) {
                rotateX.setAngle(rotateX.getAngle() + positivemultiplier[0] * 0.25);
            }
        });

        rectangleArrowSouth.setOnMouseMoved((event) -> {
            if (rotateY.getAngle() == 0) {
                if (rotateX.getAngle() > -22.5) {
                    rotateX.setAngle(rotateX.getAngle() - positivemultiplier[0] * 0.25);
                }
            } else if (rotateX.getAngle() > -31) {
                rotateX.setAngle(rotateX.getAngle() - positivemultiplier[0] * 0.25);
                if (rotateY.getAngle() > 0) {
                    rotateY.setAngle(rotateY.getAngle() - 0.1);
                } else if (rotateY.getAngle() < 0) {
                    rotateY.setAngle(rotateY.getAngle() + 0.1);
                }
            }
        });
        rectangleArrowSouth.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            if (rotateY.getAngle() == 0) {
                if (rotateX.getAngle() > -22.5) {
                    rotateX.setAngle(rotateX.getAngle() - positivemultiplier[0] * 0.25);
                }
            } else if (rotateX.getAngle() > -31) {
                rotateX.setAngle(rotateX.getAngle() - positivemultiplier[0] * 0.25);
                if (rotateY.getAngle() > 0) {
                    rotateY.setAngle(rotateY.getAngle() - 0.1);
                } else if (rotateY.getAngle() < 0) {
                    rotateY.setAngle(rotateY.getAngle() + 0.1);
                }
            }
        });

        rectangleArrowWest.setOnMouseMoved((event) -> {
            System.out.println("w : " + rotateY.getAngle());
            rotateY.setAngle(rotateY.getAngle() - 0.25);
            if (rotateX.getAngle() > 0) {
                rotateX.setAngle(rotateX.getAngle() - 0.1);
            } else if (rotateX.getAngle() < 0) {
                rotateX.setAngle(rotateX.getAngle() + 0.1);
            }
            if (rotateY.getAngle() < -90 && positivemultiplier[0] == 1) {
                positivemultiplier[0] = -1;
            } else if (rotateY.getAngle() < -270 && positivemultiplier[0] == -1) {
                positivemultiplier[0] = 1;
            } else if (rotateY.getAngle() > 90 && positivemultiplier[0] == -1) {
                positivemultiplier[0] = 1;
            } else if (rotateY.getAngle() > 270 && positivemultiplier[0] == 1) {
                positivemultiplier[0] = -1;
            }
        });
        rectangleArrowWest.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateY.setAngle(rotateY.getAngle() - 0.25);
            if (rotateX.getAngle() != 0) {

            }
        });

        rectangleArrowEast.setOnMouseMoved((event) -> {
            System.out.println("ex : " + rotateX.getAngle() + " ey : " + rotateY.getAngle());
            rotateY.setAngle(rotateY.getAngle() + 0.25);
            if (rotateX.getAngle() > 0) {
                rotateX.setAngle(rotateX.getAngle() - 0.1);
            } else if (rotateX.getAngle() < 0) {
                rotateX.setAngle(rotateX.getAngle() + 0.1);
            }
            if (rotateY.getAngle() > 90 && positivemultiplier[0] == 1) {
                positivemultiplier[0] = -1;
            } else if (rotateY.getAngle() > 270 && positivemultiplier[0] == -1) {
                positivemultiplier[0] = 1;
            } else if (rotateY.getAngle() < -90 && positivemultiplier[0] == -1) {
                positivemultiplier[0] = 1;
            } else if (rotateY.getAngle() < -270 && positivemultiplier[0] == 1) {
                positivemultiplier[0] = -1;
            }
        });
        rectangleArrowEast.addEventHandler(GazeEvent.GAZE_MOVED, (GazeEvent ge) -> {
            rotateY.setAngle(rotateY.getAngle() + 0.25);
            if (rotateX.getAngle() != 0) {
                rotateX.setAngle(rotateX.getAngle() - 0.25);
            }
        });
    }

    @Override
    public void dispose() {

    }

}
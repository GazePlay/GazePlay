package net.gazeplay.games.biboulejump;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class BibouleJump extends AnimationTimer implements GameLifeCycle {

    private static String DATA_PATH = "data/biboulejump";

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Random randomGenerator;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Group foregroundLayer;

    //private final ImageLibrary cloudImages;
    //private final ImageLibrary bibouleImages;

    private Point2D gazeTarget;
    private Timeline jumpAscentTimeline;
    private Timeline jumpDescentTimeLine;
    private Timeline fallTimeline;
    private Timeline horizontalMvmtTimeline;
    private double jumpHeight;
    private double terminalVelocity; //pixels per second

    private final double platformWidth;
    private final double platformHeight;

    private long lastTickTime = 0;

    private Rectangle biboule;
    private Label fpsCounter;
    private ArrayList<Rectangle> platforms;

    public BibouleJump(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.randomGenerator = new Random();

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        this.gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        //this.cloudImages = ImageUtils.createImageLibrary(new File("data/biboulejump/clouds"));
        //this.bibouleImages = ImageUtils.createImageLibrary(new File("data/biboulejump/biboules"));

        this.jumpHeight = dimensions.getHeight() / 2;
        this.terminalVelocity = dimensions.getHeight();

        this.platforms = new ArrayList();
        this.platformHeight = dimensions.getHeight()/15;
        this.platformWidth = dimensions.getWidth()/10;

        Rectangle backgroundImage = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        backgroundImage.setFill(Color.SKYBLUE);
        this.backgroundLayer.getChildren().add(backgroundImage);

        biboule = new Rectangle(dimensions.getWidth() / 2.0, dimensions.getHeight() / 2.0, dimensions.getHeight() / 8,
                dimensions.getHeight() / 8);
        this.middleLayer.getChildren().add(biboule);

        gazeTarget = new Point2D(dimensions.getWidth() / 2.0, dimensions.getHeight() / 2.0);

        EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                gazeTarget = new Point2D(((GazeEvent) event).getX(), ((GazeEvent) event).getY());
            }

            double distance = gazeTarget.distance(biboule.getX() + biboule.getWidth()/2.0, gazeTarget.getY());
            horizontalMvmtTimeline = new Timeline(new KeyFrame(Duration.seconds(distance/terminalVelocity),
                    new KeyValue(biboule.xProperty(), gazeTarget.getX() - biboule.getWidth()/2)));

            horizontalMvmtTimeline.playFromStart();
        };

        Rectangle interactionOverlay = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);

        fpsCounter = new Label("60");
        this.gameContext.getChildren().add(fpsCounter);

        bounce();
    }

    private void bounce() {

        jumpAscentTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
                new KeyValue(biboule.yProperty(), biboule.getY() - jumpHeight, new Interpolator() {
                    @Override
                    protected double curve(double t) {
                        return -Math.pow(t - 1, 2) + 1;
                    }
                })));

        jumpAscentTimeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                jumpDescentTimeLine.playFromStart();
            }
        });

        jumpDescentTimeLine = new Timeline(
                new KeyFrame(Duration.seconds(1), new KeyValue(biboule.yProperty(), biboule.getY(), new Interpolator() {
                    @Override
                    protected double curve(double t) {
                        return Math.pow(t, 2);
                    }
                })));

        jumpDescentTimeLine.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fallTimeline.playFromStart();
            }
        });

        fallTimeline = new Timeline(
                new KeyFrame(Duration.seconds((dimensions.getHeight()-biboule.getY())/terminalVelocity), new KeyValue(biboule.yProperty(), dimensions.getHeight())));

        fallTimeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // gameover
            }
        });

        jumpAscentTimeline.play();
    }

    @Override
    public void launch() {
        generatePlatforms();

        biboule.setFill(new ImagePattern(new Image("data/biboulejump/biboules/green.png")));

        this.start();
    }

    @Override
    public void dispose() {
        //Show end menu (restart, quit, score)


    }

    private void generatePlatforms() {
        for(int i = 0; i < 10; i++){
            Rectangle r = new Rectangle(randomGenerator.nextInt((int)(dimensions.getWidth() - platformWidth)), randomGenerator.nextInt((int)(dimensions.getHeight() - platformHeight)), platformWidth, platformHeight);
            platforms.add(r);
            r.setFill(new ImagePattern(new Image("data/biboulejump/clouds/cloud.png")));
            backgroundLayer.getChildren().add(r);
        }
    }

    @Override
    public void handle(long now) {
        if (lastTickTime == 0) {
            lastTickTime = now;
        }
        double timeElapsed = ((double) now - (double) lastTickTime) / Math.pow(10.0, 6.0); // in ms
        lastTickTime = now;
        fpsCounter.setText(String.valueOf((int) (1000 / timeElapsed)));


        //Collision detection
        if(jumpAscentTimeline.getStatus() == Animation.Status.STOPPED){ //The biboule is falling
            Rectangle bibouleCollider = new Rectangle(biboule.getX() + biboule.getWidth()/4, biboule.getY() + biboule.getHeight() *2/3, biboule.getWidth()/2, biboule.getHeight()/3);
            for(Rectangle platform : platforms){
                Rectangle platformCollider = new Rectangle(platform.getX(), platform.getY() + platformHeight/2, platformWidth, platformHeight/2);
                if(rectangleAndRectangleCollision(bibouleCollider, platformCollider)){
                    jumpDescentTimeLine.stop();
                    fallTimeline.stop();
                    bounce();
                    break;
                }
            }
        }
        

        //Scrolling
        /*if(biboule.getY() <= dimensions.getHeight()/2){
            double difference = dimensions.getHeight()/2 - biboule.getY();
            for(Rectangle platform : platforms) {
                platform.setY(platform.getY() + difference);
                if(platform.getY() >= dimensions.getHeight()){
                    backgroundLayer.getChildren().remove(platform);
                }
            }
            biboule.setY(biboule.getY() + difference);
        }*/

        if (biboule.getY() + biboule.getHeight() >= dimensions.getHeight()) {
            dispose();
        }
    }

    private boolean rectangleAndPointCollision(Rectangle rectangle, double x, double y){
        return (x >= rectangle.getX() && x <= rectangle.getX() + rectangle.getWidth()) && (y >= rectangle.getY() && y <= rectangle.getY() + rectangle.getHeight());
    }

    private boolean rectangleAndRectangleCollision(Rectangle rect1, Rectangle rect2){
        return rect1.getX() < rect2.getX() + rect2.getWidth() &&
               rect1.getX() + rect1.getWidth() > rect2.getX() &&
               rect1.getY() < rect2.getY() + rect2.getHeight() &&
               rect1.getY() + rect1.getHeight() > rect2.getY();
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}

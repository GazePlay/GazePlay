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
    private Point2D velocity;
    private final double gravity = 0.005;
    private double terminalVelocity = 0.8;
    private final double maxSpeed = 0.5;

    private final double platformWidth;
    private final double platformHeight;
    private double highestPlatformY = Double.POSITIVE_INFINITY;

    private long lastTickTime = 0;
    private long minFPS = 1000;

    private Rectangle biboule;
    private Label onScreenText;
    private Label scoreLabel;
    private ArrayList<Rectangle> platforms;

    private long score;

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

        velocity = Point2D.ZERO;

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
        };

        Rectangle interactionOverlay = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);

        onScreenText = new Label();
        this.gameContext.getChildren().add(onScreenText);

        scoreLabel = new Label();
        this.gameContext.getChildren().add(scoreLabel);

        bounce();
    }

    private void bounce(){
        velocity = new Point2D(velocity.getX(), -terminalVelocity * 3);
    }

    @Override
    public void launch() {
        generatePlatforms(dimensions.getHeight());

        biboule.setFill(new ImagePattern(new Image("data/biboulejump/biboules/green.png")));

        score = 0;

        this.start();
    }

    @Override
    public void dispose() {
        //Show end menu (restart, quit, score)
        ProgressButton restartButton = new ProgressButton();
        foregroundLayer.getChildren().add(restartButton);

    }

    private void generatePlatforms(double bottomLimit) {
        double topLimit = -dimensions.getHeight();
        int nbPlatforms = (int)(bottomLimit - topLimit) / 100;
        log.info("Generating " + nbPlatforms + " platforms");
        for(int i = 0; i < nbPlatforms; i++){
            Rectangle r = new Rectangle(randomGenerator.nextInt((int)(dimensions.getWidth() - platformWidth)), randomGenerator.nextInt((int)(bottomLimit - topLimit)) + topLimit, platformWidth, platformHeight);
            if(r.getY() < highestPlatformY)
                highestPlatformY = r.getY();
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
        String logs = "FPS: " + String.valueOf((int) (1000 / timeElapsed)) + "\n";
        if(1000/timeElapsed < minFPS){
            minFPS = 1000/(int)timeElapsed;
            log.info("New min fps: " + minFPS);
        }

        //Movement
        //Gravity
        velocity = velocity.add(0, gravity*timeElapsed);
        if(velocity.getY() > terminalVelocity){
            velocity = new Point2D(velocity.getX(), terminalVelocity);
        }

        double distance = Math.abs(gazeTarget.getX() - (biboule.getX() + biboule.getWidth()/2));
        double direction = distance == 0 ? 1 : (gazeTarget.getX() - (biboule.getX() + biboule.getWidth()/2)) / distance;
        if(distance > maxSpeed){
            velocity = new Point2D(maxSpeed * direction, velocity.getY());
        }else{
            velocity = new Point2D(distance * direction, velocity.getY());
        }
        //Apply velocity
        biboule.setY(biboule.getY() + velocity.getY() * timeElapsed);
        biboule.setX(biboule.getX() + velocity.getX() * timeElapsed);

        //Collision detection
        if(velocity.getY() > 0){ //The biboule is falling
            Rectangle bibouleCollider = new Rectangle(biboule.getX() + biboule.getWidth()/4, biboule.getY() + biboule.getHeight() *2/3, biboule.getWidth()/2, biboule.getHeight()/3);
            for(Rectangle platform : platforms){
                Rectangle platformCollider = new Rectangle(platform.getX(), platform.getY() + platformHeight/2, platformWidth, platformHeight/2);
                if(rectangleAndRectangleCollision(bibouleCollider, platformCollider)){
                    bounce();
                    break;
                }
            }
        }


        //Scrolling
        if(biboule.getY() <= dimensions.getHeight()/3){
            double difference = dimensions.getHeight()/3 - biboule.getY();
            score += difference;
            for(Rectangle platform : platforms) {
                platform.setY(platform.getY() + difference);
                if(platform.getY() >= dimensions.getHeight()){
                    backgroundLayer.getChildren().remove(platform);
                    platforms.remove(platform);
                }
            }
            biboule.setY(biboule.getY() + difference);
            highestPlatformY += difference;
        }

        if(highestPlatformY >= -dimensions.getHeight()/2)
            generatePlatforms(highestPlatformY);

        if (biboule.getY() + biboule.getHeight() >= dimensions.getHeight()) {
            dispose();
        }

        logs += "Score: " + score;
        logs += "nb platforms: " + platforms.size();
        logs += "nbhighlatforms: " + highestPlatformY;
        onScreenText.setText(logs);
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

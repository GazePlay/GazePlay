package net.gazeplay.games.biboulejump;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Iterator;
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
    private final Rectangle interactionOverlay;

    // private final ImageLibrary cloudImages;
    // private final ImageLibrary bibouleImages;

    private Point2D gazeTarget;
    private Point2D velocity;
    private final double gravity = 0.005;
    private double terminalVelocity = 0.8;
    private final double maxSpeed = 0.7;

    private final double platformWidth;
    private final double platformHeight;
    private double highestPlatformY;

    private long lastTickTime = 0;
    private long minFPS = 1000;

    private Rectangle biboule;
    private Label onScreenText;
    private Text scoreText;
    private ArrayList<Rectangle> platforms;

    private long score;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private Text finalScoreText;
    private final int fixationLength;

    public BibouleJump(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.randomGenerator = new Random();

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        this.gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        // this.cloudImages = ImageUtils.createImageLibrary(new File("data/biboulejump/clouds"));
        // this.bibouleImages = ImageUtils.createImageLibrary(new File("data/biboulejump/biboules"));

        this.platforms = new ArrayList();
        this.platformHeight = dimensions.getHeight() / 15;
        this.platformWidth = dimensions.getWidth() / 10;

        Rectangle backgroundImage = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        backgroundImage.setFill(Color.SKYBLUE);
        this.backgroundLayer.getChildren().add(backgroundImage);

        onScreenText = new Label();
        foregroundLayer.getChildren().add(onScreenText);

        scoreText = new Text(0, 50, "0");
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFont(new Font(50));
        scoreText.setWrappingWidth(dimensions.getWidth());
        foregroundLayer.getChildren().add(scoreText);

        // Menu
        fixationLength = Configuration.getInstance().getFixationLength();

        shade = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        ImageView restartImage = new ImageView("data/biboulejump/menu/restart.png");
        restartImage.setFitHeight(dimensions.getHeight() / 6);
        restartImage.setFitWidth(dimensions.getHeight() / 6);
        restartButton.setImage(restartImage);
        restartButton.setLayoutX(dimensions.getWidth() / 2 - dimensions.getHeight() / 12);
        restartButton.setLayoutY(dimensions.getHeight() / 2 - dimensions.getHeight() / 12);
        restartButton.assignIndicator(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                launch();
            }
        }, fixationLength);

        finalScoreText = new Text(0, dimensions.getHeight() / 3, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimensions.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        // Interaction
        gazeTarget = new Point2D(dimensions.getWidth() / 2.0, dimensions.getHeight() / 2.0);

        EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                gazeTarget = new Point2D(((GazeEvent) event).getX(), ((GazeEvent) event).getY());
            }
        };

        interactionOverlay = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);
    }

    private void bounce() {
        velocity = new Point2D(velocity.getX(), -terminalVelocity * 3);
    }

    @Override
    public void launch() {

        // hide end game menu
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

        highestPlatformY = Double.POSITIVE_INFINITY;
        backgroundLayer.getChildren().removeAll(platforms);
        platforms.clear();
        generatePlatforms(dimensions.getHeight());

        this.middleLayer.getChildren().clear();
        biboule = new Rectangle(dimensions.getWidth() / 2, dimensions.getHeight() / 2, dimensions.getHeight() / 8,
                dimensions.getHeight() / 8);
        this.middleLayer.getChildren().add(biboule);
        biboule.setFill(new ImagePattern(new Image("data/biboulejump/biboules/green.png")));

        velocity = Point2D.ZERO;
        score = 0;
        lastTickTime = 0;
        gazeTarget = new Point2D(dimensions.getWidth() / 2, 0);
        createPlatform(biboule.getX() + biboule.getWidth() / 2, biboule.getY() + biboule.getHeight() + platformHeight);

        this.start();
        bounce();
    }

    @Override
    public void dispose() {

    }

    private void showDeathMenu() {
        this.stop();
        // Show end menu (restart, quit, score)
        interactionOverlay.setDisable(true);
        shade.setOpacity(1);
        finalScoreText.setText("You scored " + score + " points");
        finalScoreText.setOpacity(1);
        restartButton.active();
    }

    private void createPlatform(double centerX, double centerY) {
        Rectangle r = new Rectangle(centerX - platformWidth / 2, centerY - platformHeight / 2, platformWidth,
                platformHeight);
        if (r.getY() < highestPlatformY)
            highestPlatformY = r.getY();
        platforms.add(r);
        r.setFill(new ImagePattern(new Image("data/biboulejump/clouds/cloud.png")));
        backgroundLayer.getChildren().add(r);
    }

    private void generatePlatforms(double bottomLimit) {
        double topLimit = -dimensions.getHeight();
        int nbPlatforms = (int) (bottomLimit - topLimit) / 100;
        log.info("Generating " + nbPlatforms + " platforms");
        for (int i = 0; i < nbPlatforms; i++) {
            createPlatform(randomGenerator.nextInt((int) (dimensions.getWidth() - platformWidth)),
                    randomGenerator.nextInt((int) (bottomLimit - topLimit)) + topLimit);
        }
    }

    @Override
    public void handle(long now) {
        if (lastTickTime == 0) {
            lastTickTime = now;
        }
        double timeElapsed = ((double) now - (double) lastTickTime) / Math.pow(10.0, 6.0); // in ms
        lastTickTime = now;
        String logs = "FPS: " + (int) (1000 / timeElapsed) + "\n";
        if (1000 / timeElapsed < minFPS) {
            minFPS = 1000 / (int) timeElapsed;
        }
        logs += "MinFPS: " + minFPS + "\n";

        // Movement
        // Gravity
        velocity = velocity.add(0, gravity * timeElapsed);
        if (velocity.getY() > terminalVelocity) {
            velocity = new Point2D(velocity.getX(), terminalVelocity);
        }

        // Lateral mouvement
        double distance = Math.abs(gazeTarget.getX() - (biboule.getX() + biboule.getWidth() / 2));
        double direction = distance == 0 ? 1
                : (gazeTarget.getX() - (biboule.getX() + biboule.getWidth() / 2)) / distance;
        if (distance > maxSpeed) {
            velocity = new Point2D(maxSpeed * direction, velocity.getY());
        } else {
            velocity = new Point2D(distance * direction, velocity.getY());
        }
        // Apply velocity
        biboule.setY(biboule.getY() + velocity.getY() * timeElapsed);
        biboule.setX(biboule.getX() + velocity.getX() * timeElapsed);

        // Collision detection
        if (velocity.getY() > 0) { // The biboule is falling
            Rectangle bibouleCollider = new Rectangle(biboule.getX() + biboule.getWidth() / 4,
                    biboule.getY() + biboule.getHeight() * 2 / 3, biboule.getWidth() / 2, biboule.getHeight() / 3);
            for (Rectangle platform : platforms) {
                Rectangle platformCollider = new Rectangle(platform.getX(), platform.getY() + platformHeight / 2,
                        platformWidth, platformHeight / 2);
                if (rectangleAndRectangleCollision(bibouleCollider, platformCollider)) {
                    bounce();
                    break;
                }
            }
        }

        // Scrolling
        if (biboule.getY() <= dimensions.getHeight() / 3) {
            double difference = dimensions.getHeight() / 3 - biboule.getY();
            updateScore(difference);
            Iterator<Rectangle> platformIter = platforms.iterator();
            while (platformIter.hasNext()) {
                Rectangle platform = platformIter.next();
                platform.setY(platform.getY() + difference);
                if (platform.getY() >= dimensions.getHeight()) {
                    backgroundLayer.getChildren().remove(platform);
                    platformIter.remove();
                }
            }
            biboule.setY(biboule.getY() + difference);
            highestPlatformY += difference;
        }

        if (highestPlatformY >= -dimensions.getHeight() / 2)
            generatePlatforms(highestPlatformY);

        // Fall out of screen
        if (biboule.getY() >= dimensions.getHeight()) {
            showDeathMenu();
        }

        logs += "Score: " + score + "\n";
        logs += "nb platforms: " + platforms.size() + "\n";
        logs += "nbhighlatforms: " + highestPlatformY + "\n";
        onScreenText.setText(logs);
    }

    private void updateScore(double difference) {
        score += difference;
        scoreText.setText(String.valueOf(score));
        scoreText.setX(dimensions.getWidth() / 2 - scoreText.getWrappingWidth() / 2);
    }

    private boolean rectangleAndRectangleCollision(Rectangle rect1, Rectangle rect2) {
        return rect1.getX() < rect2.getX() + rect2.getWidth() && rect1.getX() + rect1.getWidth() > rect2.getX()
                && rect1.getY() < rect2.getY() + rect2.getHeight() && rect1.getY() + rect1.getHeight() > rect2.getY();
    }

}

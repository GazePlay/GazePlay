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
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.*;
import java.util.*;

@Slf4j
public class BibouleJump extends AnimationTimer implements GameLifeCycle {

    private static String DATA_PATH = "data/biboulejump";

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Random randomGenerator;
    private final Configuration config;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Group foregroundLayer;
    private final Rectangle interactionOverlay;

    private Point2D gazeTarget;
    private Point2D velocity;
    private double gravity = 0.005;
    private double terminalVelocity = 0.8;
    private double maxSpeed = 0.7;

    private final double platformWidth;
    private final double platformHeight;
    private Rectangle highestPlatform;

    private long lastTickTime = 0;
    private long minFPS = 1000;

    private Rectangle biboule;
    private Label onScreenText;
    private Text scoreText;
    private ArrayList<Rectangle> platforms;
    private ArrayList<Rectangle> bouncepads;

    private int score;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private Text finalScoreText;
    private final int fixationLength;

    private final Multilinguism translate;

    public BibouleJump(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.randomGenerator = new Random();
        this.config = Configuration.getInstance();

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        this.gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        this.platforms = new ArrayList();
        this.platformHeight = dimensions.getHeight() / 10;
        this.platformWidth = dimensions.getWidth() / 7;

        this.bouncepads = new ArrayList();

        this.translate = Multilinguism.getSingleton();

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
        fixationLength = config.getFixationLength();

        shade = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        ImageView restartImage = new ImageView(DATA_PATH + "/menu/restart.png");
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

        finalScoreText = new Text(0, dimensions.getHeight() / 4, "");
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

    private void bounce(double intensity, String soundName) {
        velocity = new Point2D(velocity.getX(), -terminalVelocity * intensity);
        Utils.playSound(DATA_PATH + "/sounds/" + soundName);
    }

    @Override
    public void launch() {

        // hide end game menu
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

        backgroundLayer.getChildren().removeAll(platforms);
        platforms.clear();

        this.middleLayer.getChildren().clear();
        biboule = new Rectangle(dimensions.getWidth() / 2, dimensions.getHeight() / 2, dimensions.getHeight() / 6,
                dimensions.getHeight() / 6);
        this.middleLayer.getChildren().add(biboule);
        biboule.setFill(new ImagePattern(new Image(DATA_PATH + "/biboules/green.png")));

        velocity = Point2D.ZERO;
        score = 0;
        lastTickTime = 0;
        gazeTarget = new Point2D(dimensions.getWidth() / 2, 0);
        createPlatform(biboule.getX() + biboule.getWidth() / 2, biboule.getY() + biboule.getHeight() + platformHeight);

        generatePlatforms(dimensions.getHeight());

        this.start();
        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {

    }

    private int getsetHighscore(int score) {
        File f = new File(Utils.getUserStatsFolder(config.getUserName()) + "/Biboule Jump/highscores.dat");
        try {
            ArrayList<Integer> highscores = new ArrayList();
            if (!f.createNewFile()) {
                Scanner scanner = new Scanner(f, "utf-8");
                scanner.useDelimiter(":");
                while (scanner.hasNextInt()) {
                    highscores.add(scanner.nextInt());
                }
            }
            highscores.add(score);

            Collections.sort(highscores);
            if (highscores.size() > 3) {
                highscores = new ArrayList(highscores.subList(highscores.size() - 3, highscores.size()));
            }

            Writer writer = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
            for (int i : highscores)
                writer.write(i + ":");
            writer.close();

            return highscores.get(highscores.size() - 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return score;
    }

    private void death() {
        this.stop();
        // Show end menu (restart, quit, score)
        interactionOverlay.setDisable(true);
        shade.setOpacity(1);
        int highscore = getsetHighscore(score);
        StringBuilder sb = new StringBuilder();
        sb.append(translate.getTrad("Score", config.getLanguage()) + translate.getTrad("Colon", config.getLanguage())
                + " " + score + "\n");
        sb.append(translate.getTrad("Highscore", config.getLanguage())
                + translate.getTrad("Colon", config.getLanguage()) + " " + highscore + "\n");
        if (highscore <= score)
            sb.append(translate.getTrad("New highscore!", config.getLanguage()));
        finalScoreText.setText(sb.toString());
        finalScoreText.setOpacity(1);
        restartButton.active();
    }

    private void createBouncepad(double platformX, double platformY) {
        Rectangle b = new Rectangle(
                platformX + randomGenerator.nextInt((int) (platformWidth * 4 / 5)) - platformWidth / 2,
                platformY - platformHeight / 3, platformWidth / 5, platformHeight / 3);
        b.setFill(new ImagePattern(new Image(DATA_PATH + "/bouncepad.png")));
        backgroundLayer.getChildren().add(b);
        bouncepads.add(b);
    }

    private void createPlatform(double centerX, double centerY) {
        Rectangle r = new Rectangle(centerX - platformWidth / 2, centerY - platformHeight / 2, platformWidth,
                platformHeight);
        highestPlatform = r;
        platforms.add(r);
        r.setFill(new ImagePattern(new Image(DATA_PATH + "/clouds/cloud.png")));
        backgroundLayer.getChildren().add(r);
    }

    private void generatePlatforms(double bottomLimit) {
        double top = -dimensions.getHeight();
        double bottom = bottomLimit;
        while (bottom > top) {
            double newPlatX;
            double newPlatY;
            do {
                newPlatX = randomGenerator.nextInt((int) (dimensions.getWidth() - platformWidth / 2))
                        + platformWidth / 2;
                newPlatY = bottom - randomGenerator.nextInt((int) (dimensions.getHeight() / 4));
            } while (Math.abs(newPlatX - highestPlatform.getX()) >= dimensions.getWidth() / 3);
            createPlatform(newPlatX, newPlatY);
            if (randomGenerator.nextInt(15) == 0) {
                createBouncepad(newPlatX, newPlatY);
            }

            bottom = newPlatY - 2 * platformHeight;
        }
    }

    private void scrollList(ArrayList<Rectangle> rects, double difference) {
        Iterator<Rectangle> rectIter = rects.iterator();
        while (rectIter.hasNext()) {
            Rectangle rect = rectIter.next();
            rect.setY(rect.getY() + difference);
            if (rect.getY() >= dimensions.getHeight()) {
                backgroundLayer.getChildren().remove(rect);
                rectIter.remove();
            }
        }
    }

    private void collisionList(ArrayList<Rectangle> rects, Rectangle bibouleCollider, double intensity, String sound) {
        for (Rectangle platform : rects) {
            Rectangle platformCollider = new Rectangle(platform.getX(), platform.getY() + platform.getHeight() / 2,
                    platform.getWidth(), platform.getHeight() / 2);
            if (rectangleAndRectangleCollision(bibouleCollider, platformCollider)) {
                bounce(intensity, sound);
                break;
            }
        }
    }

    @Override
    public void handle(long now) {

        if (lastTickTime == 0) {
            lastTickTime = now;
        }
        double timeElapsed = ((double) now - (double) lastTickTime) / Math.pow(10.0, 6.0); // in ms
        lastTickTime = now;
        timeElapsed /= config.getSpeedEffects();

        String logs = "FPS: " + (int) (1000 / timeElapsed) + "\n";
        if (1000 / timeElapsed < minFPS) {
            minFPS = 1000 / (int) timeElapsed;
        }
        logs += "MinFPS: " + minFPS + "\n";

        // Movement
        /// Gravity
        velocity = velocity.add(0, gravity * timeElapsed);
        if (velocity.getY() > terminalVelocity) {
            velocity = new Point2D(velocity.getX(), terminalVelocity);
        }

        /// Lateral mouvement
        double distance = Math.abs(gazeTarget.getX() - (biboule.getX() + biboule.getWidth() / 2));
        double direction = distance == 0 ? 1
                : (gazeTarget.getX() - (biboule.getX() + biboule.getWidth() / 2)) / distance;
        if (distance > maxSpeed) {
            velocity = new Point2D(maxSpeed * direction, velocity.getY());
        } else {
            velocity = new Point2D(distance * direction, velocity.getY());
        }
        /// Apply velocity
        biboule.setY(biboule.getY() + velocity.getY() * timeElapsed);
        biboule.setX(biboule.getX() + velocity.getX() * timeElapsed);

        // Collision detection
        if (velocity.getY() > 0) { // The biboule is falling
            Rectangle bibouleCollider = new Rectangle(biboule.getX() + biboule.getWidth() / 4,
                    biboule.getY() + biboule.getHeight() * 2 / 3, biboule.getWidth() / 2, biboule.getHeight() / 3);
            collisionList(platforms, bibouleCollider, 3, "bounce.wav");
            collisionList(bouncepads, bibouleCollider, 6, "boing.wav");
        }

        // Scrolling
        if (biboule.getY() <= dimensions.getHeight() / 3) {
            double difference = dimensions.getHeight() / 3 - biboule.getY();
            updateScore(difference);
            scrollList(platforms, difference);
            scrollList(bouncepads, difference);
            biboule.setY(biboule.getY() + difference);
        }

        if (highestPlatform.getY() >= -dimensions.getHeight() / 2)
            generatePlatforms(highestPlatform.getY());

        // Fall out of screen
        if (biboule.getY() >= dimensions.getHeight()) {
            death();
        }

        logs += "Score: " + score + "\n";
        logs += "nb platforms: " + platforms.size() + "\n";
        // onScreenText.setText(logs);
    }

    private void updateScore(double difference) {
        score += difference / dimensions.getHeight() * 100;
        scoreText.setText(String.valueOf(score));
        scoreText.setX(dimensions.getWidth() / 2 - scoreText.getWrappingWidth() / 2);
    }

    private boolean rectangleAndRectangleCollision(Rectangle rect1, Rectangle rect2) {
        return rect1.getX() < rect2.getX() + rect2.getWidth() && rect1.getX() + rect1.getWidth() > rect2.getX()
                && rect1.getY() < rect2.getY() + rect2.getHeight() && rect1.getY() + rect1.getHeight() > rect2.getY();
    }

}

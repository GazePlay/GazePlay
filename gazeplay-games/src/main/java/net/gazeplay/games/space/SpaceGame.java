package net.gazeplay.games.space;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableBooleanValue;
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
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.components.ProgressButton;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

@Slf4j
public class SpaceGame extends AnimationTimer implements GameLifeCycle {

    private final SpaceGameStats stats;
    private final Dimension2D dimension2D;
    private final ReplayablePseudoRandom random;
    private final Configuration configuration;

    private final ImageLibrary spaceshipImage;
    private final ImageLibrary bibouleImage;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Group foregroundLayer;
    private final Rectangle interactionOverlay;
    private final IGameContext gameContext;

    private Point2D gazeTarget;
    private Point2D velocity;

    private final double bibouleWidth;
    private final double bibouleHeight;
    private final double bossWidth;
    private final double bossHeight;

    private long lastTickTime = 0;
    private long minFPS = 1000;
    private long startTime;

    private Rectangle spaceship;
    private Rectangle spaceshipCollider;
    private final Text scoreText;
    private final ArrayList<Biboule> biboules;
    private final ArrayList<Biboule> biboulesKilled;
    private final ArrayList<Point2D> biboulesPos;
    private final ArrayList<Boss> bosses;
    private final ArrayList<Boss> bossKilled;

    private int score;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private final Multilinguism translate;

    private TranslateTransition bulletTransition;
    private ParallelTransition parallelTransition4;
    private SequentialTransition sequentialTransition2;
    private FadeTransition bulletDisappear;
    private FadeTransition bossDisappear;
    private FadeTransition bossFade;
    private FadeTransition bossFade2;

    private int bibouleValue;
    private final ArrayList<Rectangle> bulletListRec;
    private final ArrayList<Rectangle> bulletBibouleListRec;
    private final ArrayList<Rectangle> bulletBossListRec;
    private final ArrayList<Rectangle> spaceshipDestroyed;
    private int bossHit;
    private final SpaceGameVariant gameVariant;

    public SpaceGame(final IGameContext gameContext, final SpaceGameStats stats, SpaceGameVariant gameVariant) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        this.gameContext.startTimeLimiter();
        this.gameContext.startScoreLimiter();
        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());
        this.gameVariant = gameVariant;

        spaceshipImage = ImageUtils.createCustomizedImageLibrary(null, "space/spaceship3", random);
        bibouleImage = ImageUtils.createCustomizedImageLibrary(null, "space/biboule", random);

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        this.biboules = new ArrayList<>();
        this.bibouleWidth = dimension2D.getWidth() / 20;
        this.bibouleHeight = dimension2D.getHeight() / 10;
        this.bossWidth = dimension2D.getWidth() / 8;
        this.bossHeight = dimension2D.getHeight() / 4;

        this.translate = MultilinguismFactory.getSingleton();

        final Label onScreenText = new Label();
        foregroundLayer.getChildren().add(onScreenText);

        scoreText = new Text(0, 50, "0");
        scoreText.setFill(Color.WHITE);
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFont(new Font(50));
        scoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().add(scoreText);

        shade = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        final String dataPath = "data/space";
        final ImageView restartImage = new ImageView(dataPath + "/menu/restart.png");
        restartImage.setFitHeight(dimension2D.getHeight() / 6);
        restartImage.setFitWidth(dimension2D.getHeight() / 6);
        restartButton.setImage(restartImage);
        restartButton.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getHeight() / 12);
        restartButton.setLayoutY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 12);
        restartButton.assignIndicatorUpdatable(event -> launch(), gameContext);

        finalScoreText = new Text(0, dimension2D.getHeight() / 4, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        gameContext.getGazeDeviceManager().addEventFilter(restartButton);

        gazeTarget = new Point2D(dimension2D.getWidth() / 2, dimension2D.getHeight() / 2);

        interactionOverlay = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());

        final EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                gazeTarget = new Point2D(((GazeEvent) event).getX(), ((GazeEvent) event).getY());
            }
        };

        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);

        gameContext.getGazeDeviceManager().addEventFilter(interactionOverlay);

        this.bibouleValue = 0;
        this.bossHit = 0;
        this.bulletListRec = new ArrayList<>();
        this.biboulesKilled = new ArrayList<>();
        this.biboulesPos = new ArrayList<>();
        this.bulletBibouleListRec = new ArrayList<>();
        this.spaceshipDestroyed = new ArrayList<>();
        this.bosses = new ArrayList<>();
        this.bossKilled = new ArrayList<>();
        this.bulletBossListRec = new ArrayList<>();
    }

    public SpaceGame(final IGameContext gameContext, final SpaceGameStats stats, double gameSeed, SpaceGameVariant gameVariant) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        this.gameContext.startTimeLimiter();
        this.gameContext.startScoreLimiter();
        this.random = new ReplayablePseudoRandom(gameSeed);
        this.gameVariant = gameVariant;

        spaceshipImage = ImageUtils.createCustomizedImageLibrary(null, "space/spaceship", random);
        bibouleImage = ImageUtils.createCustomizedImageLibrary(null, "space/biboule", random);

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        this.biboules = new ArrayList<>();
        this.bibouleWidth = dimension2D.getWidth() / 20;
        this.bibouleHeight = dimension2D.getHeight() / 10;
        this.bossWidth = dimension2D.getWidth() / 8;
        this.bossHeight = dimension2D.getHeight() / 4;

        this.translate = MultilinguismFactory.getSingleton();

        final Label onScreenText = new Label();
        foregroundLayer.getChildren().add(onScreenText);

        scoreText = new Text(0, 50, "0");
        scoreText.setFill(Color.WHITE);
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFont(new Font(50));
        scoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().add(scoreText);

        shade = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        final String dataPath = "data/space";
        final ImageView restartImage = new ImageView(dataPath + "/menu/restart.png");
        restartImage.setFitHeight(dimension2D.getHeight() / 6);
        restartImage.setFitWidth(dimension2D.getHeight() / 6);
        restartButton.setImage(restartImage);
        restartButton.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getHeight() / 12);
        restartButton.setLayoutY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 12);
        restartButton.assignIndicatorUpdatable(event -> launch(), gameContext);

        finalScoreText = new Text(0, dimension2D.getHeight() / 4, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        gameContext.getGazeDeviceManager().addEventFilter(restartButton);

        gazeTarget = new Point2D(dimension2D.getWidth() / 2, dimension2D.getHeight() / 2);

        interactionOverlay = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());

        final EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                gazeTarget = new Point2D(((GazeEvent) event).getX(), ((GazeEvent) event).getY());
            }
        };

        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);

        gameContext.getGazeDeviceManager().addEventFilter(interactionOverlay);

        this.bibouleValue = 0;
        this.bossHit = 0;
        this.bulletListRec = new ArrayList<>();
        this.biboulesKilled = new ArrayList<>();
        this.biboulesPos = new ArrayList<>();
        this.bulletBibouleListRec = new ArrayList<>();
        this.spaceshipDestroyed = new ArrayList<>();
        this.bosses = new ArrayList<>();
        this.bossKilled = new ArrayList<>();
        this.bulletBossListRec = new ArrayList<>();
    }

    @Override
    public void launch() {
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

        gameContext.setLimiterAvailable();

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();
        gameContext.getChildren().clear();
        bulletListRec.clear();
        biboulesKilled.clear();
        biboulesPos.clear();
        bulletBibouleListRec.clear();
        spaceshipDestroyed.clear();
        bulletBossListRec.clear();
        bosses.clear();
        bossKilled.clear();

        gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        final Rectangle backgroundImage = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        backgroundImage.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage.setFill(new ImagePattern(new Image("data/space/background/space_img.png")));

        final Rectangle backgroundImage2 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        backgroundImage2.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage2.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage2.setFill(new ImagePattern(new Image("data/space/background/space_img.png")));

        final Rectangle backgroundImage3 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        backgroundImage3.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage3.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage3.setFill(new ImagePattern(new Image("data/space/background/space_img.png")));

        backgroundImage.setOpacity(0.08);
        backgroundImage2.setOpacity(0.08);
        backgroundImage3.setOpacity(0.4);

        backgroundLayer.getChildren().add(backgroundImage);
        backgroundLayer.getChildren().add(backgroundImage2);
        backgroundLayer.getChildren().add(backgroundImage3);
        backgroundImage.toFront();
        backgroundImage2.toFront();
        backgroundImage3.toBack();

        final TranslateTransition translateTransition = new TranslateTransition(Duration.millis(10000), backgroundImage);
        translateTransition.setFromY(0);
        translateTransition.setToY(dimension2D.getHeight());
        translateTransition.setInterpolator(Interpolator.LINEAR);

        final TranslateTransition translateTransition2 = new TranslateTransition(Duration.millis(10000), backgroundImage2);
        translateTransition2.setFromY(-dimension2D.getHeight());
        translateTransition2.setToY(0);
        translateTransition2.setInterpolator(Interpolator.LINEAR);

        final ParallelTransition parallelTransition = new ParallelTransition(translateTransition, translateTransition2);
        parallelTransition.setCycleCount(Animation.INDEFINITE);
        parallelTransition.play();

        spaceship = new Rectangle(dimension2D.getWidth() / 2, 6 * dimension2D.getHeight() / 7,
            dimension2D.getWidth() / 8, dimension2D.getHeight() / 7);
        this.middleLayer.getChildren().add(spaceship);
        ImagePattern imageShip = new ImagePattern(spaceshipImage.pickRandomImage());
        spaceship.setFill(imageShip);

        if (imageShip.getImage().getUrl().contains("spaceship3.gif")) {
            spaceshipCollider = new Rectangle(spaceship.getX() + spaceship.getWidth() / 3,
                spaceship.getY() + spaceship.getHeight() / 3, spaceship.getWidth() * 3 / 4, spaceship.getHeight() / 5);
            backgroundLayer.getChildren().add(spaceshipCollider);
            spaceshipCollider.setFill(Color.TRANSPARENT);
        } else if (imageShip.getImage().getUrl().contains("spaceship.gif")) {
            spaceshipCollider = new Rectangle(spaceship.getX() - 500,
                spaceship.getY() + spaceship.getHeight() / 3, spaceship.getWidth() * 4 / 5, spaceship.getHeight() / 5);
            backgroundLayer.getChildren().add(spaceshipCollider);
            spaceshipCollider.setFill(Color.TRANSPARENT);
        } else {
            //case when the image is not detected
            spaceshipCollider = new Rectangle(spaceship.getX() + spaceship.getWidth() / 3,
                spaceship.getY() + spaceship.getHeight() / 3, spaceship.getWidth() / 3, spaceship.getHeight() / 5);
            backgroundLayer.getChildren().add(spaceshipCollider);
            spaceshipCollider.setFill(Color.YELLOW);
        }

        velocity = Point2D.ZERO;
        score = 0;
        lastTickTime = 0;
        gazeTarget = new Point2D(dimension2D.getWidth() / 2, 0);
        bossHit = 0;


        spawnBiboule();

        updatePosition();


        score = biboulesKilled.size() + bossKilled.size() * 125;
        stats.incrementNumberOfGoalsReached(score);
        scoreText.setText(String.valueOf(score));
        scoreText.setX(dimension2D.getWidth() / 2 - scoreText.getWrappingWidth() / 2);

        this.start();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.start();

        gameContext.setOffFixationLengthControl();
    }


    @Override
    public void handle(final long now) {

        if (lastTickTime == 0) {
            lastTickTime = now;
        }

        double timeElapsed = ((double) now - (double) lastTickTime) / Math.pow(10, 6); // in ms
        lastTickTime = now;


        if (1000 / timeElapsed < minFPS) {
            minFPS = 1000 / (int) timeElapsed;
        }
        timeElapsed /= getGameSpeed();

        shipMovement();

        spaceship.setX(spaceship.getX() + velocity.getX() * timeElapsed);

        spaceshipCollider.setX(spaceship.getX() + spaceship.getWidth() / 10);
        spaceshipCollider.setY(spaceship.getY() + spaceship.getHeight() / 2);

        verticalBibouleMovement();

        computeBulletPlayer();
        computeBulletBiboule();

        if (biboules.size() == 0) {
            spawnBiboule();
        }
        if (bosses.size() == 0 && biboulesKilled.size() % 25 == 24) {
            displayBoss();
        }
        computeBulletBoss();

    }

    private void removeAll() {

        bulletListRec.clear();
        bulletBibouleListRec.clear();
        biboules.clear();
        bulletBossListRec.clear();
        middleLayer.getChildren().remove(spaceship);
    }

    private void updateShipPosition() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        spaceship.setY(6 * dimension2D.getHeight() / 7);
    }

    private void shipMovement() {
        final double distance = Math.floor(Math.abs(gazeTarget.getX() - (spaceship.getX() + spaceship.getWidth() / 2)));
        final double direction = distance <= 5 ? 0
            : (gazeTarget.getX() - (spaceship.getX() + spaceship.getWidth() / 2)) / distance;
        shipSpeed(distance, direction);
    }

    private void shipSpeed(double distance, double direction) {
        final double maxSpeed = 0.7;
        if (distance > maxSpeed) {
            velocity = new Point2D(maxSpeed * direction, velocity.getY());
        } else {
            velocity = new Point2D(0 * direction, velocity.getY());
        }
    }

    private void verticalBibouleMovement() {
        bibouleValue += 1;
        if (bibouleValue == 300) {
            for (final Biboule b : biboules) {
                final boolean lower = random.nextBoolean();
                if (lower) {
                    b.moveToLower(biboulesPos.get(0).getX(), biboulesPos.get(0).getY());
                } else {
                    b.moveToUpper(biboulesPos.get(0).getX(), 50);
                    if (b.getY() <= 50) {
                        b.moveToLower(biboulesPos.get(0).getX(), biboulesPos.get(0).getY());
                    }
                }
            }

            bibouleValue = 0;
        }
    }

    private void spawnBiboule() {
        while (biboules.size() < 10) {
            displayBiboule();
        }
    }

    @Override
    public void dispose() {

    }

    private double getGameSpeed() {
        final double speed = gameContext.getAnimationSpeedRatioSource().getDurationRatio();
        return Math.max(speed, 1.0);
    }

    private int getsetHighscore(final int score) {
        final File f = new File(GazePlayDirectories.getUserStatsFolder(configuration.getUserName()), "/space-game/highscore.dat");
        try {
            ArrayList<Integer> highscores = new ArrayList();
            if (!f.createNewFile()) {
                final Scanner scanner = new Scanner(f, StandardCharsets.UTF_8);
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

            final Writer writer = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8);
            for (final int i : highscores) {
                writer.write(i + ":");
            }
            writer.close();

            return highscores.get(highscores.size() - 1);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        return score;
    }

    private void death() {
        this.stop();

        // Show restart and score
        interactionOverlay.setDisable(true);
        shade.setOpacity(1);
        final int highscore = getsetHighscore(score);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(translate.getTranslation("Score", configuration.getLanguage())).append(translate.getTranslation("Colon", configuration.getLanguage())).append(" ").append(score).append("\n");
        stringBuilder.append(translate.getTranslation("Highscore", configuration.getLanguage())).append(translate.getTranslation("Colon", configuration.getLanguage())).append(" ").append(highscore).append("\n");
        if (highscore <= score) {
            stringBuilder.append(translate.getTranslation("New highscore!", configuration.getLanguage()));
        }
        finalScoreText.setText(stringBuilder.toString());
        finalScoreText.setOpacity(1);
        restartButton.active();
        stats.addRoundDuration();
    }

    private void updateScore() {
        score = biboulesKilled.size() + bossKilled.size() * 125;
        stats.incrementNumberOfGoalsReached(score);
        scoreText.setText(String.valueOf(score));
        scoreText.setX(dimension2D.getWidth() / 2 - scoreText.getWrappingWidth() / 2);
        gameContext.updateScore(stats, this);
    }

    private void createBiboule(final double x, final double y) {
        final Biboule b;
        b = new Biboule(x, y, bibouleWidth, bibouleHeight, null, dimension2D.getWidth(), getGameSpeed(), 0, 0, 0, 0);

        biboules.add(b);
        b.setFill(new ImagePattern(bibouleImage.pickRandomImage()));
        backgroundLayer.getChildren().add(b);
        //code for transition when biboule appear, bug sometimes when the game restart
        /*
        final FadeTransition bibouleAppear = new FadeTransition(Duration.seconds(1), b);
        bibouleAppear.setInterpolator(Interpolator.LINEAR);
        bibouleAppear.setCycleCount(1);
        bibouleAppear.setFromValue(0);
        bibouleAppear.setToValue(1);
        bibouleAppear.play();
        */
    }

    private void createBoss(final double x, final double y) {
        final Boss boss;
        boss = new Boss(x, y, bossWidth, bossHeight, null, dimension2D.getWidth(), getGameSpeed(), 0, 0, 0, 0);
        boss.setFill(new ImagePattern(new Image("data/space/enemy/boss.gif")));
        bosses.add(boss);
        backgroundLayer.getChildren().add(boss);
        final FadeTransition bossAppear = new FadeTransition(Duration.seconds(1), boss);
        bossAppear.setInterpolator(Interpolator.LINEAR);
        bossAppear.setCycleCount(1);
        bossAppear.setFromValue(0);
        bossAppear.setToValue(1);
        bossAppear.play();
    }

    private void displayBoss() {
        final double bossX;
        final double bossY;
        bossX = random.nextInt((int) (dimension2D.getWidth()));
        bossY = random.nextInt((int) (dimension2D.getHeight() / 2));
        createBoss(bossX, bossY);
    }

    private void displayBiboule() {
        final double newBibX;
        final double newBibY;
        newBibX = random.nextInt((int) (dimension2D.getWidth()));
        newBibY = random.nextInt((int) (dimension2D.getHeight() / 4));
        createBiboule(newBibX, newBibY);
    }

    private void updatePosition() {
        final Point2D p1 = new Point2D(200, 200);
        final Point2D p2 = new Point2D(300, 300);
        biboulesPos.add(p1);
        biboulesPos.add(p2);
    }

    private void computeBulletPlayer() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= 500) {

            final Rectangle bulletRec = new Rectangle(spaceship.getX() + spaceship.getWidth() / 2,
                spaceship.getY() - spaceship.getHeight() / 3, spaceship.getHeight() / 12, spaceship.getHeight() / 6);
            bulletRec.setFill(new ImagePattern(new Image("data/space/bullet/laserBlue01.png")));
            middleLayer.getChildren().add(bulletRec);
            bulletListRec.add(bulletRec);

            double bulletTransitionTime;
            switch(gameVariant){
                case EASY:
                    bulletTransitionTime = 0.5;
                    break;
                case NORMAL:
                    bulletTransitionTime = 2;
                    break;
                case HARD:
                    bulletTransitionTime = 4;
                    break;
                default:
                    bulletTransitionTime = 0.5;
            }
            bulletTransition = new TranslateTransition(Duration.seconds(bulletTransitionTime), bulletRec);
            bulletTransition.setToY(-1 * dimension2D.getHeight());
            bulletTransition.setCycleCount(1);
            bulletTransition.setInterpolator(Interpolator.LINEAR);
            bulletTransition.setOnFinished(event -> {
                bulletListRec.remove(bulletRec);
                middleLayer.getChildren().remove(bulletRec);
                //this fonction is usefull to remove freezes from bullets. He is commented because of gradle dislike this fonction so we can't merge with this.
                //System.gc();
            });

            bulletTransition.play();
            startTime = currentTime;
        }

        for (final Rectangle r : bulletListRec) {
            for (final Biboule b : biboules) {
                final ObservableBooleanValue colliding = Bindings.createBooleanBinding(() -> r.getBoundsInParent().intersects(b.getBoundsInParent()), r.boundsInParentProperty(), b.boundsInParentProperty());

                colliding.addListener((obs, oldValue, newValue) -> {
                    if (newValue) {

                        boolean bibouleBoolean = false;

                        if (!biboulesKilled.contains(b)) {
                            biboulesKilled.add(b);
                            bibouleBoolean = true;
                        }
                        if (bibouleBoolean) {

                            bulletListRec.remove(r);
                            biboules.remove(b);
                            backgroundLayer.getChildren().remove(b);
                            middleLayer.getChildren().remove(r);
                            updateScore();
                        }

                    }

                });
            }
            for (final Boss b : bosses) {
                final ObservableBooleanValue colliding2 = Bindings.createBooleanBinding(() -> r.getBoundsInParent().intersects(b.getBoundsInParent()), r.boundsInParentProperty(), b.boundsInParentProperty());

                colliding2.addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        bulletListRec.remove(r);

                        boolean bossHitBoolean = false;
                        boolean bossKilledBoolean = false;

                        if (bossHit < b.getHealthPoint()) {

                            if (!bossKilled.contains(b)) {
                                bossHitBoolean = true;
                            }

                            if (bossHitBoolean) {
                                bossHit += 1;
                                bossFade = new FadeTransition(Duration.millis(250), b);
                                bossFade.setFromValue(1);
                                bossFade.setToValue(0.5);
                                bossFade.setCycleCount(1);
                                bossFade.setInterpolator(Interpolator.LINEAR);

                                bossFade2 = new FadeTransition(Duration.millis(250), b);
                                bossFade2.setFromValue(0.5);
                                bossFade2.setToValue(1);
                                bossFade2.setCycleCount(1);
                                bossFade.setInterpolator(Interpolator.LINEAR);

                                sequentialTransition2 = new SequentialTransition(bossFade, bossFade2);

                                bulletDisappear = new FadeTransition(Duration.millis(100), r);
                                bulletDisappear.setFromValue(1);
                                bulletDisappear.setToValue(0);
                                bulletDisappear.setCycleCount(1);
                                bulletDisappear.setInterpolator(Interpolator.LINEAR);

                                parallelTransition4 = new ParallelTransition(sequentialTransition2,
                                    bulletDisappear);
                                parallelTransition4.play();
                            }
                        } else {
                            if (!bossKilled.contains(b)) {
                                bossKilled.add(b);
                                bossKilledBoolean = true;
                            }

                            if (bossKilledBoolean) {
                                bossDisappear = new FadeTransition(Duration.millis(1000), b);
                                bossDisappear.setFromValue(1);
                                bossDisappear.setToValue(0);
                                bossDisappear.setCycleCount(1);
                                bossDisappear.setInterpolator(Interpolator.LINEAR);

                                bulletDisappear = new FadeTransition(Duration.millis(100), r);
                                bulletDisappear.setFromValue(1);
                                bulletDisappear.setToValue(0);
                                bulletDisappear.setCycleCount(1);
                                bulletDisappear.setInterpolator(Interpolator.LINEAR);

                                parallelTransition4 = new ParallelTransition(bossDisappear,
                                    bulletDisappear);
                                parallelTransition4.play();

                                bosses.remove(b);
                                middleLayer.getChildren().remove(r);
                                backgroundLayer.getChildren().remove(b);
                                bossHit = 0;
                                updateScore();
                            }
                        }

                    }

                });

                if (r.getY() < 0) {
                    bulletListRec.remove(r);
                    backgroundLayer.getChildren().remove(r);
                }
            }
        }
        updateShipPosition();
    }

    private void computeBulletBiboule() {
        for (final Rectangle b : biboules) {
            final int bibouleShoot = random.nextInt(1500);

            final Rectangle bulletBibouleRec = new Rectangle(b.getX() + b.getWidth() / 2, b.getY(), spaceship.getHeight() / 12, spaceship.getHeight() / 6);
            bulletBibouleRec.setFill(new ImagePattern(new Image("data/space/bullet/laserRed01.png")));

            if (bibouleShoot == 1) {
                backgroundLayer.getChildren().add(bulletBibouleRec);
                bulletBibouleListRec.add(bulletBibouleRec);

                final Timeline timeline = new Timeline();
                timeline.setCycleCount(1);
                timeline.getKeyFrames()
                    .add(new KeyFrame(Duration.seconds(15), new KeyValue(bulletBibouleRec.translateYProperty(),
                        dimension2D.getHeight(), Interpolator.LINEAR)));
                timeline.setOnFinished(event -> {
                    bulletBibouleListRec.remove(bulletBibouleRec);
                    backgroundLayer.getChildren().remove(bulletBibouleRec);
                    //System.gc();
                });
                timeline.play();

            }

            for (final Rectangle rb : bulletBibouleListRec) {
                final ObservableBooleanValue collidingBulletBibSpaceship = Bindings
                    .createBooleanBinding(() -> rb.getBoundsInParent().intersects(spaceshipCollider.getBoundsInParent()), rb.boundsInParentProperty(), spaceshipCollider.boundsInParentProperty());

                collidingBulletBibSpaceship.addListener((obs, oldValue, newValue) -> {
                    if (newValue) {

                        boolean deathBoolean = false;

                        if (!spaceshipDestroyed.contains(spaceship)) {
                            spaceshipDestroyed.add(spaceship);
                            deathBoolean = true;
                        }

                        if (deathBoolean) {
                            removeAll();
                            death();
                            spaceshipDestroyed.remove(spaceship);
                        }

                    }
                });
            }
        }
    }

    private void computeBulletBoss() {
        for (final Rectangle b : bosses) {
            final int bossShoot = random.nextInt(240);

            final Rectangle bulletBossRec = new Rectangle(b.getX() + b.getWidth() / 2, b.getY(), spaceship.getHeight() / 8, spaceship.getHeight() / 4);
            bulletBossRec.setFill(new ImagePattern(new Image("data/space/bullet/laserRed01.png")));

            if (bossShoot == 1) {
                backgroundLayer.getChildren().add(bulletBossRec);
                bulletBossListRec.add(bulletBossRec);

                final Timeline timeline = new Timeline();
                timeline.setCycleCount(1);
                timeline.getKeyFrames()
                    .add(new KeyFrame(Duration.seconds(15), new KeyValue(bulletBossRec.translateYProperty(),
                        dimension2D.getHeight(), Interpolator.LINEAR)));
                timeline.setOnFinished(event -> {
                    bulletBossListRec.remove(bulletBossRec);
                    backgroundLayer.getChildren().remove(bulletBossRec);
                    //System.gc();
                });
                timeline.play();

            }

            for (final Rectangle rboss : bulletBossListRec) {
                final ObservableBooleanValue collidingBulletBossSpaceship = Bindings
                    .createBooleanBinding(() -> rboss.getBoundsInParent().intersects(spaceshipCollider.getBoundsInParent()), rboss.boundsInParentProperty(), spaceshipCollider.boundsInParentProperty());

                collidingBulletBossSpaceship.addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        log.info("die");

                        boolean deathBoolean = false;

                        if (!spaceshipDestroyed.contains(spaceship)) {
                            spaceshipDestroyed.add(spaceship);
                            deathBoolean = true;
                        }

                        if (deathBoolean) {
                            backgroundLayer.getChildren().remove(rboss);
                            removeAll();
                            death();
                            spaceshipDestroyed.remove(spaceship);
                        }
                    }
                });
            }
        }
    }
}

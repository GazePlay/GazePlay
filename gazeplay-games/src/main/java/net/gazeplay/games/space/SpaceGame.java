package net.gazeplay.games.space;

import javafx.animation.*;
import javafx.application.Platform;
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
import javafx.scene.layout.StackPane;
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
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.components.ProgressButton;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

@Slf4j
public class SpaceGame extends AnimationTimer implements GameLifeCycle {

    private final SpaceGameStats spaceGameStats;
    private final Dimension2D dimension2D;
    private final Random random;
    private final Configuration configuration;

    private final ImageLibrary spaceshipImage;
    private final ImageLibrary bibouleImage;

    private final Group backgroundLayer;
    private final Group middleLayer;
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

    private Rectangle spaceship;
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
    private ParallelTransition parallelTransition;
    private ParallelTransition parallelTransition2;
    private ParallelTransition parallelTransition3;
    private ParallelTransition parallelTransition4;
    private SequentialTransition sequentialTransition2;
    private FadeTransition bibouleDisappear;
    private FadeTransition bulletDisappear;
    private FadeTransition spaceshipDisappear;
    private FadeTransition bulletBibouleDisappear;
    private FadeTransition bossDisappear;
    private FadeTransition bossFade;
    private FadeTransition bossFade2;
    private FadeTransition bulletBossDisappear;

    private int bulletValue;
    private int bibouleValue;
    private int bibouleBulletValue;
    private int bossBulletValue;
    private final ArrayList<Rectangle> bulletListRec;
    private final ArrayList<Rectangle> bulletBibouleListRec;
    private final ArrayList<Rectangle> bulletBossListRec;
    private final ArrayList<Rectangle> spaceshipDestroyed;

    private int bossHit;

    public SpaceGame(final IGameContext gameContext, final SpaceGameStats stats) {
        this.spaceGameStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.random = new Random();
        this.configuration = gameContext.getConfiguration();

        spaceshipImage = ImageUtils.createCustomizedImageLibrary(null, "space/spaceship");
        bibouleImage = ImageUtils.createCustomizedImageLibrary(null, "space/biboule");

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        final StackPane sp = new StackPane();
        gameContext.getChildren().addAll(sp, backgroundLayer, middleLayer, foregroundLayer);

        this.biboules = new ArrayList<>();
        this.bibouleWidth = dimension2D.getWidth() / 20;
        this.bibouleHeight = dimension2D.getHeight() / 10;
        this.bossWidth = dimension2D.getWidth() / 8;
        this.bossHeight = dimension2D.getHeight() / 4;

        this.translate = Multilinguism.getSingleton();

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

        sp.getChildren().add(backgroundImage);
        sp.getChildren().add(backgroundImage2);
        sp.getChildren().add(backgroundImage3);
        backgroundImage.toFront();
        backgroundImage2.toBack();
        backgroundImage3.toBack();

        final TranslateTransition translateTransition = new TranslateTransition(Duration.millis(10000), backgroundImage);
        translateTransition.setFromY(0);
        translateTransition.setToY(dimension2D.getHeight());
        translateTransition.setInterpolator(Interpolator.LINEAR);

        final TranslateTransition translateTransition2 = new TranslateTransition(Duration.millis(10000), backgroundImage2);
        translateTransition2.setFromY(0);
        translateTransition2.setToY(dimension2D.getHeight());
        translateTransition2.setInterpolator(Interpolator.LINEAR);

        final SequentialTransition sequentialTransition = new SequentialTransition(translateTransition, translateTransition2);
        sequentialTransition.setCycleCount(Animation.INDEFINITE);
        sequentialTransition.play();

        final Label onScreenText = new Label();
        foregroundLayer.getChildren().add(onScreenText);

        scoreText = new Text(0, 50, "0");
        scoreText.setFill(Color.WHITE);
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFont(new Font(50));
        scoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().add(scoreText);

        // Menu
        final int fixationLength = configuration.getFixationLength();

        shade = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        final String DATA_PATH = "data/space";
        final ImageView restartImage = new ImageView(DATA_PATH + "/menu/restart.png");
        restartImage.setFitHeight(dimension2D.getHeight() / 6);
        restartImage.setFitWidth(dimension2D.getHeight() / 6);
        restartButton.setImage(restartImage);
        restartButton.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getHeight() / 12);
        restartButton.setLayoutY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 12);
        restartButton.assignIndicator(event -> launch(), fixationLength);

        finalScoreText = new Text(0, dimension2D.getHeight() / 4, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        gameContext.getGazeDeviceManager().addEventFilter(restartButton);

        // Interaction
        gazeTarget = new Point2D(dimension2D.getWidth() / 2, dimension2D.getHeight() / 2);

        interactionOverlay = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());

        final EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                gazeTarget = interactionOverlay.screenToLocal(((GazeEvent) event).getX(), ((GazeEvent) event).getY());
            }
        };

        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);

        gameContext.getGazeDeviceManager().addEventFilter(interactionOverlay);

        this.bulletValue = 0;
        this.bibouleValue = 0;
        this.bossBulletValue = 0;
        this.bibouleBulletValue = 0;
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
        // hide end game menu
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();
        bulletListRec.clear();
        biboulesKilled.clear();
        biboulesPos.clear();
        bulletBibouleListRec.clear();
        spaceshipDestroyed.clear();
        bulletBossListRec.clear();
        bosses.clear();
        bossKilled.clear();
        // for (Timeline t : timelineList){
        // t.stop();
        // }

        spaceship = new Rectangle(dimension2D.getWidth() / 2, 6 * dimension2D.getHeight() / 7,
            dimension2D.getWidth() / 8, dimension2D.getHeight() / 7);
        this.middleLayer.getChildren().add(spaceship);
        // spaceship.setFill(new ImagePattern(new Image("data/space/spaceship/spaceship.gif")));
        spaceship.setFill(new ImagePattern(spaceshipImage.pickRandomImage()));

        velocity = Point2D.ZERO;
        score = 0;
        lastTickTime = 0;
        gazeTarget = new Point2D(dimension2D.getWidth() / 2, 0);
        bossHit = 0;

        for (int i = 0; i < 10; i++) {
            displayBiboule();
        }
        // displayBoss();

        updatePosition();
        updateScore();

        this.start();

        spaceGameStats.notifyNewRoundReady();
    }

    @Override
    public void handle(final long now) {

        if (lastTickTime == 0) {
            lastTickTime = now;
        }

        double timeElapsed = ((double) now - (double) lastTickTime) / Math.pow(10, 6); // in ms
        lastTickTime = now;

        // log.info("FPS: " + (int) (1000 / timeElapsed));
        if (1000 / timeElapsed < minFPS) {
            minFPS = 1000 / (int) timeElapsed;
        }
        timeElapsed /= getGameSpeed();
        // log.info("Speed effect: " + configuration.getSpeedEffects());
        // log.info("MinFPS: " + minFPS);
        // log.info("Time elapsed: " + timeElapsed);

        // Movement
        /// Lateral movement: Mouse Moved
        final double distance = Math.abs(gazeTarget.getX() - (spaceship.getX() + spaceship.getWidth() / 2));
        final double direction = distance == 0 ? 1
            : (gazeTarget.getX() - (spaceship.getX() + spaceship.getWidth() / 2)) / distance;
        final double maxSpeed = 0.7;
        if (distance > maxSpeed) {
            velocity = new Point2D(maxSpeed * direction, velocity.getY());
        } else {
            velocity = new Point2D(0 * direction, velocity.getY());
        }

        spaceship.setX(spaceship.getX() + velocity.getX() * timeElapsed);
        // log.info("velocity x: " + velocity.getX());
        spaceship.setY(6 * dimension2D.getHeight() / 7);
        // log.info("Number of bullets: " + bulletListRec.size());
        // log.info("Number of biboules: " + biboules.size());
        // log.info("Number of biboule bullets: " + bulletBibouleListRec.size());
        // log.info("Number of timeline biboule bullets: " + timelineList.size());
        // log.info("Number of bullet - transition pair: " + hashMap.size());

        /// Lateral movement: Mouse Pressed
        // spaceship.setX(gazeTarget.getX() - spaceship.getWidth() / 2);
        // spaceship.setY(gazeTarget.getY() - spaceship.getHeight() / 2);

        bibouleValue += 1;
        if (bibouleValue == 300) {
            for (final Biboule b : biboules) {
                final boolean lower = random.nextBoolean();
                if (lower) {
                    b.moveToLower(biboulesPos.get(0).getX(), biboulesPos.get(0).getY());
                } else {
                    b.moveToUpper(biboulesPos.get(0).getX(), biboulesPos.get(0).getY());
                    if (b.getY() <= 50) {
                        b.moveToLower(biboulesPos.get(0).getX(), biboulesPos.get(0).getY());
                    }
                }
            }

            bibouleValue = 0;
        }

        bulletValue += 1;
        if (bulletValue == 20) {
            final Rectangle bulletRec = new Rectangle(spaceship.getX() + spaceship.getWidth() / 2,
                spaceship.getY() - spaceship.getHeight() / 3, 10, 20);
            bulletRec.setFill(new ImagePattern(new Image("data/space/bullet/laserBlue01.png")));
            middleLayer.getChildren().add(bulletRec);
            bulletListRec.add(bulletRec);

            bulletTransition = new TranslateTransition(Duration.seconds(3), bulletRec);
            bulletTransition.setToY(-1 * dimension2D.getHeight());
            bulletTransition.setCycleCount(1);
            bulletTransition.setInterpolator(Interpolator.LINEAR);
            // bulletTransition.setOnFinished(event -> bulletListRec.remove(bulletRec));
            bulletTransition.setOnFinished(event -> {
                bulletListRec.remove(bulletRec);
                middleLayer.getChildren().remove(bulletRec);
            });

            bulletTransition.play();
            bulletValue = 0;
        }
        // bulletValue = 0;

        for (final Rectangle r : bulletListRec) {
            // r.setY(r.getY() - 4);
            // Rectangle realBullet = new Rectangle(r.getX() + r.getWidth()/3, r.getY() + r.getHeight()/3,
            // r.getWidth()/3,r.getHeight()/3);
            for (final Biboule b : biboules) {
                // for (Rectangle r : bulletListRec) {
                final ObservableBooleanValue colliding = Bindings.createBooleanBinding(() -> {
                    return r.getBoundsInParent().intersects(b.getBoundsInParent());
                    // return realBullet.getBoundsInParent().intersects(b.getBoundsInParent());
                }, r.boundsInParentProperty(), b.boundsInParentProperty());

                colliding.addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        // log.info("Colliding");

                        boolean bibouleBoolean = false;

                        if (!biboulesKilled.contains(b)) {
                            biboulesKilled.add(b);
                            bibouleBoolean = true;
                        }
                        // bulletListRec.remove(r);
                        // biboules.remove(b);
                        if (bibouleBoolean) {
                            // bibouleDisappear = new FadeTransition(Duration.millis(1000), b);
                            // bibouleDisappear.setFromValue(1);
                            // bibouleDisappear.setToValue(0);
                            // bibouleDisappear.setCycleCount(1);
                            // bibouleDisappear.setInterpolator(Interpolator.LINEAR);
                            //
                            // bulletDisappear = new FadeTransition(Duration.millis(100), r);
                            // bulletDisappear.setFromValue(1);
                            // bulletDisappear.setToValue(0);
                            // bulletDisappear.setCycleCount(1);
                            // bulletDisappear.setInterpolator(Interpolator.LINEAR);
                            //
                            // parallelTransition = new ParallelTransition(bibouleDisappear, bulletDisappear);
                            // parallelTransition.play();

                            bulletListRec.remove(r);
                            biboules.remove(b);
                            backgroundLayer.getChildren().remove(b);
                            middleLayer.getChildren().remove(r);
                            updateScore();
                        }

                    }
                    // else {
                    // log.info("Not colliding");
                    // }
                });
                // }
            }
            // updateScore();
        }
        // }

        // bibouleBulletValue += 1;
        // if (bibouleBulletValue == 120) {
        final Rectangle spaceshipCollider = new Rectangle(spaceship.getX() + spaceship.getWidth() / 3,
            spaceship.getY() + spaceship.getHeight() * 2 / 3, spaceship.getWidth() / 3, spaceship.getHeight() / 3);
        backgroundLayer.getChildren().add(spaceshipCollider);
        spaceshipCollider.setFill(Color.TRANSPARENT);

        for (final Rectangle b : biboules) {
            final int bibouleShoot = random.nextInt(1200);

            final Rectangle bulletBibouleRec = new Rectangle(b.getX() + b.getWidth() / 2, b.getY(), 10, 20);
            bulletBibouleRec.setFill(new ImagePattern(new Image("data/space/bullet/laserRed01.png")));

            if (bibouleShoot == 1) {
                backgroundLayer.getChildren().add(bulletBibouleRec);
                bulletBibouleListRec.add(bulletBibouleRec);

                final Timeline timeline = new Timeline();
                timeline.setCycleCount(1);
                timeline.getKeyFrames()
                    .add(new KeyFrame(Duration.seconds(15), new KeyValue(bulletBibouleRec.translateYProperty(),
                        dimension2D.getHeight(), Interpolator.LINEAR)));
                // timeline.setOnFinished(event -> bulletBibouleListRec.remove(bulletBibouleRec));
                timeline.setOnFinished(event -> {
                    bulletBibouleListRec.remove(bulletBibouleRec);
                    backgroundLayer.getChildren().remove(bulletBibouleRec);
                });
                timeline.play();

            }

            for (final Rectangle rb : bulletBibouleListRec) {
                final ObservableBooleanValue collidingBulletBibSpaceship = Bindings
                    .createBooleanBinding(() -> rb.getBoundsInParent().intersects(spaceshipCollider.getBoundsInParent()), rb.boundsInParentProperty(), spaceshipCollider.boundsInParentProperty());

                collidingBulletBibSpaceship.addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        log.info("die");

                        boolean deathBoolean = false;

                        if (!spaceshipDestroyed.contains(spaceship)) {
                            spaceshipDestroyed.add(spaceship);
                            deathBoolean = true;
                        }

                        if (deathBoolean) {
                            bulletBibouleListRec.remove(rb);

                            // spaceshipDisappear = new FadeTransition(Duration.millis(1000), spaceship);
                            // spaceshipDisappear.setFromValue(1);
                            // spaceshipDisappear.setToValue(0);
                            // spaceshipDisappear.setCycleCount(1);
                            // spaceshipDisappear.setInterpolator(Interpolator.LINEAR);
                            //
                            // bulletBibouleDisappear = new FadeTransition(Duration.millis(100), rb);
                            // bulletBibouleDisappear.setFromValue(1);
                            // bulletBibouleDisappear.setToValue(0);
                            // bulletBibouleDisappear.setCycleCount(1);
                            // bulletBibouleDisappear.setInterpolator(Interpolator.LINEAR);
                            //
                            // parallelTransition2 = new ParallelTransition(spaceshipDisappear,
                            // bulletBibouleDisappear);
                            //
                            // parallelTransition2.play();
                            middleLayer.getChildren().remove(spaceship);
                            backgroundLayer.getChildren().remove(rb);
                            death();
                        }

                    } else {
                        log.info("Not die");
                    }
                });
            }
        }

        // bibouleBulletValue = 0;
        // }

        if (biboules.size() < 5) {
            while (biboules.size() != 15) {
                displayBiboule();
            }
        }

        // ex.execute(new Runnable() {
        // @Override
        // public  void run() {
        // computeBulletPlayer();
        // }
        // });

        // ex.execute((new Runnable() {
        // @Override
        // public  void run() {
        // computeBulletBiboule();
        // }
        // }));

        // bossValue += 1;
        // if (bossValue == 3000) {
        // if (bosses.size() < 1) {
        // displayBoss();
        // }
        // bossValue = 0;
        // }
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

            final Writer writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
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
        stringBuilder.append(translate.getTrad("Score", configuration.getLanguage())).append(translate.getTrad("Colon", configuration.getLanguage())).append(" ").append(score).append("\n");
        stringBuilder.append(translate.getTrad("Highscore", configuration.getLanguage())).append(translate.getTrad("Colon", configuration.getLanguage())).append(" ").append(highscore).append("\n");
        if (highscore <= score) {
            stringBuilder.append(translate.getTrad("New highscore!", configuration.getLanguage()));
        }
        finalScoreText.setText(stringBuilder.toString());
        finalScoreText.setOpacity(1);
        restartButton.active();
        spaceGameStats.addRoundDuration();
    }

    private void updateScore() {
        score = biboulesKilled.size() + +bossKilled.size() * 125;
        spaceGameStats.incNbGoals(score);
        scoreText.setText(String.valueOf(biboulesKilled.size() + bossKilled.size() * 125));
        scoreText.setX(dimension2D.getWidth() / 2 - scoreText.getWrappingWidth() / 2);
    }

    private void createBiboule(final double x, final double y) {
        final Biboule b;
        b = new Biboule(x, y, bibouleWidth, bibouleHeight, null, dimension2D.getWidth(), getGameSpeed(), 0, 0, 0, 0);

        biboules.add(b);
        b.setFill(new ImagePattern(bibouleImage.pickRandomImage()));
        // b.setFill(new ImagePattern(new Image("data/space/enemy/boss.gif")));
        backgroundLayer.getChildren().add(b);
        final FadeTransition bibouleAppear = new FadeTransition(Duration.seconds(1), b);
        bibouleAppear.setInterpolator(Interpolator.LINEAR);
        bibouleAppear.setCycleCount(1);
        bibouleAppear.setFromValue(0);
        bibouleAppear.setToValue(1);
        bibouleAppear.play();
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
        Platform.runLater(() -> {
            bulletValue += 1;
            if (bulletValue == 30) {
                final Rectangle bulletRec = new Rectangle(spaceship.getX() + spaceship.getWidth() / 2,
                    spaceship.getY() - spaceship.getHeight() / 3, 10, 20);
                bulletRec.setFill(new ImagePattern(new Image("data/space/bullet/laserBlue01.png")));

                bulletTransition = new TranslateTransition(Duration.seconds(3), bulletRec);
                bulletTransition.setToY(-1 * dimension2D.getHeight());
                bulletTransition.setCycleCount(1);
                bulletTransition.setInterpolator(Interpolator.LINEAR);

                middleLayer.getChildren().add(bulletRec);
                bulletListRec.add(bulletRec);
                bulletTransition.play();
                bulletValue = 0;

                for (final Biboule b : biboules) {
                    for (final Rectangle r : bulletListRec) {
                        final ObservableBooleanValue colliding = Bindings.createBooleanBinding(() -> r.getBoundsInParent().intersects(b.getBoundsInParent()), r.boundsInParentProperty(), b.boundsInParentProperty());

                        colliding.addListener((obs, oldValue, newValue) -> {
                            if (newValue) {
                                log.info("Colliding");

                                boolean bibouleBoolean = false;

                                if (!biboulesKilled.contains(b)) {
                                    biboulesKilled.add(b);
                                    bibouleBoolean = true;
                                }
                                bulletListRec.remove(r);
                                biboules.remove(b);
                                if (bibouleBoolean) {
                                    bibouleDisappear = new FadeTransition(Duration.millis(1000), b);
                                    bibouleDisappear.setFromValue(1);
                                    bibouleDisappear.setToValue(0);
                                    bibouleDisappear.setCycleCount(1);
                                    bibouleDisappear.setInterpolator(Interpolator.LINEAR);

                                    bulletDisappear = new FadeTransition(Duration.millis(100), r);
                                    bulletDisappear.setFromValue(1);
                                    bulletDisappear.setToValue(0);
                                    bulletDisappear.setCycleCount(1);
                                    bulletDisappear.setInterpolator(Interpolator.LINEAR);

                                    parallelTransition = new ParallelTransition(bibouleDisappear,
                                        bulletDisappear);
                                    parallelTransition.play();

                                    backgroundLayer.getChildren().remove(b);
                                    middleLayer.getChildren().remove(r);
                                }

                            } else {
                                log.info("Not colliding");
                            }
                        });

                        if (r.getY() < 0) {
                            bulletListRec.remove(r);
                            backgroundLayer.getChildren().remove(r);
                        }
                    }
                }

                updateScore();
            }
        });
    }

    private void computeBulletBiboule() {
        Platform.runLater(() -> {
            bibouleBulletValue += 1;
            if (bibouleBulletValue == 120) {
                final Rectangle spaceshipCollider = new Rectangle(spaceship.getX() + spaceship.getWidth() / 3,
                    spaceship.getY() + spaceship.getHeight() * 2 / 3, spaceship.getWidth() / 3,
                    spaceship.getHeight() / 3);
                backgroundLayer.getChildren().add(spaceshipCollider);
                spaceshipCollider.setFill(Color.TRANSPARENT);

                for (final Rectangle b : biboules) {
                    final int bibouleShoot = random.nextInt(20);

                    final Rectangle bulletBibouleRec = new Rectangle(b.getX() + b.getWidth() / 2, b.getY(), 10, 20);
                    bulletBibouleRec.setFill(new ImagePattern(new Image("data/space/bullet/laserRed01.png")));

                    if (bibouleShoot == 1) {
                        backgroundLayer.getChildren().add(bulletBibouleRec);
                        bulletBibouleListRec.add(bulletBibouleRec);

                        final Timeline timeline = new Timeline();
                        timeline.setCycleCount(1);
                        timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(15),
                                new KeyValue(bulletBibouleRec.translateYProperty(), dimension2D.getHeight(),
                                    Interpolator.LINEAR)));
                        timeline.play();

                    }

                    for (final Rectangle rb : bulletBibouleListRec) {
                        final ObservableBooleanValue collidingBulletBibSpaceship = Bindings
                            .createBooleanBinding(() -> rb.getBoundsInParent()
                                .intersects(spaceshipCollider.getBoundsInParent()), rb.boundsInParentProperty(), spaceshipCollider.boundsInParentProperty());

                        collidingBulletBibSpaceship.addListener((obs, oldValue, newValue) -> {
                            if (newValue) {
                                log.info("die");

                                boolean deathBoolean = false;

                                if (!spaceshipDestroyed.contains(spaceship)) {
                                    spaceshipDestroyed.add(spaceship);
                                    deathBoolean = true;
                                }

                                if (deathBoolean) {
                                    bulletBibouleListRec.remove(rb);

                                    spaceshipDisappear = new FadeTransition(Duration.millis(1000), spaceship);
                                    spaceshipDisappear.setFromValue(1);
                                    spaceshipDisappear.setToValue(0);
                                    spaceshipDisappear.setCycleCount(1);
                                    spaceshipDisappear.setInterpolator(Interpolator.LINEAR);

                                    bulletBibouleDisappear = new FadeTransition(Duration.millis(100), rb);
                                    bulletBibouleDisappear.setFromValue(1);
                                    bulletBibouleDisappear.setToValue(0);
                                    bulletBibouleDisappear.setCycleCount(1);
                                    bulletBibouleDisappear.setInterpolator(Interpolator.LINEAR);

                                    parallelTransition2 = new ParallelTransition(spaceshipDisappear,
                                        bulletBibouleDisappear);

                                    parallelTransition2.play();
                                    backgroundLayer.getChildren().remove(rb);
                                    death();
                                }

                            } else {
                                log.info("Not die");
                            }
                        });

                        if (rb.getY() >= dimension2D.getHeight()) {
                            bulletBibouleListRec.remove(rb);
                            backgroundLayer.getChildren().remove(rb);
                        }
                    }
                }
                bibouleBulletValue = 0;
            }
        });
    }

    private void computeBulletBoss() {
        Platform.runLater(() -> {
            bossBulletValue += 1;
            if (bossBulletValue == 120) {
                final Rectangle spaceshipCollider = new Rectangle(spaceship.getX() + spaceship.getWidth() / 3,
                    spaceship.getY() + spaceship.getHeight() * 2 / 3, spaceship.getWidth() / 3,
                    spaceship.getHeight() / 3);
                backgroundLayer.getChildren().add(spaceshipCollider);
                spaceshipCollider.setFill(Color.TRANSPARENT);

                for (final Rectangle b : bosses) {
                    final int bossShoot = random.nextInt(2);

                    final Rectangle bulletBossRec = new Rectangle(b.getX() + b.getWidth() / 2, b.getY(), 15, 30);
                    bulletBossRec.setFill(new ImagePattern(new Image("data/space/bullet/laserRed01.png")));

                    if (bossShoot == 1) {
                        backgroundLayer.getChildren().add(bulletBossRec);
                        bulletBossListRec.add(bulletBossRec);

                        final Timeline timeline = new Timeline();
                        timeline.setCycleCount(1);
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(15), new KeyValue(
                            bulletBossRec.translateYProperty(), dimension2D.getHeight(), Interpolator.LINEAR)));
                        timeline.play();

                    }

                    for (final Rectangle rboss : bulletBossListRec) {
                        final ObservableBooleanValue collidingBulletBibSpaceship = Bindings
                            .createBooleanBinding(() -> rboss.getBoundsInParent()
                                .intersects(spaceshipCollider.getBoundsInParent()), rboss.boundsInParentProperty(), spaceshipCollider.boundsInParentProperty());

                        collidingBulletBibSpaceship.addListener((obs, oldValue, newValue) -> {
                            if (newValue) {
                                log.info("die");

                                boolean deathBoolean = false;

                                if (!spaceshipDestroyed.contains(spaceship)) {
                                    spaceshipDestroyed.add(spaceship);
                                    deathBoolean = true;
                                }

                                if (deathBoolean) {
                                    bulletBossListRec.remove(rboss);

                                    spaceshipDisappear = new FadeTransition(Duration.millis(1000), spaceship);
                                    spaceshipDisappear.setFromValue(1);
                                    spaceshipDisappear.setToValue(0);
                                    spaceshipDisappear.setCycleCount(1);
                                    spaceshipDisappear.setInterpolator(Interpolator.LINEAR);

                                    bulletBossDisappear = new FadeTransition(Duration.millis(100), rboss);
                                    bulletBossDisappear.setFromValue(1);
                                    bulletBossDisappear.setToValue(0);
                                    bulletBossDisappear.setCycleCount(1);
                                    bulletBossDisappear.setInterpolator(Interpolator.LINEAR);

                                    parallelTransition3 = new ParallelTransition(spaceshipDisappear,
                                        bulletBossDisappear);

                                    parallelTransition3.play();
                                    backgroundLayer.getChildren().remove(rboss);

                                    death();
                                }
                                // }

                            } else {
                                log.info("Not die");
                            }
                        });

                        if (rboss.getY() >= dimension2D.getHeight()) {
                            bulletBibouleListRec.remove(rboss);
                            backgroundLayer.getChildren().remove(rboss);
                        }
                    }
                }
                bossBulletValue = 0;

            }
        });
    }

    private void computeBulletPlayerBoss() {
        Platform.runLater(() -> {
            bulletValue += 1;
            if (bulletValue == 30) {
                final Rectangle bulletRec = new Rectangle(spaceship.getX() + spaceship.getWidth() / 2,
                    spaceship.getY() - spaceship.getHeight() / 3, 10, 20);
                bulletRec.setFill(new ImagePattern(new Image("data/space/bullet/laserBlue01.png")));

                bulletTransition = new TranslateTransition(Duration.seconds(3), bulletRec);
                bulletTransition.setToY(-1 * dimension2D.getHeight());
                bulletTransition.setCycleCount(1);
                bulletTransition.setInterpolator(Interpolator.LINEAR);

                middleLayer.getChildren().add(bulletRec);
                bulletListRec.add(bulletRec);

                bulletTransition.play();
                bulletValue = 0;

                for (final Boss b : bosses) {
                    for (final Rectangle r : bulletListRec) {
                        final ObservableBooleanValue colliding2 = Bindings.createBooleanBinding(() -> r.getBoundsInParent().intersects(b.getBoundsInParent()), r.boundsInParentProperty(), b.boundsInParentProperty());

                        colliding2.addListener((obs, oldValue, newValue) -> {
                            if (newValue) {
                                log.info("Hit the boss");

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
                                    bosses.remove(b);

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
                                        middleLayer.getChildren().remove(r);
                                        backgroundLayer.getChildren().remove(b);
                                        bossHit = 0;
                                        updateScore();
                                    }
                                }

                            } else {
                                log.info("Not hit the boss");
                            }
                        });

                        if (r.getY() < 0) {
                            bulletListRec.remove(r);
                            backgroundLayer.getChildren().remove(r);
                        }
                    }
                }
            }
        });
    }
}

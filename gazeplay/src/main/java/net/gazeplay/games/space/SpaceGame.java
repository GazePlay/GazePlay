package net.gazeplay.games.space;

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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

@Slf4j
public class SpaceGame extends AnimationTimer implements GameLifeCycle {
    private static String DATA_PATH = "data/space";

    private static final int MAX_RADIUS = 70;
    private static final int MIN_RADIUS = 40;

    private static final int MAX_TIME_LENGTH = 7;
    private static final int MIN_TIME_LENGTH = 4;

    private final GameContext gameContext;
    private final SpaceGameStats spaceGameStats;
    private final Dimension2D dimension2D;
    private final Random random;
    private final Configuration configuration;

    private final ImageLibrary spaceshipImage;
    private final ImageLibrary bibouleImage;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Group foregroundLayer;
    private final Rectangle interactionOverlay;
    private final StackPane sp;

    private Point2D gazeTarget;
    private Point2D velocity;
    private final double maxSpeed = 0.7;

    private final double bibouleWidth;
    private final double bibouleHeight;
    private Rectangle biboulePosition;

    private long lastTickTime = 0;
    private long minFPS = 1000;

    private Rectangle spaceship;
    private Label onScreenText;
    private Text scoreText;
    private ArrayList<Biboule> biboules;

    private int score;
    private Boolean left;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private Text finalScoreText;
    private final int fixationLength;

    private final Multilinguism translate;

    private TranslateTransition t;
    private TranslateTransition t1;
    private Transition transition;
    private ParallelTransition parallelTransition;
    private SequentialTransition sequentialTransition;
    private PauseTransition p1;
    private Timeline timeline;

    private double newSpaceshipX;
    private double newSpaceshipY;
//    private final Point[] endPoints;

    public SpaceGame(GameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.spaceGameStats = (SpaceGameStats) stats;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.random = new Random();
        this.configuration = Configuration.getInstance();

        spaceshipImage = ImageUtils.createCustomizedImageLibrary(null, "space/spaceship");
        bibouleImage = ImageUtils.createCustomizedImageLibrary(null, "space/biboule");

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        this.sp = new StackPane();
        this.gameContext.getChildren().addAll(sp, backgroundLayer, middleLayer, foregroundLayer);

        this.biboules = new ArrayList();
        this.bibouleWidth = dimension2D.getWidth()/20;
        this.bibouleHeight = dimension2D.getHeight()/10;

        this.translate = Multilinguism.getSingleton();

        Rectangle backgroundImage = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        backgroundImage.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage.setFill(new ImagePattern(new Image("data/space/background/space_img.png")));

        Rectangle backgroundImage2 = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        backgroundImage2.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage2.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage2.setFill(new ImagePattern(new Image("data/space/background/space_img.png")));

        Rectangle backgroundImage3 = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
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

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(10000), backgroundImage);
        translateTransition.setFromY(0);
        translateTransition.setToY(dimension2D.getHeight());
        translateTransition.setInterpolator(Interpolator.LINEAR);

        TranslateTransition translateTransition2 = new TranslateTransition(Duration.millis(10000), backgroundImage2);
        translateTransition2.setFromY(0);
        translateTransition2.setToY(dimension2D.getHeight());
        translateTransition2.setInterpolator(Interpolator.LINEAR);

        sequentialTransition = new SequentialTransition(translateTransition, translateTransition2);
        sequentialTransition.setCycleCount(Animation.INDEFINITE);
        sequentialTransition.play();

        onScreenText = new Label();
        foregroundLayer.getChildren().add(onScreenText);

        scoreText = new Text(0, 50, "0");
        scoreText.setFill(Color.WHITE);
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFont(new Font(50));
        scoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().add(scoreText);

        // Menu
        fixationLength = configuration.getFixationLength();

        shade = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        ImageView restartImage = new ImageView(DATA_PATH + "/menu/restart.png");
        restartImage.setFitHeight(dimension2D.getHeight() / 6);
        restartImage.setFitWidth(dimension2D.getHeight() / 6);
        restartButton.setImage(restartImage);
        restartButton.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getHeight() / 12);
        restartButton.setLayoutY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 12);
        restartButton.assignIndicator(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                launch();
            }
        }, fixationLength);

        finalScoreText = new Text(0, dimension2D.getHeight() / 4, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        this.gameContext.getGazeDeviceManager().addEventFilter(restartButton);

        // Interaction
        gazeTarget = new Point2D(dimension2D.getWidth()/2, dimension2D.getHeight()/2);

        interactionOverlay = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());

        EventHandler<Event> movementEvent = (Event event) -> {
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

        this.gameContext.getGazeDeviceManager().addEventFilter(interactionOverlay);

    }

    @Override
    public void launch(){
        // hide end game menu
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

//        backgroundLayer.getChildren().removeAll(biboules);
        this.middleLayer.getChildren().clear();

        spaceship = new Rectangle(dimension2D.getWidth()/2,dimension2D.getHeight()/2,
                dimension2D.getWidth()/8, dimension2D.getHeight()/8);
        this.middleLayer.getChildren().add(spaceship);
        spaceship.setFill(new ImagePattern(spaceshipImage.pickRandomImage()));

        velocity = Point2D.ZERO;
        score = 0;
        lastTickTime = 0;
        gazeTarget = new Point2D(dimension2D.getWidth()/2,dimension2D.getHeight()/2);

        createBiboule(spaceship.getX() + spaceship.getWidth() / 2, spaceship.getY() + spaceship.getHeight() + bibouleHeight,
                false);

//        displayBiboule(dimension2D.getHeight());
        displayBiboule();

        this.start();

        spaceGameStats.notifyNewRoundReady();
    }

    @Override
    public void dispose(){


    }

    public double getGameSpeed(){
        double speed = configuration.getSpeedEffects();
        return speed <= 1.0 ? 1.0 : speed;
    }

    private int getsetHighscore(int score){
        File f = new File(Utils.getUserStatsFolder(configuration.getUserName()) + "/Space Game/highscore.dat");
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

    private void death(){
        this.stop();

        // Show end menu (restart, quit, score)
        interactionOverlay.setDisable(true);
        shade.setOpacity(1);
        int highscore = getsetHighscore(score);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(translate.getTrad("Score", configuration.getLanguage()) + translate.getTrad("Colon", configuration.getLanguage())
                + " " + score + "\n");
        stringBuilder.append(translate.getTrad("Highscore", configuration.getLanguage())
                + translate.getTrad("Colon", configuration.getLanguage()) + " " + highscore + "\n");
        if (highscore <= score)
            stringBuilder.append(translate.getTrad("New highscore!", configuration.getLanguage()));
        finalScoreText.setText(stringBuilder.toString());
        finalScoreText.setOpacity(1);
        restartButton.active();
        spaceGameStats.addRoundDuration();
    }

    @Override
    public void handle(long now){
        Biboule b;
        if(lastTickTime == 0){
            lastTickTime = now;
        }

        double timeElapsed = ((double) now - (double) lastTickTime)/Math.pow(10,6); // in ms
        System.out.println(timeElapsed);
        lastTickTime = now;

        String logs = "FPS: " + (int)(1000/timeElapsed) + "\n";
        if(1000/timeElapsed < minFPS){
            minFPS = 1000/(int)timeElapsed;
        }
        logs += "MinFPS: " + minFPS + "\n";
        logs += "Time elasped -- Real: " + timeElapsed;
        timeElapsed /= getGameSpeed();
        System.out.println(timeElapsed);
        logs += timeElapsed + "\n";
        logs += "Speed effect: " + configuration.getSpeedEffects() + "\n";

        // Movement
        /// Lateral movement
        spaceship.setX(gazeTarget.getX() - spaceship.getWidth()/2);
        spaceship.setY(gazeTarget.getY() - spaceship.getHeight()/2);

//        Rectangle bulletRec = new Rectangle(10,20);
//        bulletRec.setFill(new ImagePattern(new Image("data/space/bullet/laserBlue01.png")));

        ImageView bulletView = new ImageView();
        bulletView.setImage(new Image("data/space/bullet/laserBlue01.png"));
//
        t = new TranslateTransition(Duration.seconds(5), bulletView);
        t.setFromX(gazeTarget.getX());
        t.setFromY(gazeTarget.getY() - spaceship.getHeight());
        t.setToY(-1 * dimension2D.getHeight());
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);

//        p1 = new PauseTransition();
//        p1.setDuration(Duration.seconds(1));

//        sequentialTransition = new SequentialTransition(t, p1);

        this.middleLayer.getChildren().add(bulletView);
        t.play();
//        sequentialTransition.setCycleCount(Animation.INDEFINITE);
//        sequentialTransition.play();
////        parallelTransition.play();

    }

    private void createBiboule(double centerX, double centerY, boolean moving) {
        Biboule p;
        p = new Biboule(centerX - bibouleWidth/2, centerY - bibouleHeight / 2, bibouleWidth,
                    bibouleHeight, null, dimension2D.getWidth(),
                getGameSpeed(), 0.5, 0, 0, 0);

//        biboulePosition = p;
        biboules.add(p);
        p.setFill(new ImagePattern(bibouleImage.pickRandomImage()));
        backgroundLayer.getChildren().add(p);
    }

    private void displayBiboule() {
//        double top = -dimension2D.getHeight();
//        double bottom = bottomLimit;
//        while (bottom > top) {
            double newBibX;
            double newBibY;
//            do {
                newBibX = random.nextInt((int) (dimension2D.getWidth()));
                newBibY = random.nextInt((int) (dimension2D.getHeight()));
//            } while (Math.abs(newBibX - biboulePosition.getX()) >= dimension2D.getWidth() / 3);

//            if (random.nextInt(4) == 0) {
                createBiboule(newBibX, newBibY, true);
//            }

//            bottom = newBibY - 2 * bibouleHeight;
    }
}

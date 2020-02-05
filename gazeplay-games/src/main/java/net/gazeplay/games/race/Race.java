package net.gazeplay.games.race;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class Race extends Parent implements GameLifeCycle {

    private static final int MIN_RADIUS = 40;

    private static final int MAX_TIME_LENGTH = 7;
    private static final int MIN_TIME_LENGTH = 4;

    private static final int MAX_RACE_TIME_LENGTH = 30;
    private static final int MIN_RACE_TIME_LENGTH = 15;

    private final IGameContext gameContext;

    private final Image blue;
    private final Image green;
    private final Image yellow;
    private final Image orange;
    private final Image red;
    private final Image flash;
    private final Image racer;

    private final int bugsAmount = 14;
    private int movementPerBug = 2;

    private Target playerRacer;
    private boolean raceIsFinished = false;
    private int racerMovement = 0;

    private Label text;
    private int score;

    private StackPane hand;
    private final ImageView cage;

    private final String gameType;

    private final Stats stats;

    private final Point[] endPoints;

    private final EventHandler<Event> enterEvent;
    private final EventHandler<GazeEvent> handEventGaze;
    private Dimension2D dimension2D;
    private final Target[] racers;

    // done
    public Race(final IGameContext gameContext, final Stats stats, final String type) {
        this.gameContext = gameContext;
        this.stats = stats;
        score = 0;
        gameType = type;

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        hand = new StackPane();

        final Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(new ImagePattern(new Image("data/" + gameType + "/images/Background.jpg")));

        final int coef = (gameContext.getConfiguration().isBackgroundWhite()) ? 1 : 0;
        imageRectangle.setOpacity(1 - coef * 0.9);

        gameContext.getChildren().add(imageRectangle);
        gameContext.getChildren().add(this);

        final EventHandler<MouseEvent> handEvent = e -> {
            if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                final double x = e.getX();
                final double y = e.getY();
                hand.setRotate(getAngle(new Point(x, y)));
            }
        };

        racers = new Target[3];

        handEventGaze = e -> {
            final double x = e.getX();
            final double y = e.getY();
            hand.setRotate(getAngle(new Point(x, y)));
        };
        imageRectangle.addEventFilter(MouseEvent.ANY, handEvent);
        this.addEventFilter(GazeEvent.ANY, handEventGaze);

        blue = new Image("data/" + gameType + "/images/Blue.png");
        green = new Image("data/" + gameType + "/images/Green.png");
        yellow = new Image("data/" + gameType + "/images/Yellow.png");
        orange = new Image("data/" + gameType + "/images/Orange.png");
        red = new Image("data/" + gameType + "/images/Red.png");
        racer = new Image("data/" + gameType + "/images/frogJump.gif");
        flash = new Image("data/" + gameType + "/images/Flash.png");
        cage = new ImageView(new Image("data/" + gameType + "/images/Cage.png"));

        final Point[] points = new Point[8];
        // init all points
        for (int i = 0; i < points.length; ++i) {
            points[i] = new Point(0, 0);
        }

        this.endPoints = points;
        // then update them
        updatePoints(imageRectangle);

        gameContext.getRoot().widthProperty().addListener((observable, oldValue, newValue) -> {
            updatePoints(imageRectangle);
        });
        gameContext.getRoot().heightProperty().addListener((observable, oldValue, newValue) -> {
            updatePoints(imageRectangle);
        });

        enterEvent = e -> {
            if (e.getTarget() instanceof Target) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    if (!((Target) e.getTarget()).done && !raceIsFinished) {
                        ((Target) e.getTarget()).done = true;
                        enter((Target) e.getTarget());
                        stats.incNbGoals();
                        stats.notifyNewRoundReady();
                    }
                }
            }
        };

    }

    private void updatePoints(final Rectangle rectangle) {

        endPoints[1].y = rectangle.getHeight();

        endPoints[2].x = rectangle.getWidth() / 2;
        endPoints[2].y = rectangle.getHeight();

        endPoints[3].x = rectangle.getWidth() * 0.96;

        endPoints[4].x = rectangle.getWidth() * 0.96;
        endPoints[4].y = rectangle.getHeight() / 2;

        endPoints[5].y = rectangle.getHeight() / 2;

        endPoints[6].x = rectangle.getWidth() / 2;

        endPoints[7].x = rectangle.getWidth() * 0.96;
        endPoints[7].y = rectangle.getHeight();
    }

    public float getAngle(final Point target) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        float angle = (float) Math
            .toDegrees(Math.atan2(target.x - (dimension2D.getWidth() / 2), -target.y + (dimension2D.getHeight())));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public void updateHand() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double x = dimension2D.getHeight();
        for (final Node child : hand.getChildren()) {
            ((ImageView) child).setPreserveRatio(true);
            ((ImageView) child).setFitHeight(x);
        }
        hand.setLayoutY(0);
        hand.setLayoutX(3 * (dimension2D.getWidth() / 7));
        final double cst2;
        if (gameType.equals("biboule")) {
            cst2 = 2;
        } else {// equals robot
            cst2 = 1.7;
        }
        hand.setLayoutY(dimension2D.getHeight() / cst2);
    }

    public void updateCage() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        cage.setLayoutX(8.5 * dimension2D.getWidth() / 29.7);

        final double y;
        if (gameType.equals("biboule")) {
            cage.setLayoutY(8.5 * dimension2D.getHeight() / 21);
            y = dimension2D.getHeight() / 6.5;
        } else {// equals robot
            cage.setLayoutY(3.5 * dimension2D.getHeight() / 21);
            y = dimension2D.getHeight() / 8.5;
        }
        cage.setFitHeight(y);
        cage.setPreserveRatio(true);
    }

    public void updateScore(final Label sc, final Label tc) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        sc.setFont(Font.font(dimension2D.getHeight() / 20));
        sc.setLayoutX(dimension2D.getWidth() * 0.8);
        sc.setLayoutY(dimension2D.getHeight() * 0.5);

        // tc.setFont(Font.font(dimension2D.getHeight() / 20));
        tc.setLayoutX(dimension2D.getWidth() * 0.8);
        tc.setLayoutY(dimension2D.getHeight() * 0.4);

    }

    @Override
    public void launch() {

        final Label sc = new Label();
        final Label tc = new Label();

        final String cst;

        tc.setText("Score:");
        tc.setTextFill(Color.WHITE);
        cst = "" + score;

        sc.setText(cst);
        sc.setTextFill(Color.WHITE);
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final ImageView iv1 = new ImageView(new Image("data/" + gameType + "/images/hand.png"));
        final ImageView iv2 = new ImageView(new Image("data/" + gameType + "/images/handShot.png"));

        final StackPane iv = new StackPane();
        final double x = dimension2D.getHeight();
        iv1.setPreserveRatio(true);
        iv1.setFitHeight(x);
        iv2.setPreserveRatio(true);
        iv2.setFitHeight(x);

        iv.getChildren().addAll(iv1, iv2);
        iv.getChildren().get(1).setOpacity(0);
        iv.setLayoutY(0);
        iv.setLayoutX(3 * (dimension2D.getWidth() / 7));

        iv.setLayoutY(dimension2D.getHeight() / 2);

        this.getChildren().add(iv);
        hand = (StackPane) this.getChildren().get(this.getChildren().indexOf(iv));
        hand.toFront();

        tc.setFont(Font.font(dimension2D.getHeight() / 20));
        sc.setFont(Font.font(dimension2D.getHeight() / 20));
        tc.setLineSpacing(10);
        tc.setLayoutX(15 * dimension2D.getWidth() / 29.7);
        tc.setLayoutY(14 * dimension2D.getHeight() / 21);

        this.getChildren().add(tc);

        text = sc;

        this.getChildren().add(sc);

        this.gameContext.resetBordersToFront();
        iv.setMouseTransparent(true);

        updateCage();

        gameContext.getRoot().widthProperty().addListener((observable, oldValue, newValue) -> {
            updateScore(sc, tc);
            updateCage();
            updateHand();
        });
        gameContext.getRoot().heightProperty().addListener((observable, oldValue, newValue) -> {
            updateScore(sc, tc);
            updateCage();
            updateHand();
        });

        cage.toBack();
        this.getChildren().add(cage);

        final Timeline waitbeforestart = new Timeline();
        waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        waitbeforestart.setOnFinished(actionEvent -> {
            if (!raceIsFinished) {
                for (int i = 0; i < bugsAmount; i++) {
                    newCircle();
                }
            }

            makePlayer(0.6);
            racers[0] = makeRacers(0.7);
            racers[1] = makeRacers(0.8);
            racers[2] = makeRacers(0.9);
        });
        waitbeforestart.play();

        stats.notifyNewRoundReady();

    }

    // done
    @Override
    public void dispose() {
        this.removeEventFilter(GazeEvent.ANY, handEventGaze);
        this.getChildren().clear();
    }

    private Transition restartTransition(final Target t) {

        final FadeTransition ft = new FadeTransition(Duration.millis(1), t);
        ft.setFromValue(0);
        ft.setToValue(1);

        final TranslateTransition tt1 = new TranslateTransition(Duration.millis(1), t);
        tt1.setToY(0);
        tt1.setToX(0);

        final ScaleTransition st = new ScaleTransition(Duration.millis(1), t);
        st.setToX(1);
        st.setToY(1);

        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft, tt1, st);

        return pt;
    }

    private void enter(final Target t) {
        t.removeEventFilter(MouseEvent.ANY, enterEvent);
        t.removeEventFilter(GazeEvent.ANY, enterEvent);
        t.t.stop();

        final String cst;
        cst = "" + ++score;

        text.setText(cst);

        final String soundResource = "data/race/sounds/frog.WAV";
        try {
            ForegroundSoundsUtils.playSound(soundResource);
        } catch (final Exception e) {
            log.warn("Can't play sound: no associated sound : " + e.toString());
        }

        t.getChildren().get(0).setOpacity(1);

        hand.getChildren().get(1).setOpacity(1);
        final FadeTransition ft = new FadeTransition(Duration.millis(500), t);
        ft.setFromValue(1);
        ft.setToValue(0);
        final FadeTransition ft2 = new FadeTransition(Duration.millis(500), hand.getChildren().get(1));
        ft2.setFromValue(1);
        ft2.setToValue(0);

        if (t.animDone) {
            t.animDone = false;
            final ScaleTransition st = new ScaleTransition(Duration.millis(100), hand);
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(0.7);
            st.setToY(0.7);
            final ScaleTransition st2 = new ScaleTransition(Duration.millis(100), hand);
            st.setFromX(0.7);
            st.setFromY(0.7);
            st2.setToX(1);
            st2.setToY(1);
            final SequentialTransition seqt = new SequentialTransition();
            seqt.getChildren().addAll(st, st2);
            seqt.setOnFinished(actionEvent -> t.animDone = true);
            seqt.play();
        }

        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft, ft2);
        pt.setOnFinished(actionEvent -> {
            final int i = getChildren().indexOf(t);
            if (i != -1) {
                getChildren().remove(i);
            }
            if (!raceIsFinished) {
                newCircle();
            }
        });
        pt.play();
        if (score % movementPerBug == 0) {
            racerMovement++;
            movePlayer(playerRacer, racerMovement);
        }
        if (racerMovement == 18) {
            racerMovement = 0;
            raceIsFinished = true;

            gameContext.playWinTransition(500, actionEvent -> {

                movementPerBug++;
                raceFinished();
                gameContext.endWinTransition();
                raceIsFinished = false;
                makePlayer(0.6);
                racers[0] = makeRacers(0.7);
                racers[1] = makeRacers(0.8);
                racers[2] = makeRacers(0.9);
                for (int i = 0; i < bugsAmount; i++) {
                    newCircle();
                }
            });
        }

    }

    private void newCircle() {

        final Target sp = buildCircle();
        sp.toBack();
        this.getChildren().add(sp);
        gameContext.getGazeDeviceManager().addEventFilter(sp);

        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        final double yMinRange = dimension2D.getHeight() * 0.1;
        final double yMaxRange = dimension2D.getHeight() * 0.25;

        final double x = (Math.random() * (dimension2D.getWidth() * 0.9));
        sp.setLayoutX(x);
        final double y = Math.random() * yMaxRange + yMinRange;
        sp.setLayoutY(y);
        sp.centerX = x;
        sp.centerY = y;
        stats.incNbShots();
        moveCircle(sp);
    }

    private void makePlayer(final double racerPosition) {

        playerRacer = buildRacer(100);
        playerRacer.toBack();
        this.getChildren().add(playerRacer);
        final double x = 0;
        playerRacer.setLayoutX(x);
        final double y = dimension2D.getHeight() * racerPosition;
        playerRacer.setLayoutY(y);
        playerRacer.centerX = x;
        playerRacer.centerY = y;

    }

    private void movePlayer(final Target frogRacer, final int iteration) {
        final double timelength = 1000;
        final double movementDistance = iteration * 0.05;

        final TranslateTransition tt1 = new TranslateTransition(new Duration(timelength), frogRacer);
        tt1.setToX(dimension2D.getWidth() * movementDistance);

        final ScaleTransition st = new ScaleTransition(new Duration(timelength), frogRacer);
        st.setByX(1);
        st.setByY(1);

        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(tt1);
        pt.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        frogRacer.t = pt;

        pt.play();
    }

    private void raceFinished() {
        for (int i = 0; i < 3; i++) {
            getChildren().remove(racers[i]);
        }
        getChildren().remove(playerRacer);
        final int index = (getChildren().indexOf(racers[0]));
        if (index != -1) {
            getChildren().remove(index);
        }
    }

    private Target makeRacers(final double racerPosition) {

        final Target frogRacer = buildRacer(70);
        frogRacer.toBack();
        this.getChildren().add(frogRacer);
        frogRacer.setLayoutX(0);

        final double y = dimension2D.getHeight() * racerPosition;
        frogRacer.setLayoutY(y);
        frogRacer.centerX = 0;
        frogRacer.centerY = y;

        final TranslateTransition tt1 = new TranslateTransition(new Duration(((MAX_RACE_TIME_LENGTH - MIN_RACE_TIME_LENGTH) * Math.random() + MIN_RACE_TIME_LENGTH)
            * 1000), frogRacer);
        tt1.setToX(dimension2D.getWidth() - dimension2D.getWidth() * 0.1);
        final ScaleTransition st = new ScaleTransition(new Duration(((MAX_RACE_TIME_LENGTH - MIN_RACE_TIME_LENGTH) * Math.random() + MIN_RACE_TIME_LENGTH)
            * 1000), frogRacer);
        st.setByX(1);
        st.setByY(1);
        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(tt1);
        pt.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        frogRacer.t = pt;
        pt.setOnFinished(event -> {

        });

        pt.play();

        return frogRacer;
    }

    private void resize(final ImageView i) {
        final double d = MIN_RADIUS;
        i.setFitHeight(d);
        i.setFitWidth(d * 5 / 4);
    }

    private void resizeRacer(final ImageView i, final double size) {
        i.setFitHeight(size);
        i.setFitWidth(size * 5 / 4);
    }

    private Target buildRacer(final double racerSize) {

        final Target sp = new Target();
        final ImageView race = new ImageView(racer);
        resizeRacer(race, racerSize);
        sp.getChildren().addAll(race);
        return sp;
    }

    private Target buildCircle() {

        final Target sp = new Target();
        final ImageView b1 = new ImageView(blue);
        final ImageView b2 = new ImageView(green);
        final ImageView b3 = new ImageView(yellow);
        final ImageView b4 = new ImageView(orange);
        final ImageView b5 = new ImageView(red);
        final ImageView f = new ImageView(flash);
        final ImageView race = new ImageView(racer);

        resize(b1);
        resize(b2);
        resize(b3);
        resize(b4);
        resize(b5);
        resize(race);

        resize(f);

        sp.getChildren().addAll(f, b1, b2, b3, b4, b5, race);
        sp.getChildren().get(0).setOpacity(0);
        sp.getChildren().get(5).setOpacity(0);
        sp.getChildren().get(2).setOpacity(0);
        sp.getChildren().get(3).setOpacity(0);
        sp.getChildren().get(4).setOpacity(0);
        sp.getChildren().get(6).setOpacity(0);

        return sp;
    }

    private void moveCircle(final Target sp) {
        final double timelength = ((MAX_TIME_LENGTH - MIN_TIME_LENGTH) * Math.random() + MIN_TIME_LENGTH) * 1000;

        final TranslateTransition tt1 = new TranslateTransition(new Duration(timelength), sp);
        final double min = Math.ceil(0);
        final double max = Math.floor(endPoints.length - 1);
        final int r = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
        final Point randomPoint = endPoints[r];
        tt1.setToY((-sp.centerY + randomPoint.y) / 4);
        tt1.setToX(-sp.centerX + randomPoint.x);

        if (r == 2) {
            this.getChildren().get(this.getChildren().indexOf(sp)).toFront();
        } else {
            this.getChildren().get(this.getChildren().indexOf(sp)).toBack();
            text.toBack();
            cage.toBack();
        }

        final ScaleTransition st = new ScaleTransition(new Duration(timelength), sp);
        st.setByX(1);
        st.setByY(1);
        final ParallelTransition pt = new ParallelTransition();

        final FadeTransition btog = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(2));
        final FadeTransition gtoy = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(3));
        final FadeTransition ytoo = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(4));
        final FadeTransition otor = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(5));

        btog.setFromValue(0);
        gtoy.setFromValue(0);
        ytoo.setFromValue(0);
        otor.setFromValue(0);

        btog.setToValue(1);
        gtoy.setToValue(1);
        ytoo.setToValue(1);
        otor.setToValue(1);

        final SequentialTransition seqt = new SequentialTransition(btog);

        pt.getChildren().addAll(seqt, tt1, st);
        sp.t = pt;

        pt.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        pt.setOnFinished(actionEvent -> {
            final int index = (getChildren().indexOf(sp));
            if (index != -1) {
                getChildren().remove(index);
                if (!raceIsFinished) {
                    newCircle();
                }
            }
        });
        pt.play();
    }

}

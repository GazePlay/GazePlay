package net.gazeplay.games.shooter;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Shooter extends Parent implements GameLifeCycle {

    private static final int MAX_RADIUS = 70;
    private static final int MIN_RADIUS = 30;

    private static final int MAX_TIME_LENGTH = 7;
    private static final int MIN_TIME_LENGTH = 4;

    private final IGameContext gameContext;

    private final Image blue;
    private final Image green;
    private final Image yellow;
    private final Image orange;
    private final Image red;
    private final Image flash;

    private final String date;
    private Label text;
    private int score;

    private StackPane hand;
    private final ImageView cage;

    private Boolean left;

    private final String gameType;

    private final Stats stats;

    private final Point[] endPoints;

    private final EventHandler<Event> enterEvent;
    private final EventHandler<GazeEvent> handEventGaze;

    // done
    public Shooter(final IGameContext gameContext, final Stats stats, final String type) {
        this.gameContext = gameContext;
        this.stats = stats;
        final LocalDate localDate = LocalDate.now();
        date = DateTimeFormatter.ofPattern("d MMMM uuuu ").format(localDate);
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

        final EventHandler<Event> handEvent = e -> {
            if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                final double x = ((MouseEvent) e).getX();
                final double y = ((MouseEvent) e).getY();
                hand.setRotate(getAngle(new Point(x, y)));
            }
        };

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

        gameContext.getRoot().widthProperty().addListener((observable, oldValue, newValue) -> updatePoints(imageRectangle));
        gameContext.getRoot().heightProperty().addListener((observable, oldValue, newValue) -> updatePoints(imageRectangle));

        enterEvent = e -> {
            if (e.getTarget() instanceof Target) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    final Target target = (Target) e.getTarget();
                    if (!target.isDone()) {
                        target.setDone(true);
                        enter(target);
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

        endPoints[3].x = rectangle.getWidth();

        endPoints[4].x = rectangle.getWidth();
        endPoints[4].y = rectangle.getHeight() / 2;

        endPoints[5].y = rectangle.getHeight() / 2;

        endPoints[6].x = rectangle.getWidth() / 2;

        endPoints[7].x = rectangle.getWidth();
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

    public void magicCage() {
        final Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(new Duration(8000)));

        timeline.setOnFinished(actionEvent -> {
            if (moveCage(left)) {
                left = !left;
            }
            magicCage();
        });
        timeline.play();

    }

    public boolean moveCage(final Boolean leftside) {

        final double min = Math.ceil(0);
        final double max = Math.floor(2);
        final int rd = (int) (Math.floor(Math.random() * (max - min + 1)) + min);

        boolean leftorright = false;

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double x = cage.getBoundsInParent().getMinY();
        final double y = cage.getBoundsInParent().getMinX();

        final Timeline timeline = new Timeline();
        final Timeline timeline2 = new Timeline();
        final Timeline timeline3 = new Timeline();

        if ((rd == 2) || (rd == 0)) {
            // Move UP
            timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),
                new KeyValue(cage.layoutYProperty(), x - (dimension2D.getHeight() / 5), Interpolator.EASE_OUT)));
        }

        if ((rd == 1) || (rd == 2)) {
            // Move Left or Right
            final double val;

            final double cst;
            if (gameType.equals("biboule")) {
                cst = 2.5;
            } else {// equals robot
                cst = 3;
            }

            if (leftside) {
                val = y + (dimension2D.getWidth() / cst);
            } else {
                val = y - (dimension2D.getWidth() / cst);
            }

            timeline3.getKeyFrames().add(
                new KeyFrame(new Duration(3000), new KeyValue(cage.layoutXProperty(), val, Interpolator.EASE_OUT)));
            leftorright = true;
        }

        if ((rd == 2) || (rd == 0)) {
            // Move DOWN
            timeline2.getKeyFrames().add(
                new KeyFrame(new Duration(500), new KeyValue(cage.layoutYProperty(), x, Interpolator.EASE_OUT)));
        }
        final SequentialTransition st = new SequentialTransition();
        st.getChildren().addAll(timeline, timeline3, timeline2);
        st.play();

        gameContext.getRoot().widthProperty().addListener((observable, oldValue, newValue) -> {
            st.stop();
            updateCage();
            left = true;
        });

        return leftorright;
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
        if (gameType.equals("biboule")) {
            sc.setFont(Font.font("AR BLANCA", dimension2D.getHeight() / 18));
            sc.setLayoutX(8.9 * dimension2D.getWidth() / 29.7);
            sc.setLayoutY(1.8 * dimension2D.getHeight() / 21);
        } else {
            sc.setFont(Font.font(dimension2D.getHeight() / 20));
            sc.setLayoutX(16 * dimension2D.getWidth() / 29.7);
            sc.setLayoutY(15.2 * dimension2D.getHeight() / 21);

            tc.setFont(Font.font(dimension2D.getHeight() / 20));
            tc.setLayoutX(15 * dimension2D.getWidth() / 29.7);
            tc.setLayoutY(14 * dimension2D.getHeight() / 21);
        }
    }

    @Override
    public void launch() {

        final Label sc = new Label();
        final Label tc = new Label();

        final String cst;
        if (gameType.equals("biboule")) {
            cst = date + "\n\t" + "Score:" + score;
        } else {// equals robot
            tc.setText("Score:");
            tc.setTextFill(Color.WHITE);
            cst = "" + score;

        }
        sc.setText(cst);
        sc.setTextFill(Color.WHITE);
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
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

        final double cst2;
        if (gameType.equals("biboule")) {
            cst2 = 2;
        } else {// equals robot
            cst2 = 1.7;
        }
        iv.setLayoutY(dimension2D.getHeight() / cst2);

        this.getChildren().add(iv);
        hand = (StackPane) this.getChildren().get(this.getChildren().indexOf(iv));
        hand.toFront();

        if (gameType.equals("biboule")) {
            sc.setFont(Font.font("AR BLANCA", dimension2D.getHeight() / 18));
            sc.setLayoutX(8.9 * dimension2D.getWidth() / 29.7);
            sc.setLayoutY(1.8 * dimension2D.getHeight() / 21);
        } else {
            sc.setFont(Font.font(dimension2D.getHeight() / 20));
            sc.setLineSpacing(10);
            sc.setLayoutX(16 * dimension2D.getWidth() / 29.7);
            sc.setLayoutY(15.2 * dimension2D.getHeight() / 21);

            tc.setFont(Font.font(dimension2D.getHeight() / 20));
            tc.setLineSpacing(10);
            tc.setLayoutX(15 * dimension2D.getWidth() / 29.7);
            tc.setLayoutY(14 * dimension2D.getHeight() / 21);

            this.getChildren().add(tc);
        }
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
        left = true;
        this.getChildren().add(cage);

        final Timeline waitbeforestart = new Timeline();
        waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        waitbeforestart.setOnFinished(actionEvent -> {

            for (int i = 0; i < 5; i++) {
                newCircle();
            }

            magicCage();
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
        t.getTransition().stop();

        final String cst;
        if (gameType.equals("biboule")) {
            cst = date + "\n\t" + "Score:" + score++;
        } else {// equals robot
            cst = "" + score++;
        }

        text.setText(cst);

        final double min = Math.ceil(1);
        final double max = Math.floor(3);
        final int r = (int) (Math.floor(Math.random() * (max - min + 1)) + min);

        final String soundResource = "data/" + gameType + "/sounds/hand_sound" + r + ".mp3";
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

        if (t.isAnimDone()) {
            t.setAnimDone(false);
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
            seqt.setOnFinished(actionEvent -> t.setAnimDone(true));
            seqt.play();
        }

        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft, ft2);
        pt.setOnFinished(actionEvent -> {
            final int i = getChildren().indexOf(t);
            if (i != -1) {
                getChildren().remove(i);
            }
            newCircle();
        });
        pt.play();
    }

    private void newCircle() {

        final Target sp = buildCircle();
        sp.toBack();
        this.getChildren().add(sp);
        gameContext.getGazeDeviceManager().addEventFilter(sp);

        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);

        final double x = (cage.getBoundsInParent().getMinX());
        sp.setLayoutX(x);
        final double y = (cage.getBoundsInParent().getMinY() + cage.getBoundsInParent().getMaxY()) / 2;
        sp.setLayoutY(y);
        sp.setCenterX(x);
        sp.setCenterY(y);
        stats.incNbShots();
        moveCircle(sp);
    }

    private void resize(final ImageView i) {
        final double d = MIN_RADIUS;
        i.setFitHeight(d);
        i.setFitWidth(d * 5 / 4);
    }

    private Target buildCircle() {

        final Target sp = new Target();

        final ImageView b1 = new ImageView(blue);
        final ImageView b2 = new ImageView(green);
        final ImageView b3 = new ImageView(yellow);
        final ImageView b4 = new ImageView(orange);
        final ImageView b5 = new ImageView(red);
        final ImageView f = new ImageView(flash);

        resize(b1);
        resize(b2);
        resize(b3);
        resize(b4);
        resize(b5);

        resize(f);

        sp.getChildren().addAll(f, b1, b2, b3, b4, b5);
        sp.getChildren().get(0).setOpacity(0);
        sp.getChildren().get(5).setOpacity(0);
        sp.getChildren().get(2).setOpacity(0);
        sp.getChildren().get(3).setOpacity(0);
        sp.getChildren().get(4).setOpacity(0);

        return sp;
    }

    private void moveCircle(final Target sp) {
        final double timebasic = ((MAX_TIME_LENGTH - MIN_TIME_LENGTH) * Math.random() + MIN_TIME_LENGTH) * 1000;

        final double min = Math.ceil(0);
        final double max = Math.floor(endPoints.length - 1);
        final int r = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
        final Point randomPoint = endPoints[r];

        final TranslateTransition tt1 = new TranslateTransition(new Duration(timebasic), sp);
        tt1.setToY(-sp.getCenterY() + randomPoint.y);
        tt1.setToX(-sp.getCenterX() + randomPoint.x);
        sp.setDestination(randomPoint);

        if (r == 2) {
            this.getChildren().get(this.getChildren().indexOf(sp)).toFront();
        } else {
            this.getChildren().get(this.getChildren().indexOf(sp)).toBack();
            text.toBack();
            cage.toBack();
        }

        final FadeTransition btog = new FadeTransition(new Duration(timebasic / 4), sp.getChildren().get(2));
        final FadeTransition gtoy = new FadeTransition(new Duration(timebasic / 4), sp.getChildren().get(3));
        final FadeTransition ytoo = new FadeTransition(new Duration(timebasic / 4), sp.getChildren().get(4));
        final FadeTransition otor = new FadeTransition(new Duration(timebasic / 4), sp.getChildren().get(5));

        btog.setFromValue(0);
        gtoy.setFromValue(0);
        ytoo.setFromValue(0);
        otor.setFromValue(0);

        btog.setToValue(1);
        gtoy.setToValue(1);
        ytoo.setToValue(1);
        otor.setToValue(1);

        final SequentialTransition seqt = new SequentialTransition(btog, gtoy, ytoo, otor);

        final ScaleTransition st = new ScaleTransition(new Duration(timebasic), sp);
        st.setByX(10);
        st.setByY(10);

        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(seqt, tt1, st);
        pt.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        sp.setTransition(pt);

        pt.setOnFinished(actionEvent -> {
            final int index = (getChildren().indexOf(sp));
            if (index != -1) {
                getChildren().remove(index);
                newCircle();
            }
        });

        pt.play();
    }

    public void restart(final Target sp) {
        final Transition pt = restartTransition(sp);
        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        pt.setOnFinished(actionEvent -> {
            sp.getChildren().get(2).setOpacity(0);
            sp.getChildren().get(3).setOpacity(0);
            sp.getChildren().get(4).setOpacity(0);
            sp.getChildren().get(5).setOpacity(0);

            sp.getChildren().get(0).setOpacity(0);
            moveCircle(sp);
        });
        pt.play();

    }
}

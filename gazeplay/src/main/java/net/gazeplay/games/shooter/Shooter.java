package net.gazeplay.games.shooter;

import javafx.animation.*;
import javafx.event.ActionEvent;
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
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Shooter extends Parent implements GameLifeCycle {

    private static final int MAX_RADIUS = 70;
    private static final int MIN_RADIUS = 30;

    private static final int MAX_TIME_LENGTH = 7;
    private static final int MIN_TIME_LENGTH = 4;

    private final double centerX;
    private final double centerY;

    private final GameContext gameContext;

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
    public Shooter(GameContext gameContext, Stats stats, String type) {
        this.gameContext = gameContext;
        this.stats = stats;
        LocalDate localDate = LocalDate.now();
        date = DateTimeFormatter.ofPattern("d MMMM uuuu ").format(localDate);
        score = 0;
        gameType = type;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = 8.7 * dimension2D.getWidth() / 29.7;
        centerY = 10 * dimension2D.getHeight() / 21;
        hand = new StackPane();

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(new ImagePattern(new Image("data/" + gameType + "/images/Background.jpg")));

        int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;
        imageRectangle.setOpacity(1 - coef * 0.9);

        gameContext.getChildren().add(imageRectangle);
        gameContext.getChildren().add(this);

        EventHandler<Event> handEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                    double x = ((MouseEvent) e).getX();
                    double y = ((MouseEvent) e).getY();
                    hand.setRotate(getAngle(new Point(x, y)));
                }
            }
        };

        handEventGaze = new EventHandler<GazeEvent>() {
            @Override
            public void handle(GazeEvent e) {
                double x = e.getX();
                double y = e.getY();
                hand.setRotate(getAngle(new Point(x, y)));
            }
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

        Point[] points = new Point[8];
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

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (e.getTarget() instanceof Target) {
                    if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                        if (!((Target) e.getTarget()).done) {
                            ((Target) e.getTarget()).done = true;
                            enter((Target) e.getTarget());
                            stats.incNbGoals();
                            stats.notifyNewRoundReady();
                        }
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

    public float getAngle(Point target) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        float angle = (float) Math
                .toDegrees(Math.atan2(target.x - (dimension2D.getWidth() / 2), -target.y + (dimension2D.getHeight())));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public void magicCage() {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(new Duration(8000)));

        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (moveCage(left)) {
                    left = !left;
                }
                magicCage();
            }
        });
        timeline.play();

    }

    public boolean moveCage(Boolean leftside) {

        double min = Math.ceil(0);
        double max = Math.floor(2);
        int rd = (int) (Math.floor(Math.random() * (max - min + 1)) + min);

        boolean leftorright = false;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double x = cage.getBoundsInParent().getMinY();
        double y = cage.getBoundsInParent().getMinX();

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();
        Timeline timeline3 = new Timeline();

        if ((rd == 2) || (rd == 0)) {
            // Move UP
            timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),
                    new KeyValue(cage.layoutYProperty(), x - (dimension2D.getHeight() / 5), Interpolator.EASE_OUT)));
        }

        if ((rd == 1) || (rd == 2)) {
            // Move Left or Right
            double val;

            double cst;
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
        SequentialTransition st = new SequentialTransition();
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
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double x = dimension2D.getHeight();
        for (Node child : hand.getChildren()) {
            ((ImageView) child).setPreserveRatio(true);
            ((ImageView) child).setFitHeight(x);
        }
        hand.setLayoutY(0);
        hand.setLayoutX(3 * (dimension2D.getWidth() / 7));
        double cst2;
        if (gameType.equals("biboule")) {
            cst2 = 2;
        } else {// equals robot
            cst2 = 1.7;
        }
        hand.setLayoutY(dimension2D.getHeight() / cst2);
    }

    public void updateCage() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        cage.setLayoutX(8.5 * dimension2D.getWidth() / 29.7);

        double y;
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

    public void updateScore(Label sc, Label tc) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
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

        Label sc = new Label();
        Label tc = new Label();

        String cst;
        if (gameType.equals("biboule")) {
            cst = date + "\n\t" + "Score:" + score;
        } else {// equals robot
            tc.setText("Score:");
            tc.setTextFill(Color.WHITE);
            cst = "" + score;

        }
        sc.setText(cst);
        sc.setTextFill(Color.WHITE);
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        ImageView iv1 = new ImageView(new Image("data/" + gameType + "/images/hand.png"));
        ImageView iv2 = new ImageView(new Image("data/" + gameType + "/images/handShot.png"));

        StackPane iv = new StackPane();
        double x = dimension2D.getHeight();
        iv1.setPreserveRatio(true);
        iv1.setFitHeight(x);
        iv2.setPreserveRatio(true);
        iv2.setFitHeight(x);

        iv.getChildren().addAll(iv1, iv2);
        iv.getChildren().get(1).setOpacity(0);
        iv.setLayoutY(0);
        iv.setLayoutX(3 * (dimension2D.getWidth() / 7));

        double cst2;
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

        Timeline waitbeforestart = new Timeline();
        waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        waitbeforestart.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                for (int i = 0; i < 5; i++) {
                    newCircle();
                }

                magicCage();
            }

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

    private Transition restartTransition(Target t) {

        FadeTransition ft = new FadeTransition(Duration.millis(1), t);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt1 = new TranslateTransition(Duration.millis(1), t);
        tt1.setToY(0);
        tt1.setToX(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(1), t);
        st.setToX(1);
        st.setToY(1);

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft, tt1, st);

        return pt;
    }

    private void enter(Target t) {
        t.removeEventFilter(MouseEvent.ANY, enterEvent);
        t.removeEventFilter(GazeEvent.ANY, enterEvent);
        t.t.stop();

        String cst;
        if (gameType.equals("biboule")) {
            cst = date + "\n\t" + "Score:" + score++;
        } else {// equals robot
            cst = "" + score++;
        }

        text.setText(cst);

        double min = Math.ceil(1);
        double max = Math.floor(3);
        int r = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
        Utils.playSound("data/" + gameType + "/sounds/hand_sound" + r + ".mp3");
        t.getChildren().get(0).setOpacity(1);

        hand.getChildren().get(1).setOpacity(1);
        FadeTransition ft = new FadeTransition(Duration.millis(500), t);
        ft.setFromValue(1);
        ft.setToValue(0);
        FadeTransition ft2 = new FadeTransition(Duration.millis(500), hand.getChildren().get(1));
        ft2.setFromValue(1);
        ft2.setToValue(0);

        if (t.animDone) {
            t.animDone = false;
            ScaleTransition st = new ScaleTransition(Duration.millis(100), hand);
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(0.7);
            st.setToY(0.7);
            ScaleTransition st2 = new ScaleTransition(Duration.millis(100), hand);
            st.setFromX(0.7);
            st.setFromY(0.7);
            st2.setToX(1);
            st2.setToY(1);
            SequentialTransition seqt = new SequentialTransition();
            seqt.getChildren().addAll(st, st2);
            seqt.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    t.animDone = true;
                }
            });
            seqt.play();
        }

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft, ft2);
        pt.play();

        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int i = getChildren().indexOf(t);
                if (i != -1)
                    getChildren().remove(i);
                newCircle();
            }
        });

    }

    private void newCircle() {

        Target sp = buildCircle();
        sp.toBack();
        this.getChildren().add(sp);
        gameContext.getGazeDeviceManager().addEventFilter(sp);

        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);

        double x = (cage.getBoundsInParent().getMinX());
        sp.setLayoutX(x);
        double y = (cage.getBoundsInParent().getMinY() + cage.getBoundsInParent().getMaxY()) / 2;
        sp.setLayoutY(y);
        sp.centerX = x;
        sp.centerY = y;

        moveCircle(sp);
    }

    private void resize(ImageView i) {
        double d = MIN_RADIUS;
        i.setFitHeight(d);
        i.setFitWidth(d * 5 / 4);
    }

    private Target buildCircle() {

        Target sp = new Target();

        ImageView b1 = new ImageView(blue);
        ImageView b2 = new ImageView(green);
        ImageView b3 = new ImageView(yellow);
        ImageView b4 = new ImageView(orange);
        ImageView b5 = new ImageView(red);
        ImageView f = new ImageView(flash);

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

    private void moveCircle(Target sp) {

        double timebasic = ((MAX_TIME_LENGTH - MIN_TIME_LENGTH) * Math.random() + MIN_TIME_LENGTH) * 1000;
        double timelength = Configuration.getInstance().getSpeedEffects() * timebasic;

        TranslateTransition tt1 = new TranslateTransition(new Duration(timelength), sp);
        double min = Math.ceil(0);
        double max = Math.floor(endPoints.length - 1);
        int r = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
        Point randomPoint = endPoints[r];
        tt1.setToY(-sp.centerY + randomPoint.y);
        tt1.setToX(-sp.centerX + randomPoint.x);
        sp.destination = randomPoint;

        if (r == 2) {
            this.getChildren().get(this.getChildren().indexOf(sp)).toFront();
        } else {
            this.getChildren().get(this.getChildren().indexOf(sp)).toBack();
            text.toBack();
            cage.toBack();
        }

        ScaleTransition st = new ScaleTransition(new Duration(timelength), sp);
        st.setByX(10);
        st.setByY(10);
        ParallelTransition pt = new ParallelTransition();

        FadeTransition btog = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(2));
        FadeTransition gtoy = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(3));
        FadeTransition ytoo = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(4));
        FadeTransition otor = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(5));

        btog.setFromValue(0);
        gtoy.setFromValue(0);
        ytoo.setFromValue(0);
        otor.setFromValue(0);

        btog.setToValue(1);
        gtoy.setToValue(1);
        ytoo.setToValue(1);
        otor.setToValue(1);

        SequentialTransition seqt = new SequentialTransition(btog, gtoy, ytoo, otor);

        pt.getChildren().addAll(seqt, tt1, st);

        sp.t = pt;

        pt.play();

        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = (getChildren().indexOf(sp));
                if (index != -1) {
                    getChildren().remove(index);
                    newCircle();
                }
            }
        });

    }

    public void restart(Target sp) {
        Transition pt = restartTransition(sp);
        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sp.getChildren().get(2).setOpacity(0);
                sp.getChildren().get(3).setOpacity(0);
                sp.getChildren().get(4).setOpacity(0);
                sp.getChildren().get(5).setOpacity(0);

                sp.getChildren().get(0).setOpacity(0);
                moveCircle(sp);
            }
        });
        pt.play();

    }
}

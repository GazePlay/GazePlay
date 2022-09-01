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
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class ShooterGame extends Parent implements GameLifeCycle {

    private static final double MIN_RADIUS = 30.d;

    private static final int MAX_TIME_LENGTH = 7;
    private static final int MIN_TIME_LENGTH = 4;

    private final IGameContext gameContext;

    private final Image[] targetFrames;

    private final String date;
    private Label text;
    private int score;

    private StackPane hand;
    private final ImageView box;

    private Boolean isOnLeftSide;

    private final String gameType;

    private final Stats stats;

    private EventHandler<Event> enterEvent;
    private EventHandler<GazeEvent> handEventGaze;

    private final SequentialTransition st = new SequentialTransition();

    private Timer minuteur;

    private final ReplayablePseudoRandom randomGenerator;

    ShooterGame(final IGameContext gameContext, final Stats stats, final String type) {
        this.gameContext = gameContext;
        this.stats = stats;
        final LocalDate localDate = LocalDate.now();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        date = DateTimeFormatter.ofPattern("d MMMM uuuu ").format(localDate);
        score = 0;
        gameType = type;
        hand = new StackPane();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        targetFrames = new Image[6];
        targetFrames[0] = new Image("data/shooter/" + gameType + "/images/Blue.png");
        targetFrames[1] = new Image("data/shooter/" + gameType + "/images/Green.png");
        targetFrames[2] = new Image("data/shooter/" + gameType + "/images/Yellow.png");
        targetFrames[3] = new Image("data/shooter/" + gameType + "/images/Orange.png");
        targetFrames[4] = new Image("data/shooter/" + gameType + "/images/Red.png");
        targetFrames[5] = new Image("data/shooter/" + gameType + "/images/Flash.png");

        box = new ImageView(new Image("data/shooter/" + gameType + "/images/Cage.png"));

    }

    ShooterGame(final IGameContext gameContext, final Stats stats, final String type, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        final LocalDate localDate = LocalDate.now();
        date = DateTimeFormatter.ofPattern("d MMMM uuuu ").format(localDate);
        score = 0;
        gameType = type;
        hand = new StackPane();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        targetFrames = new Image[6];
        targetFrames[0] = new Image("data/shooter/" + gameType + "/images/Blue.png");
        targetFrames[1] = new Image("data/shooter/" + gameType + "/images/Green.png");
        targetFrames[2] = new Image("data/shooter/" + gameType + "/images/Yellow.png");
        targetFrames[3] = new Image("data/shooter/" + gameType + "/images/Orange.png");
        targetFrames[4] = new Image("data/shooter/" + gameType + "/images/Red.png");
        targetFrames[5] = new Image("data/shooter/" + gameType + "/images/Flash.png");

        box = new ImageView(new Image("data/shooter/" + gameType + "/images/Cage.png"));

    }

    private Rectangle createBackground() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(new ImagePattern(new Image("data/shooter/" + gameType + "/images/Background.jpg")));

        double backgroundDefaultOpacity = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<>() {
            @Override
            public Double visitLight() {
                return 0.5;
            }

            @Override
            public Double visitDark() {
                return 1.d;
            }
        });
        double finalOpacity = (gameContext.getConfiguration().isBackgroundEnabled()) ? backgroundDefaultOpacity : 0.d;
        imageRectangle.setOpacity(finalOpacity);

        gameContext.getChildren().add(imageRectangle);

        return imageRectangle;
    }

    private float getAngle(final Point target) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        float angle = (float) Math
            .toDegrees(Math.atan2(target.x - (dimension2D.getWidth() / 2), -target.y + (dimension2D.getHeight())));
        return (angle < 0) ?  angle : angle + 360;
    }

    private void initBoxTimeline() {
        minuteur = new Timer();
        TimerTask tache = new TimerTask() {
            public void run() {
                if (moveBox(isOnLeftSide)) {
                    isOnLeftSide = !isOnLeftSide;
                }
            }
        };

        minuteur.schedule(tache, 0, 8000);
    }


    private boolean moveBox(final boolean isOnLeftSide) {

        clearTransition();

        final int randomValue = randomGenerator.nextInt(3);

        boolean goesFromLeftToRight = false;

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Timeline firstMove = new Timeline();
        final Timeline thirdMove = new Timeline();
        final Timeline secondMove = new Timeline();

        if ((randomValue == 2) || (randomValue == 0)) {
            // Move UP
            double multiplier = (gameType.equals("biboule")?-1:1);
            firstMove.getKeyFrames().add(new KeyFrame(new Duration(2000),
                new KeyValue(box.translateYProperty(),  multiplier*(dimension2D.getHeight() / 5) , Interpolator.LINEAR)));
        }

        if ((randomValue == 1) || (randomValue == 2)) {
            // Move Left or Right

            double val = (dimension2D.getWidth() / (gameType.equals("biboule") ? 2.5 : 3) );

            if (!isOnLeftSide) {
                val = 0;
            }

            secondMove.getKeyFrames().add(
                new KeyFrame(new Duration(3000), new KeyValue(box.translateXProperty(), val, Interpolator.LINEAR)));
            goesFromLeftToRight = true;
        }

        if ((randomValue == 2) || (randomValue == 0)) {
            // Move DOWN
            thirdMove.getKeyFrames().add(
                new KeyFrame(new Duration(500), new KeyValue(box.translateYProperty(), 0, Interpolator.EASE_OUT)));
        }

        st.getChildren().addAll(firstMove, secondMove, thirdMove);
        st.play();

        return goesFromLeftToRight;
    }

    public void clearTransition(){
        st.stop();
        st.getChildren().clear();
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

        closeTimer();

        this.gameContext.setLimiterAvailable();
        score = 0;
        this.getChildren().clear();

        Rectangle imageRectangle = createBackground();

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

        enterEvent = e -> {
            if (e.getTarget() instanceof Target) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    final Target target = (Target) e.getTarget();
                    if (!target.isDone()) {
                        target.setDone(true);
                        enter(target);
                    }
                }
            }
        };

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
        final ImageView iv1 = new ImageView(new Image("data/shooter/" + gameType + "/images/hand.png"));
        final ImageView iv2 = new ImageView(new Image("data/shooter/" + gameType + "/images/handShot.png"));

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

        box.layoutXProperty().bind(imageRectangle.widthProperty().multiply(8.5/29.7).subtract(box.fitWidthProperty().divide(2)));

        if (gameType.equals("biboule")) {
            box.layoutYProperty().bind(imageRectangle.heightProperty().multiply(8.5/ 21));
            box.fitHeightProperty().bind(imageRectangle.heightProperty().divide(6.5));
        } else {// equals robot
            box.fitHeightProperty().bind(imageRectangle.heightProperty().divide(8.5));
        }
        box.setPreserveRatio(true);

        gameContext.getRoot().widthProperty().addListener((observable, oldValue, newValue) -> {
            updateScore(sc, tc);
            updateHand();
        });
        gameContext.getRoot().heightProperty().addListener((observable, oldValue, newValue) -> {
            updateScore(sc, tc);
            updateHand();
        });

        box.toBack();
        isOnLeftSide = true;
        this.getChildren().add(box);

        final Timeline waitbeforestart = new Timeline();
        waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        waitbeforestart.setOnFinished(actionEvent -> {

            for (int i = 0; i < 5; i++) {
                newCircle();
            }

            initBoxTimeline();
        });
        waitbeforestart.play();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        this.gameContext.start();
        clearTransition();
        box.setTranslateX(0);
        box.setTranslateY(0);

        gameContext.setOffFixationLengthControl();
    }

    private void closeTimer(){
        if(minuteur != null) {
            minuteur.cancel();
            minuteur.purge();
        }
    }

    // done
    @Override
    public void dispose() {
        this.closeTimer();
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
        stats.incrementNumberOfGoalsReached();

        final String cst;
        if (gameType.equals("biboule")) {
            cst = date + "\n\t" + "Score:" + score++;
        } else {// equals robot
            cst = "" + score++;
        }
        gameContext.updateScore(stats,this);

        text.setText(cst);

        final int r = 1 + randomGenerator.nextInt(3);

        final String soundResource = "data/shooter/" + gameType + "/sounds/hand_sound" + r + ".mp3";
        gameContext.getSoundManager().add(soundResource);

        t.getChildren().get(5).setOpacity(1);

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

        final Target sp = buildTarget();
        sp.toBack();
        this.getChildren().add(sp);
        gameContext.getGazeDeviceManager().addEventFilter(sp);

        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);

        final double x = (box.getBoundsInParent().getMinX());
        sp.setLayoutX(x);
        final double y = (box.getBoundsInParent().getMinY() + box.getBoundsInParent().getMaxY()) / 2;
        sp.setLayoutY(y);
        sp.setCenterX(x);
        sp.setCenterY(y);
        stats.incrementNumberOfGoalsToReach();
        moveTarget(sp);
    }

    private void setInitialSize(final ImageView targetFramesView) {
        targetFramesView.setFitHeight(MIN_RADIUS);
        targetFramesView.setFitWidth(MIN_RADIUS * 5.d / 4.d);
    }

    private Target buildTarget() {

        final Target target = new Target();

        final ImageView[] targetFramesViews = new ImageView[6];

        for(int i = 0; i <6 ; i++){
            targetFramesViews[i] = new ImageView(targetFrames[i]);
            setInitialSize(targetFramesViews[i]);
            target.getChildren().add(targetFramesViews[i]);
            if(i != 0){
                targetFramesViews[i].setOpacity(0);
            }
        }

        return target;
    }

    private Point getRandomPoint(int random) {

        switch(random){
            case 1:
                return new Point(0, gameContext.getRoot().heightProperty().getValue()/2);
            case 2:
                return new Point(0, gameContext.getRoot().heightProperty().getValue());
            case 3:
                return new Point(gameContext.getRoot().widthProperty().getValue()/2, 0);
            case 4:
                return new Point(gameContext.getRoot().widthProperty().getValue()/2, gameContext.getRoot().heightProperty().getValue());
            case 5:
                return new Point(gameContext.getRoot().widthProperty().getValue(), 0);
            case 6:
                return new Point(gameContext.getRoot().widthProperty().getValue(), gameContext.getRoot().heightProperty().getValue()/2);
            case 7:
                return new Point(gameContext.getRoot().widthProperty().getValue(), gameContext.getRoot().heightProperty().getValue());
            default:
                return new Point(0,0);
        }
    }

    private void moveTarget(final Target sp) {
        final double timebasic = ((MAX_TIME_LENGTH - MIN_TIME_LENGTH) * randomGenerator.nextDouble() + MIN_TIME_LENGTH) * 1000;

        Point randomPoint = getRandomPoint(randomGenerator.nextInt(8));

        final TranslateTransition tt1 = new TranslateTransition(new Duration(timebasic), sp);
        tt1.setToY(-sp.getCenterY() + randomPoint.y);
        tt1.setToX(-sp.getCenterX() + randomPoint.x);
        sp.setDestination(randomPoint);

            this.getChildren().get(this.getChildren().indexOf(sp)).toBack();
            text.toBack();
            box.toBack();


        final SequentialTransition seqt = new SequentialTransition();

        for(int i = 1; i <5 ; i++){
            final FadeTransition toNextFrame = new FadeTransition(new Duration(timebasic / 4), sp.getChildren().get(i));
            toNextFrame.setFromValue(0);
            toNextFrame.setToValue(1);
            seqt.getChildren().add(toNextFrame);
        }



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
            sp.getChildren().get(1).setOpacity(0);
            sp.getChildren().get(2).setOpacity(0);
            sp.getChildren().get(3).setOpacity(0);
            sp.getChildren().get(4).setOpacity(0);
            sp.getChildren().get(5).setOpacity(0);
            moveTarget(sp);
        });
        pt.play();

    }

}

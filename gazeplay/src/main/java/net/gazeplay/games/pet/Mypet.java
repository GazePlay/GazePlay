package net.gazeplay.games.pet;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mypet extends Pane {

    public static final int DRYER_UP = 0;
    public static final int DRYER_RIGHT = 1;
    public static final int DRYER_LEFT = 2;

    @Getter
    @Setter
    private ImageView rightWing;

    @Getter
    @Setter
    private ImageView leftWing;

    @Getter
    @Setter
    private Rectangle body;

    @Getter
    @Setter
    private Rectangle mouth;

    @Getter
    @Setter
    private Rectangle rightEye;

    @Getter
    @Setter
    private Rectangle leftEye;

    private final double hratio;
    private final double wratio;

    private int eatingBool = 10;;

    private boolean[] eyeTouched = { false, false };
    private boolean bodyTouched = false;
    private boolean mouthTouched = false;

    @Getter
    private double bibouleh;

    @Getter
    private double biboulew;

    private ParallelTransition pt;
    private Timeline t;
    private PetHouse ph;

    @Getter
    private boolean eyesAreOpen = true;

    @Getter
    private String emotion = "basic";

    public Mypet(double height, double width, PetHouse ph) {
        Image tmp = new Image("data/pet/images/body.png");

        double old_valueh = tmp.getHeight();
        double new_valueh = height / 4;
        hratio = new_valueh / old_valueh;

        double old_valuew = tmp.getWidth();
        double new_valuew = width / 4;
        wratio = new_valuew / old_valuew;

        init();
        this.ph = ph;
        this.getChildren().addAll(leftWing, rightWing, body, mouth, rightEye, leftEye);

        setMovingWings(true);
        setBlinking();

        createHandlers();

    }

    public void init() {
        Image corps = new Image("data/pet/images/body.png");
        setBody(new Rectangle(0, 0, corps.getWidth() * wratio, corps.getHeight() * hratio));

        getBody().setFill(new ImagePattern(corps));

        bibouleh = 3 * getBody().getHeight() / 2;
        biboulew = 3 * getBody().getWidth() / 2;

        getBody().setLayoutX(biboulew / 2 - getBody().getWidth() / 2);
        getBody().setLayoutY(bibouleh - getBody().getHeight());

        Image wings = new Image("data/pet/images/wing.png");

        setLeftWing((new ImageView(wings)));
        getLeftWing().setPreserveRatio(true);
        getLeftWing().setFitHeight(wings.getHeight() * hratio);
        getLeftWing().setLayoutX(biboulew / 2 - 2 * getBody().getWidth() / 3);
        getLeftWing().setRotate(-30);

        setRightWing((new ImageView(wings)));
        getRightWing().setPreserveRatio(true);
        getRightWing().setFitHeight(wings.getHeight() * hratio);
        getRightWing().setLayoutX(biboulew / 2 + getBody().getWidth() / 3);
        getRightWing().setRotate(30);

        Image mouth = new Image("data/pet/images/mouth.png");
        setMouth(new Rectangle(0, 0, mouth.getWidth() * wratio, mouth.getHeight() * hratio));
        getMouth().setX(biboulew / 2 - getMouth().getWidth() / 2);
        getMouth().setY(3 * bibouleh / 4);
        getMouth().setFill(new ImagePattern(mouth));
        // getMouth().setFHeight(mouth.getHeight() * hratio);
        // getMouth().setFitWidth(mouth.getWidth() * wratio);

        Image eyes = new Image("data/pet/images/eye.png");

        setRightEye(new Rectangle(0, 0, eyes.getWidth() * wratio, eyes.getHeight() * hratio));
        getRightEye().setLayoutX(biboulew / 2 - biboulew / 8 - getRightEye().getWidth() / 2);
        getRightEye().setLayoutY(bibouleh / 2);
        getRightEye().setFill(new ImagePattern(eyes));

        setLeftEye(new Rectangle(0, 0, eyes.getWidth() * wratio, eyes.getHeight() * hratio));
        getLeftEye().setLayoutX(biboulew / 2 + biboulew / 8 - getLeftEye().getWidth() / 2);
        getLeftEye().setLayoutY(bibouleh / 2);
        getLeftEye().setFill(new ImagePattern(eyes));

    }

    public void setBasic() {
        emotion = "basic";
        getBody().setFill(new ImagePattern(new Image("data/pet/images/body.png")));
        getLeftWing().setImage(new Image("data/pet/images/wing.png"));
        getRightWing().setImage(new Image("data/pet/images/wing.png"));
        getMouth().setFill(new ImagePattern(new Image("data/pet/images/mouth.png")));
        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        eyesAreOpen = true;

    }

    public void setHappy() {

        emotion = "happy";
        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
        getMouth().setFill(new ImagePattern(new Image("data/pet/images/smile.png")));
        eyesAreOpen = false;

    }

    public void setSmiling() {

        emotion = "smile";
        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        getMouth().setFill(new ImagePattern(new Image("data/pet/images/smile.png")));
        eyesAreOpen = true;

    }

    public void setEating() {

        emotion = "eating";
        eatingBool = 10;

        ph.refill(1);
        Timeline eat = new Timeline();
        eat.getKeyFrames().add(new KeyFrame(Duration.millis(200),
                new KeyValue(getMouth().fillProperty(), new ImagePattern(new Image("data/pet/images/mouth.png")))));

        eat.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if ((eatingBool > 0) && (eatingBool % 2 == 0)) {
                    eat.getKeyFrames().clear();
                    eat.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(getMouth().fillProperty(),
                            new ImagePattern(new Image("data/pet/images/smile.png")))));
                    eat.play();
                } else if ((eatingBool > 0) && (eatingBool % 2 == 1)) {
                    eat.getKeyFrames().clear();
                    eat.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(getMouth().fillProperty(),
                            new ImagePattern(new Image("data/pet/images/mouth.png")))));
                    eat.play();
                } else {
                    setBasic();
                    setBlinkingEnabled(true);
                }
                eatingBool--;
            }
        });

        eat.play();

        eyesAreOpen = false;

    }

    public void setMovingWings(Boolean isMoving) {
        if (isMoving) {
            int duration = 150;

            TranslateTransition tt = new TranslateTransition(new Duration(duration), getLeftWing());
            TranslateTransition tt2 = new TranslateTransition(new Duration(duration), getRightWing());
            tt.setAutoReverse(true);
            tt2.setAutoReverse(true);
            tt.setToY(getBody().getLayoutY());
            tt2.setToY(getBody().getLayoutY());

            RotateTransition rt = new RotateTransition(new Duration(duration), getLeftWing());
            RotateTransition rt2 = new RotateTransition(new Duration(duration), getRightWing());
            rt.setAutoReverse(true);
            rt2.setAutoReverse(true);
            rt.setToAngle(-120);
            rt2.setToAngle(120);

            pt = new ParallelTransition();
            pt.getChildren().addAll(tt, tt2, rt, rt2);
            pt.setCycleCount(Animation.INDEFINITE);
            pt.setAutoReverse(true);
            pt.play();
        } else {
            pt.stop();
        }
    }

    public void setBlinking() {
        eyesAreOpen = true;
        t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200),
                new KeyValue(getLeftEye().fillProperty(), new ImagePattern(new Image("data/pet/images/eye.png")))));
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200),
                new KeyValue(getRightEye().fillProperty(), new ImagePattern(new Image("data/pet/images/eye.png")))));

        t.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                double time = Math.random() * 10000;
                t.getKeyFrames().clear();
                if (eyesAreOpen) {
                    t.getKeyFrames().add(new KeyFrame(Duration.millis(time), new KeyValue(getLeftEye().fillProperty(),
                            new ImagePattern(new Image("data/pet/images/eyeclosed.png")))));
                    t.getKeyFrames().add(new KeyFrame(Duration.millis(time), new KeyValue(getRightEye().fillProperty(),
                            new ImagePattern(new Image("data/pet/images/eyeclosed.png")))));
                } else {
                    t.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(getLeftEye().fillProperty(),
                            new ImagePattern(new Image("data/pet/images/eye.png")))));
                    t.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(getRightEye().fillProperty(),
                            new ImagePattern(new Image("data/pet/images/eye.png")))));
                }
                eyesAreOpen = !eyesAreOpen;
                t.play();

            }
        });

    }

    public void setBlinkingEnabled(boolean b) {
        if (b) {
            t.play();
        } else {
            t.stop();
        }

    }

    public void createHandlers() {
        createEyesHandlers();
        createBodyHandlers();
        createMouthHandlers();
    }

    public void createMouthHandlers() {

        ph.hand.xProperty().addListener((o) -> {
            if (ph.getMode() == PetHouse.EAT_MODE) {
                Shape intersect = Shape.intersect(ph.hand, getMouth());
                if ((intersect.getBoundsInLocal().getWidth() != -1) && !mouthTouched) {
                    setBlinkingEnabled(false);
                    setHappy();
                    ph.hand.setFill(new ImagePattern(new Image("data/pet/images/emptyspoon.png")));
                    mouthTouched = true;
                } else if ((intersect.getBoundsInLocal().getWidth() == -1) && mouthTouched && (ph.isSpoonFull())) {
                    setEating();
                    ph.setSpoonFull(false);
                    mouthTouched = false;
                }
            }
        });
    }

    public void createBodyHandlers() {
        getBody().setCursor(Cursor.OPEN_HAND);
        ph.hand.xProperty().addListener((o) -> {
            if (ph.getMode() == PetHouse.INIT_MODE) {

                Shape intersect = Shape.intersect(ph.hand, getBody());
                if ((intersect.getBoundsInLocal().getWidth() != -1) && !bodyTouched && !eyeTouched[0]
                        && !eyeTouched[1]) {
                    t.stop();
                    setHappy();
                    bodyTouched = true;
                } else if ((intersect.getBoundsInLocal().getWidth() == -1) && bodyTouched) {
                    setBasic();
                    t.play();
                    bodyTouched = false;
                }
            } else if ((ph.getMode() == PetHouse.SPORT_MODE)) {

                Shape intersect = Shape.intersect(ph.hand, getBody());
                if (!ph.getBaloonGone() && (intersect.getBoundsInLocal().getWidth() != -1)) {
                    log.debug("enter baloon");
                    ph.setBaloonGone(true);
                    ph.rd.stop();
                    ImageView baloon = new ImageView("data/pet/images/ball.png");
                    baloon.setPreserveRatio(true);
                    baloon.fitWidthProperty().bind(ph.hand.widthProperty());
                    ph.getGameContext().getChildren().add(baloon);
                    Dimension2D dimension2D = ph.getGameContext().getGamePanelDimensionProvider().getDimension2D();
                    baloon.setX(0);

                    TranslateTransition tt = new TranslateTransition(Duration.millis(1000), baloon);
                    int nombreAleatoire = (int) (Math.random() * 4);
                    switch (nombreAleatoire) {
                    case 0:
                        tt.setFromX(0);
                        tt.setFromY((int) (Math.random() * dimension2D.getHeight()));
                        break;
                    case 1:
                        tt.setFromY(0);
                        tt.setFromX((int) (Math.random() * dimension2D.getWidth()));
                        break;
                    case 2:
                        tt.setFromX(dimension2D.getWidth());
                        tt.setFromY((int) (Math.random() * dimension2D.getHeight()));
                        break;
                    case 3:
                        tt.setFromY(dimension2D.getHeight());
                        tt.setFromX((int) (Math.random() * dimension2D.getWidth()));
                        break;
                    }

                    // TODO random position of the baloon enter

                    setBlinkingEnabled(false);
                    setHappy();

                    tt.setToX(getLayoutX() + (getWidth() / 3));
                    tt.setToY(getLayoutY());

                    TranslateTransition t2 = new TranslateTransition(Duration.millis(500), baloon);
                    nombreAleatoire = (int) (Math.random() * 4);
                    switch (nombreAleatoire) {
                    case 0:
                        t2.setToX(0);
                        t2.setToY((int) (Math.random() * dimension2D.getHeight()));
                        break;
                    case 1:
                        t2.setToY(0);
                        t2.setToX((int) (Math.random() * dimension2D.getWidth()));
                        break;
                    case 2:
                        t2.setToX(dimension2D.getWidth());
                        t2.setToY((int) (Math.random() * dimension2D.getHeight()));
                        break;
                    case 3:
                        t2.setToY(dimension2D.getHeight());
                        t2.setToX((int) (Math.random() * dimension2D.getWidth()));
                        break;
                    }

                    // TODO random position of the baloon exit

                    SequentialTransition st = new SequentialTransition();

                    st.getChildren().addAll(tt, t2);

                    tt.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            setSmiling();
                        }
                    });

                    RotateTransition rt = new RotateTransition(Duration.millis(500), baloon);
                    rt.setByAngle(360);
                    rt.setCycleCount(Animation.INDEFINITE);

                    ParallelTransition pt = new ParallelTransition();
                    pt.getChildren().addAll(st, rt);
                    st.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            ph.rd.play();
                            pt.stop();
                            ph.getGameContext().getChildren().remove(baloon);
                            ph.setBaloonGone(false);
                            setBasic();
                            setBlinkingEnabled(true);
                            ph.refill(2);
                        }
                    });
                    pt.play();
                }
            }

        });

    }

    public void createEyesHandlers() {
        getLeftEye().setCursor(Cursor.HAND);
        getRightEye().setCursor(Cursor.HAND);

        ph.hand.xProperty().addListener((o) -> {
            if (ph.getMode() == PetHouse.INIT_MODE) {
                Shape intersect = Shape.intersect(ph.hand, getLeftEye());
                Shape intersect2 = Shape.intersect(ph.hand, getRightEye());

                if ((intersect.getBoundsInLocal().getWidth() == -1) && (intersect2.getBoundsInLocal().getWidth() == -1)
                        && (eyeTouched[0] == true) && (eyeTouched[1] == true)) {
                    getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                    getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                    t.play();
                    eyeTouched[0] = false;
                    eyeTouched[1] = false;
                } else {
                    if ((intersect.getBoundsInLocal().getWidth() != -1)) {
                        t.stop();
                        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
                        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                        eyeTouched[0] = true;
                    }

                    if ((intersect2.getBoundsInLocal().getWidth() != -1)) {
                        t.stop();
                        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
                        eyeTouched[1] = true;
                    }
                }
            }
        });
    }

}

package net.gazeplay.games.pet;

import javafx.animation.*;
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

import java.util.Random;

@Slf4j
class Mypet extends Pane {

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

    private final double hRatio;
    private final double wRatio;

    private int eatingBool = 10;

    private final boolean[] eyeTouched = {false, false};
    private boolean bodyTouched = false;
    private boolean mouthTouched = false;

    @Getter
    private double bibouleh;

    @Getter
    private double biboulew;

    private ParallelTransition pt;
    private Timeline t;
    private final PetHouse ph;

    @Getter
    private boolean eyesAreOpen = true;

    Mypet(final double height, final double width, final PetHouse ph) {
        final Image tmp = new Image("data/pet/images/body.png");

        hRatio = height / 4d / tmp.getHeight();
        wRatio = width / 4d / tmp.getWidth();

        init();
        this.ph = ph;
        this.getChildren().addAll(leftWing, rightWing, body, mouth, rightEye, leftEye);

        setMovingWings(true);
        setBlinking();

        createHandlers();

    }

    private void init() {
        final Image corps = new Image("data/pet/images/body.png");
        setBody(new Rectangle(0, 0, corps.getWidth() * wRatio, corps.getHeight() * hRatio));

        getBody().setFill(new ImagePattern(corps));

        bibouleh = 3 * getBody().getHeight() / 2;
        biboulew = 3 * getBody().getWidth() / 2;

        getBody().setLayoutX(biboulew / 2 - getBody().getWidth() / 2);
        getBody().setLayoutY(bibouleh - getBody().getHeight());

        final Image wings = new Image("data/pet/images/wing.png");

        setLeftWing((new ImageView(wings)));
        getLeftWing().setPreserveRatio(true);
        getLeftWing().setFitHeight(wings.getHeight() * hRatio);
        getLeftWing().setLayoutX(biboulew / 2 - 2 * getBody().getWidth() / 3);
        getLeftWing().setRotate(-30);

        setRightWing((new ImageView(wings)));
        getRightWing().setPreserveRatio(true);
        getRightWing().setFitHeight(wings.getHeight() * hRatio);
        getRightWing().setLayoutX(biboulew / 2 + getBody().getWidth() / 3);
        getRightWing().setRotate(30);

        final Image mouth = new Image("data/pet/images/mouth.png");
        setMouth(new Rectangle(0, 0, mouth.getWidth() * wRatio, mouth.getHeight() * hRatio));
        getMouth().setX(biboulew / 2 - getMouth().getWidth() / 2);
        getMouth().setY(3 * bibouleh / 4);
        getMouth().setFill(new ImagePattern(mouth));
        // getMouth().setFHeight(mouth.getHeight() * hratio);
        // getMouth().setFitWidth(mouth.getWidth() * wratio);

        final Image eyes = new Image("data/pet/images/eye.png");

        setRightEye(new Rectangle(0, 0, eyes.getWidth() * wRatio, eyes.getHeight() * hRatio));
        getRightEye().setLayoutX(biboulew / 2 - biboulew / 8 - getRightEye().getWidth() / 2);
        getRightEye().setLayoutY(bibouleh / 2);
        getRightEye().setFill(new ImagePattern(eyes));

        setLeftEye(new Rectangle(0, 0, eyes.getWidth() * wRatio, eyes.getHeight() * hRatio));
        getLeftEye().setLayoutX(biboulew / 2 + biboulew / 8 - getLeftEye().getWidth() / 2);
        getLeftEye().setLayoutY(bibouleh / 2);
        getLeftEye().setFill(new ImagePattern(eyes));

    }

    void setBasic() {
        getBody().setFill(new ImagePattern(new Image("data/pet/images/body.png")));
        getLeftWing().setImage(new Image("data/pet/images/wing.png"));
        getRightWing().setImage(new Image("data/pet/images/wing.png"));
        getMouth().setFill(new ImagePattern(new Image("data/pet/images/mouth.png")));
        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        eyesAreOpen = true;

    }

    void setHappy() {
        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
        getMouth().setFill(new ImagePattern(new Image("data/pet/images/smile.png")));
        eyesAreOpen = false;
    }

    private void setSmiling() {
        getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
        getMouth().setFill(new ImagePattern(new Image("data/pet/images/smile.png")));
        eyesAreOpen = true;
    }

    private void setEating() {
        eatingBool = 10;

        ph.refill(1);
        final Timeline eat = new Timeline();
        eat.getKeyFrames().add(new KeyFrame(Duration.millis(200),
            new KeyValue(getMouth().fillProperty(), new ImagePattern(new Image("data/pet/images/mouth.png")))));

        eat.setOnFinished(e -> {
            if (eatingBool > 0) {
                final String imagePath;
                if (eatingBool % 2 == 0) {
                    imagePath = "data/pet/images/smile.png";
                } else {
                    imagePath = "data/pet/images/mouth.png";
                }
                eat.getKeyFrames().clear();
                eat.getKeyFrames().add(
                    new KeyFrame(
                        Duration.millis(200),
                        new KeyValue(
                            getMouth().fillProperty(),
                            new ImagePattern(new Image(imagePath))
                        )
                    )
                );
                eat.play();
            } else {
                setBasic();
                setBlinkingEnabled(true);
            }
            eatingBool--;
        });

        eat.play();

        eyesAreOpen = false;
    }

    private void setMovingWings(final Boolean isMoving) {
        if (isMoving) {
            final int duration = 150;

            final TranslateTransition tt = new TranslateTransition(new Duration(duration), getLeftWing());
            final TranslateTransition tt2 = new TranslateTransition(new Duration(duration), getRightWing());
            tt.setAutoReverse(true);
            tt2.setAutoReverse(true);
            tt.setToY(getBody().getLayoutY());
            tt2.setToY(getBody().getLayoutY());

            final RotateTransition rt = new RotateTransition(new Duration(duration), getLeftWing());
            final RotateTransition rt2 = new RotateTransition(new Duration(duration), getRightWing());
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

    private void setBlinking() {
        eyesAreOpen = true;
        t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200),
            new KeyValue(getLeftEye().fillProperty(), new ImagePattern(new Image("data/pet/images/eye.png")))));
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200),
            new KeyValue(getRightEye().fillProperty(), new ImagePattern(new Image("data/pet/images/eye.png")))));

        t.setOnFinished(e -> {
            final double time = Math.random() * 10000;
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

        });

    }

    void setBlinkingEnabled(final boolean b) {
        if (b) {
            t.play();
        } else {
            t.stop();
        }

    }

    private void createHandlers() {
        createEyesHandlers();
        createBodyHandlers();
        createMouthHandlers();
    }

    private void createMouthHandlers() {

        ph.getHand().xProperty().addListener((o) -> {
            if (ph.getMode() == PetHouse.EAT_MODE) {
                final Shape intersect = Shape.intersect(ph.getHand(), getMouth());
                if ((intersect.getBoundsInLocal().getWidth() != -1) && !mouthTouched) {
                    setBlinkingEnabled(false);
                    setHappy();
                    ph.getHand().setFill(new ImagePattern(new Image("data/pet/images/emptyspoon.png")));
                    mouthTouched = true;
                } else if ((intersect.getBoundsInLocal().getWidth() == -1) && mouthTouched && (ph.isSpoonFull())) {
                    setEating();
                    ph.setSpoonFull(false);
                    mouthTouched = false;
                }
            }
        });
    }

    private void createBodyHandlers() {
        getBody().setCursor(Cursor.OPEN_HAND);
        ph.getHand().xProperty().addListener((o) -> {
            if (ph.getMode() == PetHouse.INIT_MODE) {

                final Shape intersect = Shape.intersect(ph.getHand(), getBody());
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

                final Shape intersect = Shape.intersect(ph.getHand(), getBody());
                if (!ph.getBaloonGone() && (intersect.getBoundsInLocal().getWidth() != -1)) {
                    log.debug("enter baloon");
                    ph.setBaloonGone(true);
                    ph.rd.stop();
                    final ImageView baloon = new ImageView("data/pet/images/ball.png");
                    baloon.setPreserveRatio(true);
                    baloon.fitWidthProperty().bind(ph.getHand().widthProperty());
                    ph.getGameContext().getChildren().add(baloon);
                    final Dimension2D dimension2D = ph.getGameContext().getGamePanelDimensionProvider().getDimension2D();
                    baloon.setX(0);

                    final TranslateTransition tt = new TranslateTransition(Duration.millis(1000), baloon);
                    final Random random = new Random();
                    int nombreAleatoire = random.nextInt(4);
                    switch (nombreAleatoire) {
                        case 0:
                            tt.setFromX(0);
                            tt.setFromY(random.nextDouble() * dimension2D.getHeight());
                            break;
                        case 1:
                            tt.setFromY(0);
                            tt.setFromX(random.nextDouble() * dimension2D.getWidth());
                            break;
                        case 2:
                            tt.setFromX(dimension2D.getWidth());
                            tt.setFromY(random.nextDouble() * dimension2D.getHeight());
                            break;
                        case 3:
                            tt.setFromY(dimension2D.getHeight());
                            tt.setFromX(random.nextDouble() * dimension2D.getWidth());
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported value : " + nombreAleatoire);
                    }

                    // TODO random position of the baloon enter

                    setBlinkingEnabled(false);
                    setHappy();

                    tt.setToX(getLayoutX() + (getWidth() / 3));
                    tt.setToY(getLayoutY());

                    final TranslateTransition t2 = new TranslateTransition(Duration.millis(500), baloon);
                    nombreAleatoire = random.nextInt(4);
                    switch (nombreAleatoire) {
                        case 0:
                            t2.setToX(0);
                            t2.setToY(random.nextDouble() * dimension2D.getHeight());
                            break;
                        case 1:
                            t2.setToY(0);
                            t2.setToX(random.nextDouble() * dimension2D.getWidth());
                            break;
                        case 2:
                            t2.setToX(dimension2D.getWidth());
                            t2.setToY(random.nextDouble() * dimension2D.getHeight());
                            break;
                        case 3:
                            t2.setToY(dimension2D.getHeight());
                            t2.setToX(random.nextDouble() * dimension2D.getWidth());
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported value : " + nombreAleatoire);
                    }

                    // TODO random position of the baloon exit

                    final SequentialTransition st = new SequentialTransition();

                    st.getChildren().addAll(tt, t2);

                    tt.setOnFinished(e -> setSmiling());

                    final RotateTransition rt = new RotateTransition(Duration.millis(500), baloon);
                    rt.setByAngle(360);
                    rt.setCycleCount(Animation.INDEFINITE);

                    final ParallelTransition pt = new ParallelTransition();
                    pt.getChildren().addAll(st, rt);
                    st.setOnFinished(e -> {
                        ph.rd.play();
                        pt.stop();
                        ph.getGameContext().getChildren().remove(baloon);
                        ph.setBaloonGone(false);
                        setBasic();
                        setBlinkingEnabled(true);
                        ph.refill(2);
                    });
                    pt.play();
                }
            }

        });

    }

    private void createEyesHandlers() {
        getLeftEye().setCursor(Cursor.HAND);
        getRightEye().setCursor(Cursor.HAND);

        ph.getHand().xProperty().addListener((o) -> {
            if (ph.getMode() == PetHouse.INIT_MODE) {
                final Shape intersect = Shape.intersect(ph.getHand(), getLeftEye());
                final Shape intersect2 = Shape.intersect(ph.getHand(), getRightEye());

                if ((intersect.getBoundsInLocal().getWidth() == -1) && (intersect2.getBoundsInLocal().getWidth() == -1)
                    && (eyeTouched[0]) && (eyeTouched[1])) {
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

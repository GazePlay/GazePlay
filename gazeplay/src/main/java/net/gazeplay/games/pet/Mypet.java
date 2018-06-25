package net.gazeplay.games.pet;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GazePlay;

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
    private ImageView body;

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
        setBody((new ImageView(corps)));
        getBody().setFitHeight(corps.getHeight() * hratio);
        getBody().setFitWidth(corps.getWidth() * wratio);

        bibouleh = 3 * getBody().getFitHeight() / 2;
        biboulew = 3 * getBody().getFitWidth() / 2;

        getBody().setLayoutX(biboulew / 2 - getBody().getFitWidth() / 2);
        getBody().setLayoutY(bibouleh - getBody().getFitHeight());

        Image wings = new Image("data/pet/images/wing.png");

        setLeftWing((new ImageView(wings)));
        getLeftWing().setPreserveRatio(true);
        getLeftWing().setFitHeight(wings.getHeight() * hratio);
        getLeftWing().setLayoutX(biboulew / 2 - 2 * getBody().getFitWidth() / 3);
        getLeftWing().setRotate(-30);

        setRightWing((new ImageView(wings)));
        getRightWing().setPreserveRatio(true);
        getRightWing().setFitHeight(wings.getHeight() * hratio);
        getRightWing().setLayoutX(biboulew / 2 + getBody().getFitWidth() / 3);
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
        getBody().setImage(new Image("data/pet/images/body.png"));
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

    public void setSad() {

    }

    public void setAngry() {

    }

    public void setDisturbed() {

    }

    public void setDirty() {

    }

    public void setHungry() {

    }

    public void setTired() {

    }

    public void setSleepy() {

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
        EventHandler<Event> enterhandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (ph.getMode() == PetHouse.EAT_MODE) {
                    setBlinkingEnabled(false);
                    setHappy();
                    ph.hand.setFill(new ImagePattern(new Image("data/pet/images/emptyspoon.png")));
                }
            }
        };

        EventHandler<Event> exithandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if ((ph.getMode() == PetHouse.EAT_MODE) && (ph.isSpoonFull())) {
                    setEating();
                    ph.setSpoonFull(false);
                }
            }
        };

        getMouth().addEventFilter(MouseEvent.MOUSE_ENTERED, enterhandler);
        getMouth().addEventFilter(MouseEvent.MOUSE_EXITED, exithandler);
    }

    public void createBodyHandlers() {
        getBody().setCursor(Cursor.OPEN_HAND);

        EventHandler<Event> enterhandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (ph.getMode() == PetHouse.INIT_MODE) {
                    t.stop();
                    setHappy();
                }
            }
        };

        EventHandler<Event> exithandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (ph.getMode() == PetHouse.INIT_MODE) {
                    setBasic();
                    t.play();
                }
            }
        };

        getBody().addEventFilter(MouseEvent.MOUSE_ENTERED, enterhandler);
        getBody().addEventFilter(MouseEvent.MOUSE_EXITED, exithandler);

    }

    public void createEyesHandlers() {
        getLeftEye().setCursor(Cursor.HAND);
        getRightEye().setCursor(Cursor.HAND);

        EventHandler<Event> lefteyehandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (ph.getMode() == PetHouse.INIT_MODE) {
                    t.stop();
                    getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
                    getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                }
            }
        };

        getLeftEye().addEventHandler(MouseEvent.MOUSE_ENTERED, lefteyehandler);

        EventHandler<Event> righteyehandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (ph.getMode() == PetHouse.INIT_MODE) {
                    t.stop();
                    getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                    getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eyeclosed.png")));
                }
            }
        };

        getRightEye().addEventHandler(MouseEvent.MOUSE_ENTERED, righteyehandler);

        EventHandler<Event> exiteyehandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (ph.getMode() == PetHouse.INIT_MODE) {
                    getLeftEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                    getRightEye().setFill(new ImagePattern(new Image("data/pet/images/eye.png")));
                    t.play();
                }
            }
        };

        getLeftEye().addEventHandler(MouseEvent.MOUSE_EXITED, exiteyehandler);
        getRightEye().addEventHandler(MouseEvent.MOUSE_EXITED, exiteyehandler);
    }

    public void setDryer(int i) {
        if (i == DRYER_LEFT) {

        } else if (i == DRYER_RIGHT) {

        } else { // if (i == DRYER_UP)

        }
    }
}

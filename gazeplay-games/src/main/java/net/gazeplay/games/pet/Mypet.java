package net.gazeplay.games.pet;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

/**
 * The pet of the Pet game (reviewed on 30/10/2026): a Biboule (the one of Biboule Jump) flapping its wings and blinking.
 * Its mood: as usual, sad (a need of the pet is low), happy (eyes closed and smiling, when it is stroked, washed or
 * plays) or eating. Its top left corner is at (0, 0); it does not follow the gaze itself (the game reads the place of
 * the gaze on the room).
 */
class Mypet extends Group {

    private static final String FOLDER = "data/pet/images/";

    private final Image eyeOpen = new Image(FOLDER + "eye.png");
    private final Image eyeClosed = new Image(FOLDER + "eyeclosed.png");
    private final Image mouthImage = new Image(FOLDER + "mouth.png");
    private final Image smileImage = new Image(FOLDER + "smile.png");
    private final Image sadImage = new Image(FOLDER + "sad.png");

    @Getter
    private final ImageView body;

    @Getter
    private final ImageView mouth;

    private final ImageView leftEye;
    private final ImageView rightEye;
    private final ImageView leftWing;
    private final ImageView rightWing;

    @Getter
    private final double petWidth;

    @Getter
    private final double petHeight;

    private final ReplayablePseudoRandom random;
    private final ParallelTransition wings;
    private final PauseTransition blink = new PauseTransition();
    private Timeline eating;

    /**
     * Whether a need of the pet is low: its usual face is then sad.
     */
    @Getter
    private boolean sad;

    /**
     * Whether it is happy or eating now (not its usual face).
     */
    private boolean busy;

    /**
     * @param size width of the body of the pet
     */
    Mypet(final double size, final ReplayablePseudoRandom random) {
        this.random = random;
        final Image bodyImage = new Image(FOLDER + "body.png");
        final double ratio = size / bodyImage.getWidth();
        final double bodyHeight = bodyImage.getHeight() * ratio;
        petWidth = 1.5 * size;
        petHeight = 1.5 * bodyHeight;

        body = view(bodyImage, ratio);
        body.setLayoutX(petWidth / 2 - size / 2);
        body.setLayoutY(petHeight - bodyHeight);

        final Image wing = new Image(FOLDER + "wing.png");
        leftWing = view(wing, ratio);
        leftWing.setLayoutX(petWidth / 2 - 2 * size / 3);
        leftWing.setRotate(-30);
        rightWing = view(wing, ratio);
        rightWing.setLayoutX(petWidth / 2 + size / 3 - wing.getWidth() * ratio / 2);
        rightWing.setRotate(30);

        mouth = view(mouthImage, ratio);
        mouth.setLayoutX(petWidth / 2 - mouth.getFitWidth() / 2);
        mouth.setLayoutY(3 * petHeight / 4);

        leftEye = view(eyeOpen, ratio);
        leftEye.setLayoutX(petWidth / 2 - petWidth / 8 - leftEye.getFitWidth() / 2);
        leftEye.setLayoutY(petHeight / 2);
        rightEye = view(eyeOpen, ratio);
        rightEye.setLayoutX(petWidth / 2 + petWidth / 8 - rightEye.getFitWidth() / 2);
        rightEye.setLayoutY(petHeight / 2);

        getChildren().addAll(leftWing, rightWing, body, mouth, leftEye, rightEye);
        setMouseTransparent(true);

        final RotateTransition left = new RotateTransition(Duration.millis(150), leftWing);
        left.setToAngle(-60);
        final RotateTransition right = new RotateTransition(Duration.millis(150), rightWing);
        right.setToAngle(60);
        wings = new ParallelTransition(left, right);
        wings.setAutoReverse(true);
        wings.setCycleCount(Animation.INDEFINITE);
        wings.play();

        blink.setOnFinished(event -> blinkOnce());
        waitToBlink();
    }

    private static ImageView view(final Image image, final double ratio) {
        final ImageView view = new ImageView(image);
        view.setFitWidth(image.getWidth() * ratio);
        view.setFitHeight(image.getHeight() * ratio);
        return view;
    }

    private void waitToBlink() {
        blink.setDuration(Duration.millis(2000 + random.nextDouble() * 6000));
        blink.playFromStart();
    }

    private void blinkOnce() {
        if (!busy) {
            setEyes(eyeClosed);
            final PauseTransition open = new PauseTransition(Duration.millis(180));
            open.setOnFinished(event -> {
                if (!busy) {
                    setEyes(eyeOpen);
                }
            });
            open.play();
        }
        waitToBlink();
    }

    private void setEyes(final Image image) {
        leftEye.setImage(image);
        rightEye.setImage(image);
    }

    /**
     * Sets whether a need is low: the usual face is then sad.
     */
    void setSad(final boolean sad) {
        if (this.sad == sad) {
            return;
        }
        this.sad = sad;
        if (!busy) {
            showUsualFace();
        }
    }

    /**
     * Back to the usual face (sad or not), after being happy.
     */
    void calm() {
        busy = false;
        stopEating();
        showUsualFace();
    }

    private void showUsualFace() {
        setEyes(eyeOpen);
        mouth.setImage(sad ? sadImage : mouthImage);
    }

    /**
     * Happy: eyes closed and smiling, until {@link #calm()}.
     */
    void setHappy() {
        busy = true;
        stopEating();
        setEyes(eyeClosed);
        mouth.setImage(smileImage);
    }

    /**
     * Eats: the mouth opens and closes for 2 s, then back to the usual face.
     */
    void eat() {
        busy = true;
        stopEating();
        setEyes(eyeClosed);
        eating = new Timeline();
        for (int i = 0; i < 10; i++) {
            final Image image = i % 2 == 0 ? smileImage : mouthImage;
            eating.getKeyFrames().add(new KeyFrame(Duration.millis(200 * i), event -> mouth.setImage(image)));
        }
        eating.getKeyFrames().add(new KeyFrame(Duration.millis(2000), event -> calm()));
        eating.play();
    }

    private void stopEating() {
        if (eating != null) {
            eating.stop();
            eating = null;
        }
    }

    /**
     * Stops the animations, at the end of the game.
     */
    void stop() {
        wings.stop();
        blink.stop();
        stopEating();
    }
}

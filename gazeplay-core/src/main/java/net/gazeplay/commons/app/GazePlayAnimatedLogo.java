package net.gazeplay.commons.app;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;

@Slf4j
@RequiredArgsConstructor
public class GazePlayAnimatedLogo {

    private static final String IMAGES_PATH = "data/common/images/GazePlayLetters/";

    public static class LetterPane extends StackPane {

        @Getter
        private final ImageView backImageView;

        @Getter
        private final ImageView frontImageView;

        LetterPane(ImageView backImageView, ImageView frontImageView) {
            this.backImageView = backImageView;
            this.frontImageView = frontImageView;
            this.getChildren().addAll(backImageView, frontImageView);
            Configuration config = ActiveConfigurationContext.getInstance();
            if (!config.isBackgroundDark()) {
                this.setStyle("-fx-background-color: #fffaf0; ");
            }
        }

        public Transition createBumpTransition() {
            SequentialTransition bumpTransition = new SequentialTransition();
            int offset = 20;
            TranslateTransition up = new TranslateTransition(Duration.millis(200), frontImageView);
            up.setByY(-offset);
            TranslateTransition down = new TranslateTransition(Duration.millis(100), frontImageView);
            down.setByY(offset);
            bumpTransition.getChildren().addAll(up, down);
            return bumpTransition;
        }

        public Transition createTiltTransition() {
            SequentialTransition tiltTransition = new SequentialTransition();

            //
            int angle = 25;
            RotateTransition one = new RotateTransition(Duration.millis(200), frontImageView);
            one.setByAngle(-angle);
            RotateTransition two = new RotateTransition(Duration.millis(300), frontImageView);
            two.setByAngle(angle);
            //
            int offset = 15;
            TranslateTransition right = new TranslateTransition(Duration.millis(200), frontImageView);
            right.setByX(offset);
            TranslateTransition left = new TranslateTransition(Duration.millis(100), frontImageView);
            left.setByX(-offset);
            //
            tiltTransition.getChildren().addAll(new ParallelTransition(one, left), new ParallelTransition(two, right));
            return tiltTransition;
        }

    }

    public static GazePlayAnimatedLogo newInstance(int preferredHeight) {
        final HBox letters = new HBox();

        //String imageType = "fixed-size";
        String imageType = "flow-size";
        for (int i = 0; i <= 3; i++) {
            StackPane letter = createLetterView(i, imageType, preferredHeight);
            //
            letters.getChildren().add(letter);
        }
        letters.getChildren().add(createSpacingView());
        for (int i = 4; i <= 7; i++) {
            StackPane letter = createLetterView(i, imageType, preferredHeight);
            //
            letters.getChildren().add(letter);
        }

        // we want the letters to overlap a little bit
        letters.setSpacing(-42 * ((double) preferredHeight / 213d));

        BorderPane.setAlignment(letters, Pos.CENTER);
        letters.setAlignment(Pos.CENTER);
        letters.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        Configuration config = ActiveConfigurationContext.getInstance();
        if (!config.isBackgroundDark()) {
            letters.setStyle("-fx-background-color: #fffaf0; ");
        }
        return new GazePlayAnimatedLogo(letters);
    }

    private static StackPane createSpacingView() {
        ImageView imageView = new ImageView(new Image(IMAGES_PATH + "spacing.png"));
        //
        imageView.setPreserveRatio(true);
        //
        StackPane letter = new StackPane();
        letter.getChildren().addAll(imageView);
        return letter;
    }

    private static LetterPane createLetterView(int i, String type, int preferredHeight) {
        log.info("preferredHeight = {}", preferredHeight);
        //
        ImageView backImageView = new ImageView(new Image(IMAGES_PATH + type + "/" + i + "1.png"));
        ImageView frontImageView = new ImageView(new Image(IMAGES_PATH + type + "/" + i + "0.png"));
        //
        backImageView.setPreserveRatio(true);
        frontImageView.setPreserveRatio(true);
        //
        backImageView.setFitHeight(preferredHeight);
        frontImageView.setFitHeight(preferredHeight);
        //
        return new LetterPane(backImageView, frontImageView);
    }

    @Getter
    private final Pane letters;

    public SequentialTransition createAnimation() {
        SequentialTransition fadeOutOneByOne = new SequentialTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(200), letter);
            transition.setFromValue(1);
            transition.setToValue(0);
            fadeOutOneByOne.getChildren().add(transition);
        }
        SequentialTransition fadeInOneByOne = new SequentialTransition();
        for (Node letter : letters.getChildren()) {
            Transition fadeInAndBumpTransition = createFadeInAndBumpTransition(letter);
            fadeInOneByOne.getChildren().add(fadeInAndBumpTransition);
        }

        SequentialTransition fadeInAndOutOneByOne = new SequentialTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition fadeInTransition = new FadeTransition(Duration.millis(500), letter);
            fadeInTransition.setFromValue(0);
            fadeInTransition.setToValue(1);
            FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(200), letter);
            fadeOutTransition.setFromValue(1);
            fadeOutTransition.setToValue(0);
            fadeInAndOutOneByOne.setInterpolator(Interpolator.EASE_BOTH);
            fadeInAndOutOneByOne.getChildren().add(fadeInTransition);
            fadeInAndOutOneByOne.getChildren().add(fadeOutTransition);
        }

        ParallelTransition showAllTransition = new ParallelTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(500), letter);
            transition.setFromValue(0);
            transition.setToValue(1);
            showAllTransition.getChildren().add(transition);
            if (letter instanceof LetterPane) {
                LetterPane letterPane = (LetterPane) letter;
                Transition tiltTransition = letterPane.createTiltTransition();
                showAllTransition.getChildren().add(tiltTransition);
            }
        }
        ParallelTransition hideAllTransition = new ParallelTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(500), letter);
            transition.setFromValue(1);
            transition.setToValue(0);
            hideAllTransition.getChildren().add(transition);
        }

        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().add(hideAllTransition);
        sequentialTransition.getChildren().add(new PauseTransition(Duration.millis(100)));
        sequentialTransition.getChildren().add(fadeInAndOutOneByOne);
        sequentialTransition.getChildren().add(new PauseTransition(Duration.millis(100)));
        sequentialTransition.getChildren().add(fadeInOneByOne);
        sequentialTransition.getChildren().add(new PauseTransition(Duration.millis(1000)));
        sequentialTransition.getChildren().add(fadeOutOneByOne);
        sequentialTransition.getChildren().add(new PauseTransition(Duration.millis(300)));
        sequentialTransition.getChildren().add(showAllTransition);
        sequentialTransition.getChildren().add(new PauseTransition(Duration.millis(3000)));
        sequentialTransition.setInterpolator(Interpolator.LINEAR);

        return sequentialTransition;
    }

    private ParallelTransition createFadeInAndBumpTransition(Node letter) {
        ParallelTransition transition = new ParallelTransition();
        //
        FadeTransition fadeInTransition = new FadeTransition(Duration.millis(500), letter);
        fadeInTransition.setFromValue(0);
        fadeInTransition.setToValue(1);
        transition.getChildren().add(fadeInTransition);
        if (letter instanceof LetterPane) {
            LetterPane letterPane = (LetterPane) letter;
            transition.getChildren().add(letterPane.createBumpTransition());
        }
        return transition;
    }


}

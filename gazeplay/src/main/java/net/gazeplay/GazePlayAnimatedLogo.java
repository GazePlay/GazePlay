package net.gazeplay;

import javafx.animation.*;
import javafx.geometry.Insets;
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

@Slf4j
@RequiredArgsConstructor
public class GazePlayAnimatedLogo {

    public static final String IMAGES_PATH = "data/common/images/GazePlayLetters/";

    public static class LetterPane extends StackPane {

        @Getter
        private final ImageView backImageView;

        @Getter
        private final ImageView frontImageView;

        @Getter
        private final Transition bumpTransition;

        LetterPane(ImageView backImageView, ImageView frontImageView) {
            this.backImageView = backImageView;
            this.frontImageView = frontImageView;
            bumpTransition = createBumpTransition();
            this.getChildren().addAll(backImageView, frontImageView);
        }

        private Transition createBumpTransition() {
            SequentialTransition bumpTransition = new SequentialTransition();
            int offset = 20;
            TranslateTransition up = new TranslateTransition(Duration.millis(200), frontImageView);
            up.setByY(-offset);
            TranslateTransition down = new TranslateTransition(Duration.millis(100), frontImageView);
            down.setByY(offset);
            bumpTransition.getChildren().addAll(up, down);
            return bumpTransition;
        }

    }

    public static GazePlayAnimatedLogo newInstance() {
        final HBox letters = new HBox();
        //String imageType = "fixed-size";
        String imageType = "flow-size";
        for (int i = 0; i <= 3; i++) {
            StackPane letter = createLetterView(i, imageType);
            //
            letters.getChildren().add(letter);
        }
        letters.getChildren().add(createSpacingView());
        for (int i = 4; i <= 7; i++) {
            StackPane letter = createLetterView(i, imageType);
            //
            letters.getChildren().add(letter);
        }

        letters.setSpacing(0);
        letters.setPadding(new Insets(0, 0, 0, 0));

        BorderPane.setAlignment(letters, Pos.CENTER);
        letters.setAlignment(Pos.CENTER);
        letters.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        final BorderPane gamingRoot = new BorderPane();
        gamingRoot.setCenter(letters);
        gamingRoot.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        return new GazePlayAnimatedLogo(gamingRoot, letters);
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

    private static LetterPane createLetterView(int i, String type) {
        ImageView backImageView = new ImageView(new Image(IMAGES_PATH + type + "/" + i + "1.png"));
        ImageView frontImageView = new ImageView(new Image(IMAGES_PATH + type + "/" + i + "0.png"));
        //
        backImageView.setPreserveRatio(true);
        frontImageView.setPreserveRatio(true);
        //
        //backImageView.setFitHeight(primaryStage.getHeight() / 10);
        //frontImageView.setFitHeight(primaryStage.getHeight() / 10);
        //
        return new LetterPane(backImageView, frontImageView);
    }

    @Getter
    private final Pane root;

    private final HBox letters;

    public void runAnimationSequentialFading() {
        SequentialTransition fadeOutOneByOne = new SequentialTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(200), letter);
            transition.setToValue(0);
            fadeOutOneByOne.getChildren().add(transition);
        }
        SequentialTransition fadeInOneByOne = new SequentialTransition();
        for (Node letter : letters.getChildren()) {
            ParallelTransition transition = new ParallelTransition();
            fadeInOneByOne.getChildren().add(transition);
            //
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), letter);
            fadeTransition.setToValue(1);
            transition.getChildren().add(fadeTransition);
            if (letter instanceof LetterPane) {
                LetterPane letterPane = (LetterPane) letter;
                transition.getChildren().add(letterPane.getBumpTransition());
            }
        }

        ParallelTransition showAllTransition = new ParallelTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(500), letter);
            transition.setToValue(1);
            showAllTransition.getChildren().add(transition);
        }
        ParallelTransition hideAllTransition = new ParallelTransition();
        for (Node letter : letters.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(500), letter);
            transition.setToValue(0);
            hideAllTransition.getChildren().add(transition);
        }

        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().add(hideAllTransition);
        sequentialTransition.getChildren().add(fadeInOneByOne);
        sequentialTransition.getChildren().add(fadeOutOneByOne);
        sequentialTransition.getChildren().add(showAllTransition);

        //sequentialTransition.setAutoReverse(true);
        sequentialTransition.setOnFinished(event -> sequentialTransition.play());
        sequentialTransition.play();
    }


}

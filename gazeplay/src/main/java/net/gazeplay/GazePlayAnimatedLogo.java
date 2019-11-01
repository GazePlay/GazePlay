package net.gazeplay;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
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

    public static GazePlayAnimatedLogo newInstance() {
        BorderPane gamingRoot = new BorderPane();

        HBox letters = new HBox();
        for (int i = 0; i < 8; i++) {
            ImageView backImageView = new ImageView(new Image("data/common/images/GazePlayLetters/" + i + 0 + ".png"));
            ImageView frontImageView = new ImageView(new Image("data/common/images/GazePlayLetters/" + i + 1 + ".png"));
            //
            backImageView.setPreserveRatio(true);
            frontImageView.setPreserveRatio(true);
            //
            //backImageView.setFitHeight(primaryStage.getHeight() / 10);
            //frontImageView.setFitHeight(primaryStage.getHeight() / 10);
            //
            StackPane letter = new StackPane();
            letter.getChildren().addAll(frontImageView, backImageView);
            //letter.setOpacity(0);
            letters.getChildren().add(letter);
        }

        letters.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(letters, Pos.CENTER);
        gamingRoot.setCenter(letters);

        letters.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        gamingRoot.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        return new GazePlayAnimatedLogo(gamingRoot, letters);
    }

    @Getter
    private final Pane root;
    
    private final HBox letters;

    public void runAnimation() {
        FadeTransition[][] transitionLetters;
        transitionLetters = new FadeTransition[letters.getChildren().size()][2];

        for (int i = 0; i < letters.getChildren().size(); i++) {
            StackPane letter = (StackPane) letters.getChildren().get(i);
            transitionLetters[i][0] = new FadeTransition(Duration.millis(500), letter);
            transitionLetters[i][0].setToValue(1);
            int index = i;
            transitionLetters[i][0].setOnFinished(event -> {
                if (index == letters.getChildren().size() - 1) {
                    transitionLetters[0][1].play();
                } else {
                    transitionLetters[(index + 1) % letters.getChildren().size()][0].play();
                }
            });
        }

        for (int i = 0; i < letters.getChildren().size(); i++) {
            StackPane letter = (StackPane) letters.getChildren().get(i);
            transitionLetters[i][1] = new FadeTransition(Duration.millis(500), letter);
            transitionLetters[i][1].setToValue(0);
            int index = i;
            transitionLetters[i][1].setOnFinished(event -> {
                if (index == letters.getChildren().size() - 1) {
                    transitionLetters[0][0].play();
                } else {
                    transitionLetters[(index + 1) % letters.getChildren().size()][1].play();
                }
            });
        }

        transitionLetters[0][0].play();
    }

}

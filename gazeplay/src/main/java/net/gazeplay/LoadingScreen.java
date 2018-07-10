package net.gazeplay;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadingScreen extends GraphicalContext<Pane> {

    private static HBox letters;
    private FadeTransition[][] transitionLetters;

    public LoadingScreen(GazePlay gazePlay, Pane root) {

        super(gazePlay, root);

        loadingAnimation();
    }

    public static LoadingScreen newInstance(GazePlay gazePlay) {
        Pane root = new Pane();
        final Stage primaryStage = gazePlay.getPrimaryStage();

        root.prefWidthProperty().bind(primaryStage.widthProperty());
        root.prefHeightProperty().bind(primaryStage.heightProperty());
        root.minWidthProperty().bind(primaryStage.widthProperty());
        root.minHeightProperty().bind(primaryStage.heightProperty());

        BorderPane gamingRoot = new BorderPane();
        gamingRoot.prefWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.prefHeightProperty().bind(primaryStage.heightProperty());
        gamingRoot.minWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.minHeightProperty().bind(primaryStage.heightProperty());

        letters = new HBox();
        for (int i = 0; i < 8; i++) {
            StackPane letter = new StackPane();
            ImageView Back = new ImageView(new Image("data/common/images/GazePlayLetters/" + i + 0 + ".png"));
            ImageView Front = new ImageView(new Image("data/common/images/GazePlayLetters/" + i + 1 + ".png"));
            Back.setPreserveRatio(true);
            Front.setPreserveRatio(true);
            Back.setFitHeight(primaryStage.getHeight() / 10);
            Front.setFitHeight(primaryStage.getHeight() / 10);
            letter.getChildren().addAll(Front, Back);
            letter.setOpacity(0);
            letters.getChildren().add(letter);
        }

        letters.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(letters, Pos.CENTER);
        gamingRoot.setCenter(letters);

        letters.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        gamingRoot.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        root.getChildren().add(gamingRoot);

        return new LoadingScreen(gazePlay, root);
    }

    public void loadingAnimation() {
        transitionLetters = new FadeTransition[letters.getChildren().size()][2];

        for (int i = 0; i < letters.getChildren().size(); i++) {
            StackPane letter = (StackPane) letters.getChildren().get(i);
            transitionLetters[i][0] = new FadeTransition(Duration.millis(500), letter);
            transitionLetters[i][0].setToValue(1);
            int index = i;
            transitionLetters[i][0].setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (index == letters.getChildren().size() - 1) {
                        transitionLetters[0][1].play();
                    } else {
                        transitionLetters[(index + 1) % letters.getChildren().size()][0].play();
                    }
                }
            });
        }

        for (int i = 0; i < letters.getChildren().size(); i++) {
            StackPane letter = (StackPane) letters.getChildren().get(i);
            transitionLetters[i][1] = new FadeTransition(Duration.millis(500), letter);
            transitionLetters[i][1].setToValue(0);
            int index = i;
            transitionLetters[i][1].setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (index == letters.getChildren().size() - 1) {
                        transitionLetters[0][0].play();
                    } else {
                        transitionLetters[(index + 1) % letters.getChildren().size()][1].play();
                    }
                }
            });
        }

        transitionLetters[0][0].play();

    }

    @Override
    public ObservableList<Node> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }
}

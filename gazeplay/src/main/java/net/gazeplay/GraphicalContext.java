package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.HomeButton;

@Data
@Slf4j
public class GraphicalContext {

    @Getter
    private final GazePlay gazePlay;

    @Getter
    protected final Group root;

    @Getter
    protected final Scene scene;

    protected HomeButton homeButton;

    public void setUpOnStage(Stage stage) {
        stage.setTitle("GazePlay");
        stage.setScene(scene);
        stage.setFullScreen(false); // fullscreen seem to be very slow
        stage.setOnCloseRequest((WindowEvent we) -> stage.close());

        if (homeButton != null) {
            homeButton.setVisible(true);
            homeButton.toFront();
        }

        stage.show();
    }

    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public void hideHomeButton() {
        this.homeButton.setVisible(false);
    }

    public void showHomeButton() {
        this.homeButton.setVisible(false);
    }

    public void clear() {
        getScene().setFill(Color.BLACK);
        getChildren().clear();

        log.info("Nodes not removed: {}", getChildren().size());

        if (homeButton != null) {
            getChildren().add(homeButton);
            homeButton.setVisible(true);
            homeButton.toFront();
        }
    }

    public void onGameStarted() {
        if (homeButton != null) {
            homeButton.setVisible(true);
            homeButton.toFront();
        }
    }

}

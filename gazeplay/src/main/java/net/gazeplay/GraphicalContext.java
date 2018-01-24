package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.CssUtil;

@Data
@Slf4j
public abstract class GraphicalContext<T> {

    @Getter
    private final GazePlay gazePlay;

    protected final T root;

    @Getter
    protected final Scene scene;

    public void setUpOnStage(Stage stage) {
        stage.setTitle("GazePlay");

        // setting the scene again will exit fullscreen
        // so we need to backup the fullscreen status, and restore it after the scene has been set
        boolean fullscreen = stage.isFullScreen();
        stage.setScene(scene);
        stage.setFullScreen(fullscreen);

        stage.setOnCloseRequest((WindowEvent we) -> stage.close());

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

        stage.show();
        log.info("Finished setup stage with the game scene");
    }

    public abstract ObservableList<Node> getChildren();

    public void clear() {
        getScene().setFill(Color.BLACK);
        getChildren().clear();

        log.info("Nodes not removed: {}", getChildren().size());
    }

    public void onGameStarted() {
    }

}

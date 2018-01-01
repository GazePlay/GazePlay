package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.StatsDisplay;

@Data
@Slf4j
public class GameContext {

    @Getter
    private final GazePlay gazePlay;

    @Getter
    private final Group root;

    @Getter
    private final Scene scene;

    private HomeButton homeButton;

    public void setUpOnStage(Stage primaryStage) {
        primaryStage.setTitle("GazePlay");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(false); // fullscreen seem to be very slow
        primaryStage.setOnCloseRequest((WindowEvent we) -> gazePlay.onReturnToMenu());
        primaryStage.show();
    }

    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public void clear() {
        getScene().setFill(Color.BLACK);

        getChildren().clear();

        log.info("Nodes not removed: {}", getChildren().size());

        Bravo bravo = Bravo.getBravo();
        bravo.setVisible(false);
        getChildren().add(bravo);
    }

    public void hideHomeButton() {
        this.homeButton.setVisible(false);
    }

    public void showHomeButton() {
        this.homeButton.setVisible(false);
    }

    public void createHomeButtonInGameScreen(GazePlay gazePlay, Stats stats) {

        double width = scene.getWidth() / 10;
        double height = width;
        double X = scene.getWidth() * 0.9;
        double Y = scene.getHeight() - height * 1.1;

        HomeButton homeButton = new HomeButton(X, Y, width, height);

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    scene.setCursor(Cursor.WAIT); // Change cursor to wait style

                    log.info("stats = " + stats);

                    Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
                    StatsDisplay.displayStats(gazePlay, stats, GameContext.this, config);

                    scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
                }
            }
        };

        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(homeButton);

        this.homeButton = homeButton;
    }

}

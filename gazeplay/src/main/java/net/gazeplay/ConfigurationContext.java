package net.gazeplay;

import com.sun.glass.ui.Screen;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.HomeButton;

@Slf4j
public class ConfigurationContext extends GraphicalContext {

    public static ConfigurationContext newInstance(GazePlay gazePlay) {
        Group root = new Group();

        final Screen screen = Screen.getScreens().get(0);
        log.info("Screen size: {} x {}", screen.getWidth(), screen.getHeight());

        Scene scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.BLACK);
        return new ConfigurationContext(gazePlay, root, scene);
    }

    private ConfigurationContext(GazePlay gazePlay, Group root, Scene scene) {
        super(gazePlay, root, scene);
    }

    public void createHomeButtonInConfigurationManagementScreen(@NonNull GazePlay gazePlay) {

        HomeButton homeButton = new HomeButton();

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    scene.setCursor(Cursor.WAIT); // Change cursor to wait style

                    gazePlay.onReturnToMenu();

                    scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
                }
            }
        };

        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        homeButton.recomputeSizeAndPosition(scene);
        getChildren().add(homeButton);

        this.homeButton = homeButton;
    }

}

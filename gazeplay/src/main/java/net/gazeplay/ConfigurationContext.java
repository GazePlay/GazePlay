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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.HomeButton;

@Slf4j
public class ConfigurationContext extends GraphicalContext<Group> {

    public static ConfigurationContext newInstance(GazePlay gazePlay) {
        Group root = new Group();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

        return new ConfigurationContext(gazePlay, root, scene);
    }

    private ConfigurationContext(GazePlay gazePlay, Group root, Scene scene) {
        super(gazePlay, root, scene);
    }

    public HomeButton createHomeButtonInConfigurationManagementScreen(@NonNull GazePlay gazePlay) {

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

        return homeButton;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}

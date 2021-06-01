package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import net.gazeplay.GazePlay;
import net.gazeplay.components.ProgressButton;

public class ConfigurationButtonFactory {

    public static StackPane createConfigurationButton(GazePlay gazePlay) {
        StackPane Pconf = new StackPane();
        ProgressButton Bconf = new ProgressButton();

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        ConfigurationButton configurationButton = new ConfigurationButton(screenDimension);

        Pconf.getChildren().addAll(configurationButton, Bconf);

        configurationButton.addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            e -> gazePlay.onDisplayConfigurationManagement());

        Bconf.assignIndicatorUpdatable(e -> {gazePlay.onDisplayConfigurationManagement();});
        Bconf.active();
        Bconf.getButton().setRadius(50);
        Bconf.getButton().setVisible(false);

        return Pconf;
    }

}

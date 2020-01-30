package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.screen.PrimaryScreenDimensionSupplier;

public class ConfigurationButtonFactory {

    public static ConfigurationButton createConfigurationButton(GazePlay gazePlay) {
        Dimension2D screenDimension = new PrimaryScreenDimensionSupplier().get();
        ConfigurationButton configurationButton = new ConfigurationButton(screenDimension);
        configurationButton.addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            e -> gazePlay.onDisplayConfigurationManagement());
        return configurationButton;
    }

}

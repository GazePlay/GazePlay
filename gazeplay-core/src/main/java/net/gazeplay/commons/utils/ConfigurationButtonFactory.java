package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import net.gazeplay.GazePlay;

/**
 * Creates the button opening the settings page.
 */
public class ConfigurationButtonFactory {

    /**
     * @return a button opening the settings page when clicked
     */
    public static ConfigurationButton createConfigurationButton(GazePlay gazePlay) {
        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        ConfigurationButton configurationButton = new ConfigurationButton(screenDimension);
        configurationButton.addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            e -> gazePlay.onDisplayConfigurationManagement());
        return configurationButton;
    }

}

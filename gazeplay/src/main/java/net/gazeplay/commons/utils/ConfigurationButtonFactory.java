package net.gazeplay.commons.utils;

import javafx.scene.input.MouseEvent;
import net.gazeplay.GazePlay;

public class ConfigurationButtonFactory {

    public static ConfigurationButton createConfigurationButton(GazePlay gazePlay) {
        ConfigurationButton configurationButton = new ConfigurationButton();
        configurationButton.addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            e -> gazePlay.onDisplayConfigurationManagement());
        return configurationButton;
    }

}

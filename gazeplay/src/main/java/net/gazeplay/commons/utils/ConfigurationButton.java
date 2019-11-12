package net.gazeplay.commons.utils;

import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;

/**
 * Created by schwab on 28/10/2017.
 */

@Slf4j
public class ConfigurationButton extends CustomButton {

    public static ConfigurationButton createConfigurationButton(GazePlay gazePlay) {
        ConfigurationButton configurationButton = new ConfigurationButton();
        configurationButton.addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            e -> gazePlay.onDisplayConfigurationManagement());
        return configurationButton;
    }

    private ConfigurationButton() {
        super("data/common/images/configuration-button-alt4.png");
    }

}

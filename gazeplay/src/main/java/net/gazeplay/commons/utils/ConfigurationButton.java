package net.gazeplay.commons.utils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.ConfigurationContext;

/**
 * Created by schwab on 28/10/2017.
 */

@Slf4j
public class ConfigurationButton extends CustomButton {

    public static ConfigurationButton createConfigurationButton(ConfigurationContext configurationContext) {
        ConfigurationButton configurationButton = new ConfigurationButton();
        /*
         * configurationButton.addEventFilter(KeyEvent.KEY_PRESSED,new EventHandler<KeyEvent>() {
         * 
         * @Override public void handle(KeyEvent ke) { if (ke.getCode() == KeyCode.SPACE) {
         * configurationContext.getGazePlay().onReturnToMenu(); } } });
         */
        configurationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> {
            configurationContext.getGazePlay().onDisplayConfigurationManagement(configurationContext);
        });
        return configurationButton;
    }

    private ConfigurationButton() {
        super("data/common/images/configuration-button-alt4.png");
    }

}

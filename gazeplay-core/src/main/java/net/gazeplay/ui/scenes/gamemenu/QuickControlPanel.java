package net.gazeplay.ui.scenes.gamemenu;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import lombok.Getter;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.ui.MusicControl;

/**
 * Panel of the menu with the settings button and the music controls.
 */
public class QuickControlPanel {

    @Getter
    private static final QuickControlPanel instance = new QuickControlPanel();

    /**
     * @return the panel
     */
    public HBox createQuickControlPanel(
        GazePlay gazePlay,
        MusicControl musicControl,
        ConfigurationButton configurationButton,
        Configuration config
    ) {
        HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.getChildren().add(configurationButton);
        leftControlPane.getChildren().add(musicControl.createMusicControlPane());
        leftControlPane.getChildren().add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()));
        return leftControlPane;
    }

}

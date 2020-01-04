package net.gazeplay.ui.scenes.gamemenu;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import lombok.Getter;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.ui.MusicControl;

public class QuickControlPanel {

    @Getter
    private static final QuickControlPanel instance = new QuickControlPanel();

    public HBox createQuickControlPanel(
        GazePlay gazePlay,
        MusicControl musicControl,
        ConfigurationButton configurationButton,
        Configuration config
    ) {
        HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.getChildren().add(configurationButton);
        leftControlPane.getChildren().add(musicControl.createMusicControlPane());
        leftControlPane.getChildren().add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()));
        return leftControlPane;
    }

}

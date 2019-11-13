package net.gazeplay.games.pianosight;

import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

import java.io.File;

@Slf4j
public class Jukebox extends Pane {

    private final IGameContext gameContext;

    Jukebox(IGameContext gameContext) {
        super();
        this.gameContext = gameContext;
    }

    public String getS() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Midi & Mid Files", "*.midi", "*.mid"),
            new ExtensionFilter("Midi Files", "*.midi"),
            new ExtensionFilter("Mid Files", "*.mid")
        );
        File selectedFile = fileChooser.showOpenDialog(gameContext.getPrimaryStage());
        String s = null;
        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
        }
        return s;
    }

}

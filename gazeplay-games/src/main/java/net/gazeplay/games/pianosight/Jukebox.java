package net.gazeplay.games.pianosight;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

import java.io.File;

@Slf4j
public class Jukebox extends Pane {

    HBox musiques;
    String s;
    IGameContext gameContext;

    public String getS() {
        String s = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Midi & Mid Files", "*.midi", "*.mid"),
                new ExtensionFilter("Midi Files", "*.midi"), new ExtensionFilter("Mid Files", "*.mid"));
        File selectedFile = fileChooser.showOpenDialog(gameContext.getPrimaryStage());
        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
        }
        return s;
    }

    public Jukebox(IGameContext gctx) {
        super();
        gameContext = gctx;
    }

}

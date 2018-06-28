package net.gazeplay.games.pianosight;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class Jukebox extends Pane {

    HBox musiques;
    String s;
    GameContext gc;

    public String getS() {
        String s = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Midi & Mid Files", "*.midi", "*.mid"),
                new ExtensionFilter("Midi Files", "*.midi"), new ExtensionFilter("Mid Files", "*.mid"));
        File selectedFile = fileChooser.showOpenDialog(gc.getGazePlay().getPrimaryStage());
        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
        }
        return s;
    }

    public Jukebox(GameContext gctx) {
        super();
        gc = gctx;
    }

}

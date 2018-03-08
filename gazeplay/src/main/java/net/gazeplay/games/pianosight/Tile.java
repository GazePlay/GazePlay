package net.gazeplay.games.pianosight;

import javafx.scene.shape.Arc;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Tile {

    int note;
    Arc arc;

    public Tile() {
        note = 0;
        arc = new Arc();
    }
}

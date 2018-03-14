package net.gazeplay.games.pianosight;

import javafx.scene.shape.Arc;

public class Tile extends Arc {

    int note;

    public Tile() {
        super();
        note = 0;
    }

    public Tile(double d1, double d2, double d3, double d4, int i, double d5) {
        super(d1, d2, d3, d4, i, d5);

    }
}

package net.gazeplay.games.cups.utils;

import lombok.Getter;

/**
 * A movement of a cup, from a cell of the grid to another.
 */
public class Action {

    @Getter
    private final int initCellX;
    @Getter
    private final int initCellY;
    @Getter
    private final int targetCellX;
    @Getter
    private final int targetCellY;

    public Action(int initCellX, int initCellY, int targetCellX, int targetCellY) {
        this.initCellX = initCellX;
        this.initCellY = initCellY;
        this.targetCellX = targetCellX;
        this.targetCellY = targetCellY;
    }
}

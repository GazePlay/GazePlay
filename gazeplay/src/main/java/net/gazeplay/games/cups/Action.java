package net.gazeplay.games.cups;

import lombok.Getter;

public class Action {
    
    @Getter
    private int initCellX;
    @Getter
    private int initCellY;
    @Getter
    private int targetCellX;
    @Getter
    private int targetCellY;

    public Action(int initCellX, int initCellY, int targetCellX, int targetCellY){
        this.initCellX = initCellX;
        this.initCellY = initCellY;
        this.targetCellX = targetCellX;
        this.targetCellY = targetCellY;
    }
}

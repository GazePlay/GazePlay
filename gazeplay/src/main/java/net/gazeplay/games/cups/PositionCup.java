package net.gazeplay.games.cups;

import java.awt.Point;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PositionCup {
    @Getter @Setter
    private int cellX;
    @Getter @Setter
    private int cellY;
    private int maxCellsX;
    private int maxCellsY;

    private double screenHeight;
    private double screenWidth;
    @Getter
    private double imageWidth;
    @Getter
    private double imageHeight;

    public PositionCup(int initCellX, int initCellY, int maxCellsX, int maxCellsY, double screenHeight,
            double screenWidth, double imageWidth, double imageHeight) {
        this.cellX = initCellX;
        this.cellY = initCellY;
        this.maxCellsX = maxCellsX;
        this.maxCellsY = maxCellsY;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public Point calculateXY(int newCellX, int newCellY) {
        double cellSizeWidth = screenWidth / maxCellsX;
        double cellSizeHeight = screenHeight / maxCellsY;
        double newXPos = cellSizeWidth * newCellX + (cellSizeWidth - imageWidth) / 2;
        double newYPos = cellSizeHeight * newCellY + (cellSizeHeight - imageHeight) / 2;
        return new Point((int) newXPos, (int) newYPos);
    }
}

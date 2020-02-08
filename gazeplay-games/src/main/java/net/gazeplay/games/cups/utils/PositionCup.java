package net.gazeplay.games.cups.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class PositionCup {
    @Getter
    @Setter
    private int cellX;
    @Getter
    @Setter
    private int cellY;
    private final int maxCellsX;
    private final int maxCellsY;

    private final double screenHeight;
    private final double screenWidth;
    @Getter
    private final double imageWidth;
    @Getter
    private final double imageHeight;

    public PositionCup(final int initCellX, final int initCellY, final int maxCellsX, final int maxCellsY, final double screenHeight,
                       final double screenWidth, final double imageWidth, final double imageHeight) {
        this.cellX = initCellX;
        this.cellY = initCellY;
        this.maxCellsX = maxCellsX;
        this.maxCellsY = maxCellsY;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public Point calculateXY(final int newCellX, final int newCellY) {
        final double cellSizeWidth = screenWidth / maxCellsX;
        final double cellSizeHeight = screenHeight / maxCellsY;
        final double newXPos = cellSizeWidth * newCellX + (cellSizeWidth - imageWidth) / 2;
        final double newYPos = cellSizeHeight * newCellY + (cellSizeHeight - imageHeight) / 2;
        return new Point((int) newXPos, (int) newYPos);
    }
}

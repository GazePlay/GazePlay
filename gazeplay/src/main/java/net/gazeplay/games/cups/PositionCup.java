package net.gazeplay.games.cups;

import java.awt.Point;
import lombok.Getter;
import lombok.Setter;

public class PositionCup {
    @Getter @Setter
    private int cellX;
    @Getter @Setter
    private int cellY;
    private int maxCellsX;
    private int maxCellsY;
    
    private double height;
    private double width;
    @Getter
    double imageWidth;
    @Getter
    double imageHeight;

    double cellSizeWidth = width / maxCellsX;
    double cellSizeHeight = height / maxCellsY;
        
    public PositionCup(int cellX, int cellY, int maxCellsX, int maxCellsY, double height, double width, 
            double imageWidth, double imageHeight) {
        this.cellX = cellX;
        this.cellY = cellY;
        this.maxCellsX = maxCellsX;
        this.maxCellsY = maxCellsY;
        this.height = height;
        this.width = width;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public Point calculateXY(int newCellX, int newCellY) {
        double newXPos = cellSizeWidth*cellX + (cellSizeWidth - imageWidth)/2;
        double newYPos = cellSizeHeight*cellY+ (cellSizeHeight - imageHeight)/2;

        return new Point((int) newXPos, (int) newYPos);
    }
    
}

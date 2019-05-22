package net.gazeplay.commons.utils.stats;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class AreaOfInterestProps {
    private final long TTFF;
    private final ArrayList listOfPoints;
//    private final int revisits;
    private final long timeSpent;
    private final int fixations;
    private final int centerX;
    private final int centerY;
    private final Double[] convexPoints;


    public AreaOfInterestProps(long TTFF, long timeSpent, ArrayList listOfPoints, int centerX , int centerY, Double[] convexPoints)
    {
        this.TTFF = TTFF;
        this.listOfPoints = listOfPoints;
        this.timeSpent = timeSpent;
        this.fixations = listOfPoints.size();
        this.centerX = centerX;
        this.centerY = centerY;
        this.convexPoints = convexPoints;
    }
    public Double[] getConvexPoints(){
        return this.convexPoints;
    }
    public int getCenterX() {
        return this.centerX;
    }
    public int getCenterY(){
        return this.centerY;
    }
    public int getFixations(){
        return this.fixations;
    }
    public long getTimeSpent(){
        return this.timeSpent;
    }
    public long getTTFF(){
        return this.TTFF;
    }

}

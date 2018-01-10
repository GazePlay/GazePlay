package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import lombok.AllArgsConstructor;

import java.util.Random;

@AllArgsConstructor
public abstract class RandomPositionGenerator {

    private final Random random = new Random();

    public abstract Dimension2D getDimension2D();

    public Position newRandomPosition(double radius) {

        double minX = radius;
        double minY = radius;

        Dimension2D dimension2D = getDimension2D();

        double maxX = dimension2D.getWidth() - radius;
        double maxY = dimension2D.getHeight() - radius;
        if (maxX > 0 && maxY > 0) {
            double positionX = random.nextInt((int) (maxX - minX)) + minX;
            double positionY = random.nextInt((int) (maxY - minY)) + minY;
            return new Position((int) positionX, (int) positionY);
        } else {
            return new Position((int) radius, (int) radius);
        }
    }

}

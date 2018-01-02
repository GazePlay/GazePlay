package net.gazeplay.commons.utils;

import javafx.scene.Scene;
import lombok.AllArgsConstructor;

import java.util.Random;

@AllArgsConstructor
public class RandomPositionGenerator {

    private final Random random = new Random();

    private final Scene scene;

    public Position newRandomPosition(double radius) {

        double minX = radius;
        double minY = radius;
        double maxX = scene.getWidth() - radius;
        double maxY = scene.getHeight() - radius;
        double positionX = random.nextInt((int) (maxX - minX)) + minX;
        double positionY = random.nextInt((int) (maxY - minY)) + minY;
        return new Position((int) positionX, (int) positionY);
    }

}

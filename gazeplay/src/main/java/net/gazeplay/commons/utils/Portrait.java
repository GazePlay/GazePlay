package net.gazeplay.commons.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.gazeplay.commons.utils.games.Utils;

import java.util.Random;

/**
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    public static Image[] loadAllImages() {
        return Utils.images(Utils.getImagesFolder() + "portraits");
    }

    private final RandomPositionGenerator randomPositionGenerator;

    @Getter
    private final int initialRadius;

    public Portrait(int initialRadius, RandomPositionGenerator randomPositionGenerator, Image[] availableImages) {
        super(initialRadius);
        this.initialRadius = initialRadius;
        this.randomPositionGenerator = randomPositionGenerator;

        this.setPosition(randomPositionGenerator.newRandomPosition(initialRadius));

        setFill(new ImagePattern(pickRandomImage(availableImages), 0, 0, 1, 1, true));
    }

    public void setPosition(Position position) {
        this.setCenterX(position.getX());
        this.setCenterY(position.getY());
    }

    public Position getPosition() {
        return new Position((int) getCenterX(), (int) getCenterY());
    }

    public Position getCurrentPositionWithTranslation() {
        return new Position((int) getCenterX() + (int) getTranslateX(), (int) getCenterY() + (int) getTranslateY());
    }

    protected Image pickRandomImage(Image[] availableImages) {
        int count = availableImages.length;
        int index = (int) (count * Math.random());
        return availableImages[index];
    }

    @Data
    public static class Position {
        private final int x;
        private final int y;
    }

    @AllArgsConstructor
    public static class RandomPositionGenerator {

        private final Random random = new Random();

        private final Scene scene;

        public Position newRandomPosition(double radius) {

            // if (scene.getWidth() == 0 || scene.getHeight() == 0) {
            // return new Position(0, 0);
            // }

            double minX = radius;
            double minY = radius;
            double maxX = scene.getWidth() - radius;
            double maxY = scene.getHeight() - radius;
            double positionX = random.nextInt((int) (maxX - minX)) + minX;
            double positionY = random.nextInt((int) (maxY - minY)) + minY;
            return new Position((int) positionX, (int) positionY);
        }

    }

}

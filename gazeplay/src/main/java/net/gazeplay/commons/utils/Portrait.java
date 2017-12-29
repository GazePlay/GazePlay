package net.gazeplay.commons.utils;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
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

    protected static final RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator();

    @Getter
    private final int initialRadius;

    public Portrait(int initialRadius, Scene scene, Image[] availableImages) {
        super(initialRadius);
        this.initialRadius = initialRadius;

        this.setPosition(randomPositionGenerator.newRandomPosition(initialRadius, scene));

        setFill(new ImagePattern(pickRandomImage(availableImages), 0, 0, 1, 1, true));
    }

    public void setPosition(Position position) {
        this.setCenterX(position.getX());
        this.setCenterY(position.getY());
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

    public static class RandomPositionGenerator {

        private static final int initRadius = 100;

        private static Rectangle2D getScreenBounds() {
            return Screen.getPrimary().getBounds();
        }

        private static Rectangle2D getSceneBounds(Scene scene) {
            return new Rectangle2D(0, 0, scene.getWidth(), scene.getHeight());
        }

        private final Random random = new Random();

        public Position newRandomPosition(double radius, Scene scene) {
            Rectangle2D bounds = getSceneBounds(scene);
            double maxX = bounds.getMaxX() - 2 * initRadius;
            double maxY = bounds.getMaxY() - 2 * initRadius;
            double positionX = random.nextInt((int) maxX) + radius;
            double positionY = random.nextInt((int) maxY) + radius;
            return new Position((int) positionX, (int) positionY);
        }

    }

}

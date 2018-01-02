package net.gazeplay.commons.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import lombok.Getter;
import net.gazeplay.commons.utils.games.Utils;

/**
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    public static Image[] loadAllImages() {
        return Utils.images(Utils.getImagesFolder() + "portraits");
    }

    @Getter
    private final int initialRadius;

    public Portrait(int initialRadius, RandomPositionGenerator randomPositionGenerator, Image[] availableImages) {
        super(initialRadius);
        this.initialRadius = initialRadius;

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

}

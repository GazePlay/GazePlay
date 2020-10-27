package net.gazeplay.components;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;

import java.util.List;

/**
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    public static ImageLibrary createImageLibrary(ReplayablePseudoRandom randomGenerator) {
        return ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"), randomGenerator);
    }

    @Getter
    private final int initialRadius;

    public Portrait(final int initialRadius, final RandomPositionGenerator randomPositionGenerator, final ImageLibrary imageLibrary) {
        super(initialRadius);
        this.initialRadius = initialRadius;

        this.setPosition(randomPositionGenerator.newRandomPosition(initialRadius));

        setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
    }

    public void setPosition(final Position position) {
        this.setCenterX(position.getX());
        this.setCenterY(position.getY());
    }

    public Position getPosition() {
        return new Position((int) getCenterX(), (int) getCenterY());
    }

    public Position getCurrentPositionWithTranslation() {
        return new Position((int) getCenterX() + (int) getTranslateX(), (int) getCenterY() + (int) getTranslateY());
    }

    protected Image pickRandomImage(final List<Image> availableImages) {
        final int count = availableImages.size();
        final ReplayablePseudoRandom r = new ReplayablePseudoRandom();
        final int index = r.nextInt(count);
        return availableImages.get(index);
    }

}

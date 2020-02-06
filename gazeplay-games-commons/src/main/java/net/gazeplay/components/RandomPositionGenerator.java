package net.gazeplay.components;

import javafx.geometry.Dimension2D;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@AllArgsConstructor
public abstract class RandomPositionGenerator {

    private final Random random = new Random();

    public abstract Dimension2D getDimension2D();

    public Position newRandomPosition(final double radius) {

        final Dimension2D dimension2D = getDimension2D();

        final double maxX = dimension2D.getWidth() - radius;
        final double maxY = dimension2D.getHeight() - radius;

        return createPosition(radius, radius, maxX, maxY);
    }

    public Position newRandomBoundedPosition(final double radius, final double ratioXLeft, final double ratioXRight, final double ratioYBottom,
                                             final double ratioYTop) {

        double minX = radius;
        double minY = radius;

        final Dimension2D dimension2D = getDimension2D();

        minX = minX + (dimension2D.getWidth() * ratioXLeft);
        minY = minY + (dimension2D.getHeight() * ratioYBottom);

        final double maxX = (dimension2D.getWidth() * ratioXRight) - radius;
        final double maxY = (dimension2D.getHeight() * ratioYTop) - radius;

        log.debug("the width is ={}", dimension2D.getWidth());
        log.debug("the height is ={}", dimension2D.getHeight());
        log.debug("the minX is ={}", minX);
        log.debug("the minY is ={}", minY);
        log.debug("the maxX is ={}", maxX);
        log.debug("the maxY is ={}", maxY);

        return createPosition(minX, minY, maxX, maxY);
    }

    public Position createPosition(final double minX, final double minY, final double maxX, final double maxY) {
        if (maxX > 0 && maxY > 0) {
            final double positionX = random.nextInt((int) (maxX - minX)) + minX;
            final double positionY = random.nextInt((int) (maxY - minY)) + minY;
            log.debug("the posX is ={}", positionX);
            log.debug("the posY is ={}", positionY);

            return new Position((int) positionX, (int) positionY);
        } else {
            return new Position((int) minX, (int) minY);
        }
    }

}

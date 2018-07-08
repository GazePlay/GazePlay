package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
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

        return createPosition(minX, minY, maxX, maxY);
    }

    public Position newRandomBoundedPosition(double radius, double ratioXLeft, double ratioXRight, double ratioYBottom,
            double ratioYTop) {

        double minX = radius;
        double minY = radius;

        Dimension2D dimension2D = getDimension2D();

        minX = minX + (dimension2D.getWidth() * ratioXLeft);
        minY = minY + (dimension2D.getHeight() * ratioYBottom);

        double maxX = (dimension2D.getWidth() * ratioXRight) - radius;
        double maxY = (dimension2D.getHeight() * ratioYTop) - radius;

        log.info("the width is ={}", dimension2D.getWidth());
        log.info("the height is ={}", dimension2D.getHeight());
        log.info("the minX is ={}", minX);
        log.info("the minY is ={}", minY);
        log.info("the maxX is ={}", maxX);
        log.info("the maxY is ={}", maxY);

        return createPosition(minX, minY, maxX, maxY);
    }

    public Position createPosition(double minX, double minY, double maxX, double maxY) {
        if (maxX > 0 && maxY > 0) {
            double positionX = random.nextInt((int) (maxX - minX)) + minX;
            double positionY = random.nextInt((int) (maxY - minY)) + minY;
            log.info("the posX is ={}", positionX);
            log.info("the posY is ={}", positionY);

            return new Position((int) positionX, (int) positionY);
        } else {
            return new Position((int) minX, (int) minY);
        }
    }

}

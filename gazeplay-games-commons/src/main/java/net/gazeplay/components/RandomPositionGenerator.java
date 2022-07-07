package net.gazeplay.components;

import javafx.geometry.Dimension2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.Random;

@Slf4j
public abstract class RandomPositionGenerator {
    @Setter
    private ReplayablePseudoRandom randomGenerator = new ReplayablePseudoRandom();

    public RandomPositionGenerator(ReplayablePseudoRandom randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public abstract Dimension2D getDimension2D();

    public Position newRandomPosition(final double radius) {

        final Dimension2D dimension2D = getDimension2D();

        final double maxX = dimension2D.getWidth() - radius;
        final double maxY = dimension2D.getHeight() - radius;

        return createPosition(radius, radius, maxX, maxY);
    }

    public Position newRandomBoundedPosition(final double radius, final double ratioXLeft, final double ratioXRight, final double ratioYBottom,
                                             final double ratioYTop) {

        final Dimension2D dimension2D = getDimension2D();

        final double minX = (dimension2D.getWidth() * ratioXLeft) + radius;
        final double minY = (dimension2D.getHeight() * ratioYBottom) + radius;

        final double maxX = (dimension2D.getWidth() * ratioXRight) - radius * 2;
        final double maxY = (dimension2D.getHeight() * ratioYTop) - radius * 2;

        log.debug("the width is ={}", dimension2D.getWidth());
        log.debug("the height is ={}", dimension2D.getHeight());
        log.debug("the minX is ={}", minX);
        log.debug("the minY is ={}", minY);
        log.debug("the maxX is ={}", maxX);
        log.debug("the maxY is ={}", maxY);

         return createPosition(minX, minY, maxX, maxY);
    }
    public Position newRandomBoundedPositionCreamPie(final double radius, final double ratioXLeft, final double ratioXRight, final double ratioYBottom,
                                             final double ratioYTop) {

        final Dimension2D dimension2D = getDimension2D();

        final double minX = (dimension2D.getWidth() * ratioXLeft) + radius *1.6;
        final double minY = (dimension2D.getHeight() * ratioYBottom) + radius *1.6;

        final double maxX = (dimension2D.getWidth() * ratioXRight) - radius *1.6;
        final double maxY = (dimension2D.getHeight() * ratioYTop) - radius *1.6;

        log.debug("the width is ={}", dimension2D.getWidth());
        log.debug("the height is ={}", dimension2D.getHeight());
        log.debug("the minX is ={}", minX);
        log.debug("the minY is ={}", minY);
        log.debug("the maxX is ={}", maxX);
        log.debug("the maxY is ={}", maxY);

        return createPositionCreamPie(minX, minY, maxX, maxY);
    }

    public Position createPosition(final double minX, final double minY, final double maxX, final double maxY) {
        if (maxX > 0 && maxY > 0) {
            final double positionX = randomGenerator.nextDouble(((maxX - minX + 1) + minX));
            final double positionY = randomGenerator.nextDouble(((maxY - minY + 1) + minY));
            log.debug("the posX is ={}", positionX);
            log.debug("the posY is ={}", positionY);
            return new Position(positionX, positionY);
        } else {
            return new Position(minX, minY);
        }
    }
    public Position createPositionCreamPie(final double minX, final double minY, final double maxX, final double maxY) {

        if (maxX > 0 && maxY > 0) {
            final Random r = new Random();
            double positionX = r.nextDouble()*maxX + minX;
            double positionY = r.nextDouble()*maxY + minY;
            log.debug("the posX is ={}", positionX);
            log.debug("the posY is ={}", positionY);
            if (positionX > maxX) {
                positionX = maxX;
            }
            if (positionY > maxY) {
                positionY = maxY;
            }
            return new Position(positionX, positionY);
        }
        return new Position(minX, minY);
    }
}

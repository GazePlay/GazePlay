package net.gazeplay.components;

import javafx.geometry.Dimension2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

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

        final double minX = (dimension2D.getWidth() * ratioXLeft) + radius * 2;
        final double minY = (dimension2D.getHeight() * ratioYBottom) + radius * 2;

        final double maxX = (dimension2D.getWidth() * ratioXRight) - radius * 2;
        final double maxY = (dimension2D.getHeight() * ratioYTop) - radius * 2;

        log.debug("the width is ={}", dimension2D.getWidth());
        log.debug("the height is ={}", dimension2D.getHeight());
        log.debug("the minX is ={}", minX);
        log.debug("the minY is ={}", minY);
        log.debug("the maxX is ={}", maxX);
        log.debug("the maxY is ={}", maxY);

        return createPositionCreamPie(minX, minY, maxX, maxY);
    }

    public Position createPosition(final double minX, final double minY, final double maxX, final double maxY) {
        final Dimension2D dimension2D = getDimension2D();
        if ((maxX >0 && maxY >0)){
            double positionX = randomGenerator.nextDouble(((maxX - minX + 1) + minX));
            double positionY = randomGenerator.nextDouble(((maxY - minY + 1) + minY));
            log.debug("the posX is ={}", positionX);
            log.debug("the posY is ={}", positionY);
            return new Position(positionX, positionY);
        } else {
            return new Position(minX, minY);
        }
    }

    public Position createPositionCreamPie(final double minX, final double minY, final double maxX, final double maxY) {
        final Dimension2D dimension2D = getDimension2D();
        int decalage =50;
        if ((maxX <= dimension2D.getWidth() - decalage *1.6 && maxY <= dimension2D.getHeight() - decalage *1.6) && (minX >= decalage *1.6 && minY >= decalage *1.6)) {
      //  if ((maxX >0 && maxY >0)){
        double positionX = randomGenerator.nextDouble(((maxX - minX + 1) + minX));
            double positionY = randomGenerator.nextDouble(((maxY - minY + 1) + minY));
            log.debug("the posX is ={}", positionX);
            log.debug("the posY is ={}", positionY);
            if (positionX < minX) {
                positionX = minX;
            }
            if (positionY < minY) {
                positionY = minY;
            }
            return new Position(positionX, positionY);
        } else {
            return new Position(minX, minY);
        }
    }

}

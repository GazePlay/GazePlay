package net.gazeplay.components;

import javafx.geometry.Dimension2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;


/**
 * Draws random positions in the game area, with the random generator of the game (for the replay).
 */
@Slf4j
public abstract class RandomPositionGenerator {
    @Setter
    private ReplayablePseudoRandom randomGenerator = new ReplayablePseudoRandom();

    public RandomPositionGenerator(ReplayablePseudoRandom randomGenerator) { this.randomGenerator = randomGenerator;
    }

    /**
     * @return the size of the area where the positions are drawn
     */
    public abstract Dimension2D getDimension2D();

    /**
     * @return a random position at which an element of this radius stays inside the area
     */
    public Position newRandomPosition(final double radius) {

        final Dimension2D dimension2D = getDimension2D();

        final double maxX = dimension2D.getWidth() - radius;
        final double maxY = dimension2D.getHeight() - radius;

        return createPosition(radius, radius, maxX, maxY);
    }

    /**
     * @return a random position in a part of the area, given as ratios of its width (left, right) and height (top as
     *   "bottom", bottom as "top"), so that an element of this radius stays in it
     */
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
    /**
     * Same as {@link #newRandomBoundedPosition}, with the margins of the "Cream Pie" game. The positions used to be
     * drawn with {@link java.util.Random}, so that a replay of "Cream Pie" did not show the same positions: they are now
     * drawn with the generator of the game, like the other positions.
     */
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

    /**
     * @return a position drawn between 0 and the maxima, then raised to the minima
     */
    public Position createPosition(final double minX, final double minY, final double maxX, final double maxY) {
        if (maxX > 0 && maxY > 0) {
            double positionX = randomGenerator.nextDouble(maxX);
            double positionY = randomGenerator.nextDouble(maxY);
            if(positionX<minX){
                positionX = minX;
            }
            if(positionY<minY){
                positionY = minY;
            }
            log.debug("the posX is ={}", positionX);
            log.debug("the posY is ={}", positionY);
            return new Position(positionX, positionY);
        } else {
            return new Position(minX, minY);
        }
    }
    /**
     * @return a position drawn between the minima and the minima plus the maxima, then lowered to the maxima
     */
    public Position createPositionCreamPie(final double minX, final double minY, final double maxX, final double maxY) {

        if (maxX > 0 && maxY > 0) {
            double positionX = randomGenerator.nextDouble() * maxX + minX;
            double positionY = randomGenerator.nextDouble() * maxY + minY;
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

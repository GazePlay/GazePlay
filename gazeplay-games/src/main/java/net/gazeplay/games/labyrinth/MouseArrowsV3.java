package net.gazeplay.games.labyrinth;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

/*
 * MouseArrowsV3 :
 * Arrows are around the labyrinth.
 */

public class MouseArrowsV3 extends MouseArrows {

    public MouseArrowsV3(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext,
                         final Stats stats, final Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

    }

    @Override
    protected void recomputeArrowsPositions() {
    }

    @Override
    protected void placementFleche() {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double delta = dimension2D.getHeight() / 15;

        double x = gameInstance.entiereRecX + gameInstance.entiereRecWidth / 2 - delta;
        double y = gameInstance.entiereRecY - 2 * delta;

        this.buttonUp = new Rectangle(x, y, buttonDimWidth, buttonDimHeight);
        this.buttonUp.setFill(new ImagePattern(new Image("data/labyrinth/images/upArrow.png"), 5, 5, 1, 1, true));
        this.indicatorUp = createProgressIndicator(x, y, buttonDimWidth, buttonDimHeight);

        y = gameInstance.entiereRecY + gameInstance.entiereRecHeight;
        this.buttonDown = new Rectangle(x, y, buttonDimWidth, buttonDimHeight);
        this.buttonDown.setFill(new ImagePattern(new Image("data/labyrinth/images/downArrow.png"), 5, 5, 1, 1, true));
        this.indicatorDown = createProgressIndicator(x, y, buttonDimWidth, buttonDimHeight);

        y = gameInstance.entiereRecY + gameInstance.entiereRecHeight / 2 - delta;
        x = gameInstance.entiereRecX - 2.5 * delta;
        this.buttonLeft = new Rectangle(x, y, buttonDimHeight, buttonDimWidth);
        this.buttonLeft.setFill(new ImagePattern(new Image("data/labyrinth/images/leftArrow.png"), 5, 5, 1, 1, true));
        this.indicatorLeft = createProgressIndicator(x, y, buttonDimWidth, buttonDimHeight);

        x = gameInstance.entiereRecX + gameInstance.entiereRecWidth + 0.5 * delta;
        this.buttonRight = new Rectangle(x, y, buttonDimHeight, buttonDimWidth);
        this.buttonRight.setFill(new ImagePattern(new Image("data/labyrinth/images/rightArrow.png"), 5, 5, 1, 1, true));
        this.indicatorRight = createProgressIndicator(x, y, buttonDimWidth, buttonDimHeight);

    }

}

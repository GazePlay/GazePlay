package net.gazeplay.games.labyrinth;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

/*
 * MouseArrowsV2 :
 * Arrows are at around the mouse
 * They move with the mouse.
 */

public class MouseArrowsV2 extends MouseArrows {

    public MouseArrowsV2(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext,
                         final Stats stats, final Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

    }

    @Override
    protected void placementFleche() {

        buttonDimHeight = buttonDimHeight / 1.5;
        buttonDimWidth = buttonDimWidth / 2.5;

        final double x = gameInstance.positionX(indiceX) + gameInstance.adjustmentCaseWidth;
        final double y = gameInstance.positionY(indiceY) + gameInstance.adjustmentCaseHeight;

        final double dx = gameInstance.caseWidth;
        final double dy = gameInstance.caseHeight;
        final double dx2 = dx * 0.8;
        final double dy2 = dy * 0.8;

        this.buttonUp = new Rectangle(x, y - 1.2 * dy, buttonDimWidth, buttonDimHeight);
        this.buttonUp.setFill(new ImagePattern(new Image("data/labyrinth/images/upArrow.png"), 5, 5, 1, 1, true));
        this.indicatorUp = createProgressIndicator(x, y - 1.2 * dy, buttonDimWidth, buttonDimHeight);

        this.buttonDown = new Rectangle(x, y + dy2, buttonDimWidth, buttonDimHeight);
        this.buttonDown.setFill(new ImagePattern(new Image("data/labyrinth/images/downArrow.png"), 5, 5, 1, 1, true));
        this.indicatorDown = createProgressIndicator(x, y + dy2, buttonDimWidth, buttonDimHeight);

        this.buttonLeft = new Rectangle(x - 1.25 * dx, y, buttonDimHeight, buttonDimWidth);
        this.buttonLeft.setFill(new ImagePattern(new Image("data/labyrinth/images/leftArrow.png"), 5, 5, 1, 1, true));
        this.indicatorLeft = createProgressIndicator(x - 1.25 * dx, y, buttonDimWidth, buttonDimHeight);

        this.buttonRight = new Rectangle(x + dx2, y, buttonDimHeight, buttonDimWidth);
        this.buttonRight.setFill(new ImagePattern(new Image("data/labyrinth/images/rightArrow.png"), 5, 5, 1, 1, true));
        this.indicatorRight = createProgressIndicator(x + dx2, y, buttonDimWidth, buttonDimHeight);
    }

    @Override
    protected void recomputeArrowsPositions() {

        final double x = gameInstance.positionX(indiceX) + gameInstance.adjustmentCaseWidth;
        final double y = gameInstance.positionY(indiceY) + gameInstance.adjustmentCaseHeight;
        final double dx = gameInstance.caseWidth;
        final double dy = gameInstance.caseHeight;
        final double dx2 = dx * 0.8;
        final double dy2 = dy * 0.8;

        this.buttonUp.setX(x);
        this.buttonUp.setY(y - 1.2 * dy);
        this.indicatorUp.setTranslateX(x);
        this.indicatorUp.setTranslateY(y - 1.2 * dy);

        this.buttonDown.setX(x);
        this.buttonDown.setY(y + dy2);
        this.indicatorDown.setTranslateX(x);
        this.indicatorDown.setTranslateY(y + dy2);

        this.buttonLeft.setX(x - 1.25 * dx);
        this.buttonLeft.setY(y);
        this.indicatorLeft.setTranslateX(x - 1.25 * dx);
        this.indicatorLeft.setTranslateY(y);

        this.buttonRight.setX(x + dx2);
        this.buttonRight.setY(y);
        this.indicatorRight.setTranslateX(x + dx2);
        this.indicatorRight.setTranslateY(y);

    }

}

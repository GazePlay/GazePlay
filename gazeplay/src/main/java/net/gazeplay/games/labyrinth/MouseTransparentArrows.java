package net.gazeplay.games.labyrinth;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.stats.Stats;

/*
 * MouseTransparentArrows : 
 * Arrows are around the mouse but there are transparent.
 * This version is the same as MouseV0 for the player 
 */

public class MouseTransparentArrows extends MouseArrows {

    public MouseTransparentArrows(double positionX, double positionY, double width, double height,
            GameContext gameContext, Stats stats, Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

    }

    @Override
    protected void updateArrowsColor() {
        return;
    }

    @Override
    protected void placementFleche() {

        buttonDimHeight = gameInstance.caseHeight;
        buttonDimWidth = gameInstance.caseWidth;

        double x = gameInstance.positionX(indiceX);
        double y = gameInstance.positionY(indiceY);

        double dx = gameInstance.caseWidth;
        double dy = gameInstance.caseHeight;

        this.buttonUp = new Rectangle(x, y - dy, buttonDimWidth, buttonDimHeight);
        this.buttonUp
                .setFill(new ImagePattern(new Image("data/labyrinth/images/TransparentArrow.png"), 5, 5, 1, 1, true));
        this.indicatorUp = createProgressIndicator(x, y - dy, buttonDimWidth, buttonDimHeight);

        this.buttonDown = new Rectangle(x, y + dy, buttonDimWidth, buttonDimHeight);
        this.buttonDown
                .setFill(new ImagePattern(new Image("data/labyrinth/images/TransparentArrow.png"), 5, 5, 1, 1, true));
        this.indicatorDown = createProgressIndicator(x, y + dy, buttonDimWidth, buttonDimHeight);

        this.buttonLeft = new Rectangle(x - dx, y, buttonDimHeight, buttonDimWidth);
        this.buttonLeft
                .setFill(new ImagePattern(new Image("data/labyrinth/images/TransparentArrow.png"), 5, 5, 1, 1, true));
        this.indicatorLeft = createProgressIndicator(x - dx, y, buttonDimWidth, buttonDimHeight);

        this.buttonRight = new Rectangle(x + dx, y, buttonDimHeight, buttonDimWidth);
        this.buttonRight
                .setFill(new ImagePattern(new Image("data/labyrinth/images/TransparentArrow.png"), 5, 5, 1, 1, true));
        this.indicatorRight = createProgressIndicator(x + dx, y, buttonDimWidth, buttonDimHeight);
    }

    @Override
    protected void recomputeArrowsPositions() {

        double x = gameInstance.positionX(indiceX);
        double y = gameInstance.positionY(indiceY);
        double dx = gameInstance.caseWidth;
        double dy = gameInstance.caseHeight;

        this.buttonUp.setX(x);
        this.buttonUp.setY(y - dy);
        this.indicatorUp.setTranslateX(x);
        this.indicatorUp.setTranslateY(y - dy);

        this.buttonDown.setX(x);
        this.buttonDown.setY(y + dy);
        this.indicatorDown.setTranslateX(x);
        this.indicatorDown.setTranslateY(y + dy);

        this.buttonLeft.setX(x - dx);
        this.buttonLeft.setY(y);
        this.indicatorLeft.setTranslateX(x - dx);
        this.indicatorLeft.setTranslateY(y);

        this.buttonRight.setX(x + dx);
        this.buttonRight.setY(y);
        this.indicatorRight.setTranslateX(x + dx);
        this.indicatorRight.setTranslateY(y);

    }

    @Override
    protected ProgressIndicator createProgressIndicator(double x, double y, double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(x);
        indicator.setTranslateY(y);
        indicator.setMouseTransparent(true);
        indicator.setMinWidth(width);
        indicator.setMinHeight(height);
        indicator.setOpacity(0);
        return indicator;
    }
}
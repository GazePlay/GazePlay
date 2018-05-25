package net.gazeplay.games.labyrinth;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

public class MouseArrowsV3 extends MouseArrows {

    public MouseArrowsV3(double positionX, double positionY, double width, double height, GameContext gameContext,
            Stats stats, Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

    }

    @Override
    protected void placementFleche() {

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double delta = dimension2D.getHeight() / 20;
        double x = gameInstance.entiereRecX + gameInstance.entiereRecWidth / 2;
        double y = gameInstance.entiereRecY - delta;

        this.buttonUp = new Rectangle(x, y, buttonDimWidth, buttonDimHeight);
        this.buttonUp.setFill(new ImagePattern(new Image("data/labyrinth/images/upArrow.png"), 5, 5, 1, 1, true));
        this.indicatorUp = createProgressIndicator(x, y, buttonDimWidth, buttonDimHeight);
        this.buttonUp.addEventHandler(MouseEvent.ANY, buttonUpEvent);
        this.buttonUp.addEventHandler(GazeEvent.ANY, buttonUpEvent);

        y = gameInstance.entiereRecY + gameInstance.entiereRecHeight + delta;
        this.buttonDown = new Rectangle(x, y, buttonDimWidth, buttonDimHeight);
        this.buttonDown.setFill(new ImagePattern(new Image("data/labyrinth/images/downArrow.png"), 5, 5, 1, 1, true));
        this.indicatorDown = createProgressIndicator(x, y, buttonDimWidth, buttonDimHeight);
        this.buttonDown.addEventHandler(MouseEvent.ANY, buttonDownEvent);
        this.buttonDown.addEventHandler(GazeEvent.ANY, buttonDownEvent);

        y = gameInstance.entiereRecY + gameInstance.entiereRecHeight / 2;
        x = gameInstance.entiereRecX - delta;
        this.buttonLeft = new Rectangle(x, y, buttonDimHeight, buttonDimWidth);
        this.buttonLeft.setFill(new ImagePattern(new Image("data/labyrinth/images/leftArrow.png"), 5, 5, 1, 1, true));
        this.indicatorLeft = createProgressIndicator(x, y, buttonDimHeight, buttonDimWidth);
        this.buttonLeft.addEventHandler(MouseEvent.ANY, buttonLeftEvent);
        this.buttonLeft.addEventHandler(GazeEvent.ANY, buttonLeftEvent);

        x = gameInstance.entiereRecX + gameInstance.entiereRecWidth + delta;
        this.buttonRight = new Rectangle(x, y, buttonDimHeight, buttonDimWidth);
        this.buttonRight.setFill(new ImagePattern(new Image("data/labyrinth/images/rightArrow.png"), 5, 5, 1, 1, true));
        this.indicatorRight = createProgressIndicator(x, y, buttonDimHeight, buttonDimWidth);
        this.buttonRight.addEventHandler(MouseEvent.ANY, buttonRightEvent);
        this.buttonRight.addEventHandler(GazeEvent.ANY, buttonRightEvent);

        this.getChildren().addAll(buttonUp, buttonDown, buttonLeft, buttonRight);
        this.getChildren().addAll(indicatorUp, indicatorDown, indicatorLeft, indicatorRight);
    }

    @Override
    protected void recomputeArrowsPositions() {
    }

}

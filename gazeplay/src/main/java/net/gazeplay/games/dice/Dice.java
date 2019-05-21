package net.gazeplay.games.dice;

import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.DiceRoll;
import net.gazeplay.commons.utils.stats.Stats;



public class Dice implements GameLifeCycle {

    private GameContext gameContext;
    private Stats stats;
    private DiceRoll diceRoll;
    private Dimension2D dimensions;

    public Dice(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        diceRoll = new DiceRoll(gameContext, 4);
        dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();

        diceRoll.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> diceRoll.roll());
    }

    @Override
    public void launch() {
        gameContext.getChildren().add(diceRoll);
    }

    @Override
    public void dispose() {

    }
}

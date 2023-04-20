package net.gazeplay.games.cooperativeGame;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


public abstract class Cat extends Parent {


    protected final Rectangle cat;
    protected final IGameContext gameContext;
    protected final CooperativeGame gameInstance;
    double positionX;
    double positionY;

    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance){
        this.cat = new Rectangle(positionX, positionY, width, height);
        this.cat.setFill(Color.WHITE);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.positionX = positionX;
        this.positionY = positionY;
    }


}

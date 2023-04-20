package net.gazeplay.games.cooperativeGame;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;


public abstract class Cat extends Parent {


    protected final Rectangle cat;
    protected final IGameContext gameContext;
    protected final CooperativeGame gameInstance;
    protected float speed;
    protected final ArrayList<Rectangle> obstacles;


    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance, float speed, ArrayList<Rectangle> obstacles){
        this.cat = new Rectangle(positionX, positionY, width, height);
        this.cat.setFill(Color.BLACK);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.obstacles = obstacles;
    }


}

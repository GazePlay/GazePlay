package net.gazeplay.games.cooperativeGame;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


public abstract class Cat extends Parent {


    protected final Rectangle hitbox;
    protected final IGameContext gameContext;
    protected final CooperativeGame gameInstance;
    protected double speed;
    protected boolean isACat;
    protected Rectangle target;



    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance, double speed, boolean isACat){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.hitbox.setFill(Color.BLACK);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.isACat = isACat;
    }

    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance, double speed, boolean isACat, Rectangle target){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.hitbox.setFill(Color.YELLOW);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.isACat = isACat;
        this.target = target;
    }


}

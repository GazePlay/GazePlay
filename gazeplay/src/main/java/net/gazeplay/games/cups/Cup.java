package net.gazeplay.games.cups;

import java.awt.Point;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import net.gazeplay.GameContext;

public class Cup extends Parent {

    @Getter
    private final Rectangle item;
    private boolean hasBall;
    
    private final GameContext gameContext;
    
    @Getter
    private PositionCup position;
    @Getter
    private double width;
    @Getter
    private double height;
    
    public Cup (Rectangle item, PositionCup position, GameContext gameContext){
        this.item = item;
        this.position = position;
        this.width = position.getImageWidth();
        this.height = position.getImageHeight();
        this.gameContext = gameContext;
    }
    
    public boolean containsBall(){
        return hasBall;
    }
    
    public void setBall(boolean hasBall){
        this.hasBall = hasBall;
    }
    
    public void updatePosition(int newCellX, int newCellY){
        Point newPos = position.calculateXY(newCellX, newCellY);
    }
}

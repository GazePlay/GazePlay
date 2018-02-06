package net.gazeplay.games.cups;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameContext;

public class Ball {

    @Getter
    @Setter
    private double radius;

    @Getter
    @Setter
    private Color color;
    @Getter
    @Setter
    private Cup theCup;
    @Getter
    private Circle item;
    
    @Getter
    private double XPos;
    @Getter
    private double YPos;
    
    private final GameContext gameContext;

    public Ball(double radius, Color color, GameContext gameContext) {
        this.radius = radius;
        this.color = color;
        item = new Circle(radius);
        item.setFill(color);
        this.gameContext = gameContext;
    }
    
    public Ball(double radius, Color color, Cup theCup, GameContext gameContext){
        this(radius, color, gameContext);
        this.theCup = theCup;
        this.XPos = theCup.getPosition().getCellX() + (theCup.getWidth() - 2*radius)/2;
        this.YPos = theCup.getPosition().getCellY() + (theCup.getHeight() - 2*radius)/2 + (theCup.getHeight() - 2*radius)/4;
    }
    
    public void updatePosition(double XPos, double YPos){
        int index = gameContext.getChildren().indexOf(this);
        this.XPos = XPos;
        this.YPos = YPos;

        gameContext.getChildren().add(item);
    }
    
}

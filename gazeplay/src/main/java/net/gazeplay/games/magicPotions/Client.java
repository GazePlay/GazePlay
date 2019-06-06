package net.gazeplay.games.magicPotions;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import net.gazeplay.GameContext;

import java.util.LinkedList;


public class Client extends Parent {

private final GameContext gameContext;

private final double initWidth;
private final double initHeight;

private final double initX;
private final double initY;

private final Rectangle client ;
//private final Image clientImg;

private PotionMix potionRequest;
@Getter
private LinkedList<Color> colorsToMix;

    public enum PotionMix{
        RED_Potion("Red", Color.RED),YELLOW_Potion("Yellow", Color.YELLOW),BLUE_Potion("Blue",Color.BLUE),
        ORANGE_Potion("Orange",Color.ORANGE), GREEN_Potion("Green",Color.GREEN),PURPLE_Potion("Purple",Color.PURPLE),
        BLACK_Potion("Black",Color.BLACK);

        @Getter
        private final String colorName;
        private final Color color;

        PotionMix(String name , Color color){
            this.colorName = name;
            this.color = color;
        }
    }

    public Client(double posX, double posY, double width, double height,
                  GameContext gameContext, Image clientImage , PotionMix request){
        this.gameContext = gameContext;

        this.client = new Rectangle(posX, posY, width, height);
        this.client.setFill(new ImagePattern(clientImage,0,0,1,1,true));
        this.potionRequest = request;
        this.client.setFill(request.color);
        this.colorsToMix = toMix(request);

        this.initX = posX;
        this.initY = posY;
        this.initWidth = width;
        this.initHeight = height;

    }
public LinkedList<Color> toMix(PotionMix potionRequest){
        LinkedList<Color> colorsToMix = new LinkedList<Color>();
    if(potionRequest.color == Color.RED || potionRequest.color == Color.YELLOW ||  potionRequest.color == Color.BLUE){
        colorsToMix.add(potionRequest.color);
    }
    else{
        switch (potionRequest){
            case ORANGE_Potion:
                colorsToMix.clear();
                colorsToMix.add(Color.RED);
                colorsToMix.add(Color.YELLOW);
                break;
            case PURPLE_Potion:
                colorsToMix.clear();
                colorsToMix.add(Color.RED);
                colorsToMix.add(Color.BLUE);
                break;
            case GREEN_Potion:
                colorsToMix.clear();
                colorsToMix.add(Color.YELLOW);
                colorsToMix.add(Color.BLUE);
                break;
            case BLACK_Potion:
                colorsToMix.clear();
                colorsToMix.add(Color.RED);
                colorsToMix.add(Color.YELLOW);
                colorsToMix.add(Color.BLUE);
                break;
        }
    }
    return colorsToMix;
}

}

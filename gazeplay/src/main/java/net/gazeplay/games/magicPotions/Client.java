package net.gazeplay.games.magicPotions;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Johana MARKU
 *
 */

public class Client extends Rectangle {

    @Getter
    private final Rectangle client;
    @Getter
    private PotionMix potionRequest;
    @Getter
    private LinkedList<Color> colorsToMix;

    public enum PotionMix {
        RED_Potion("Red", Color.RED), YELLOW_Potion("Yellow", Color.YELLOW), BLUE_Potion("Blue",
                Color.BLUE), ORANGE_Potion("Orange", Color.ORANGE), GREEN_Potion("Green",
                        Color.GREEN), PURPLE_Potion("Purple", Color.PURPLE), BLACK_Potion("Black", Color.BLACK);

        @Getter
        private final String colorName;
        @Getter
        private final Color color;

        PotionMix(String name, Color color) {
            this.colorName = name;
            this.color = color;
        }

        public static PotionMix getRandomPotionRequest() {
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        }
    }

    public Client(double posX, double posY, double width, double height, Image clientImage, PotionMix request) {

        this.client = new Rectangle(posX, posY, width, height);
        this.client.setFill(new ImagePattern(clientImage, 0, 0, 1, 1, true));
        this.potionRequest = request;
        this.colorsToMix = toMix(request);
    }

    public LinkedList<Color> toMix(PotionMix potionRequest) {
        LinkedList<Color> colorsToMix = new LinkedList<Color>();
        if (potionRequest.color == Color.RED || potionRequest.color == Color.YELLOW
                || potionRequest.color == Color.BLUE) {
            colorsToMix.add(potionRequest.color);
        } else {
            switch (potionRequest) {
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

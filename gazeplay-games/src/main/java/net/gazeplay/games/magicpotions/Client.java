package net.gazeplay.games.magicpotions;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Johana MARKU
 */
class Client extends Rectangle {

    @Getter
    private final Rectangle client;

    @Getter
    private final PotionMix potionRequest;

    @Getter
    private final List<Color> colorsToMix;

    Client(final double posX, final double posY, final double width, final double height, final Image clientImage, final PotionMix request) {
        this.client = new Rectangle(posX, posY, width, height);
        this.client.setFill(new ImagePattern(clientImage, 0, 0, 1, 1, true));
        this.potionRequest = request;
        this.colorsToMix = toMix(request);
    }

    private List<Color> toMix(final PotionMix potionRequest) {
        final List<Color> colorsToMix = new LinkedList<>();
        if (potionRequest.getColor() == Color.RED || potionRequest.getColor() == Color.YELLOW
            || potionRequest.getColor() == Color.BLUE) {
            colorsToMix.add(potionRequest.getColor());
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
                default:
                    throw new IllegalArgumentException("value : " + potionRequest);
            }
        }
        return colorsToMix;
    }

}

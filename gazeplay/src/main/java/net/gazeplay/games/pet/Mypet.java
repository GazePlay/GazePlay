package net.gazeplay.games.pet;

import javafx.event.EventType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;

@Slf4j
public class Mypet extends StackPane {

    public static final int DRYER_UP = 0;
    public static final int DRYER_RIGHT = 1;
    public static final int DRYER_LEFT = 2;

    @Getter
    @Setter
    private ImageView wings;

    @Getter
    @Setter
    private ImageView body;

    @Getter
    @Setter
    private ImageView mouth;

    @Getter
    @Setter
    private ImageView eyes;

    public Mypet() {
        setBasic();
        this.getChildren().addAll(wings, body, mouth, eyes);
    }

    public void setBasic() {
        setWings((new ImageView(new Image("basicWings.png"))));
        setBody((new ImageView(new Image("basicBody.png"))));
        setMouth((new ImageView(new Image("basicMouth.png"))));
        setEyes((new ImageView(new Image("basicEyes.png"))));
    }

    public void setHappy() {

    }

    public void setSad() {

    }

    public void setAngry() {

    }

    public void setDisturbed() {

    }

    public void setDirty() {

    }

    public void setHungry() {

    }

    public void setTired() {

    }

    public void setSleepy() {

    }

    public void setMovingWings(Boolean isMoving) {
        if (isMoving) {

        } else {

        }
    }

    public void setDryer(int i) {
        if (i == DRYER_LEFT) {

        } else if (i == DRYER_RIGHT) {

        } else { // if (i == DRYER_UP)

        }
    }
}

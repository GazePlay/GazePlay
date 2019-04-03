package net.gazeplay.games.biboulejump;

import javafx.scene.shape.Rectangle;
import lombok.Getter;

public class Platform extends Rectangle {
    @Getter
    private String soundFileLocation;
    @Getter
    private int bounceFactor;
    protected Rectangle collider;

    public Platform(double x, double y, double width, double height, String soundFileLocation, int bounceFactor) {
        this(x, y, width, height, soundFileLocation, bounceFactor, 0, 0, 0, 0);
    }

    public Platform(double x, double y, double width, double height, String soundFileLocation, int bounceFactor, double colliderMarginUp, double colliderMarginRight, double colliderMarginDown, double colliderMarginLeft) {
        super(x, y, width, height);
        this.soundFileLocation = soundFileLocation;
        this.bounceFactor = bounceFactor;
        this.collider = new Rectangle(x + colliderMarginLeft*width, y + colliderMarginUp*height, width * (1 - colliderMarginLeft - colliderMarginRight), height * (1 - colliderMarginUp - colliderMarginDown));
    }

    public void scroll(double difference){
        setY(getY() + difference);
        collider.setY(collider.getY() + difference);
    }

    public boolean isColliding(Rectangle rect){
        return collider.getX() < rect.getX() + rect.getWidth() && collider.getX() + collider.getWidth() > rect.getX()
                && collider.getY() < rect.getY() + rect.getHeight() && collider.getY() + collider.getHeight() > rect.getY();
    }
}

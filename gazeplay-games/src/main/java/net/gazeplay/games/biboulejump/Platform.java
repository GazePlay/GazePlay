package net.gazeplay.games.biboulejump;

import javafx.scene.shape.Rectangle;
import lombok.Getter;

public class Platform extends Rectangle {

    @Getter
    private final String soundFileLocation;

    @Getter
    private final int bounceFactor;

    protected final Rectangle collider;

    public Platform(final double x, final double y, final double width, final double height, final String soundFileLocation, final int bounceFactor) {
        this(x, y, width, height, soundFileLocation, bounceFactor, 0, 0, 0, 0);
    }

    public Platform(
        final double x, final double y,
        final double width, final double height,
        final String soundFileLocation,
        final int bounceFactor,
        final double colliderMarginUp, final double colliderMarginRight, final double colliderMarginDown, final double colliderMarginLeft
    ) {
        super(x, y, width, height);
        this.soundFileLocation = soundFileLocation;
        this.bounceFactor = bounceFactor;
        this.collider = new Rectangle(x + colliderMarginLeft * width, y + colliderMarginUp * height,
            width * (1 - colliderMarginLeft - colliderMarginRight),
            height * (1 - colliderMarginUp - colliderMarginDown));
    }

    public void scroll(final double difference) {
        setY(getY() + difference);
        collider.setY(collider.getY() + difference);
    }

    public boolean isColliding(final Rectangle rect) {
        return collider.getX() < rect.getX() + rect.getWidth() && collider.getX() + collider.getWidth() > rect.getX()
            && collider.getY() < rect.getY() + rect.getHeight()
            && collider.getY() + collider.getHeight() > rect.getY();
    }

}

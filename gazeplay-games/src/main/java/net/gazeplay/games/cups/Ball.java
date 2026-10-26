package net.gazeplay.games.cups;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The ball hidden under a cup; it moves with its cup.
 */
@Slf4j
public final class Ball {

    @Getter
    @Setter
    private double radius;

    @Getter
    @Setter
    private Color color;
    @Setter
    private Cup theCup;
    @Getter
    private final Circle item;

    @Getter
    private double XCenterPos;
    @Getter
    private double YCenterPos;

    public Ball(double radius, Color color, Cup theCup) {
        this.radius = radius;
        this.color = color;
        item = new Circle(radius);
        item.setFill(color);
        this.theCup = theCup;
        updatePosition(theCup.getItem().getX(), theCup.getItem().getY());
    }

    /**
     * Places the ball at the bottom of its cup, at the new position of the cup.
     */
    public void updatePosition(double newXPosCup, double newYPosCup) {
        this.XCenterPos = newXPosCup + theCup.getItem().getFitWidth() / 2;
        this.YCenterPos = newYPosCup + theCup.getItem().getFitHeight() - 2 * radius;
        item.setCenterX(XCenterPos);
        item.setCenterY(YCenterPos);
    }

}

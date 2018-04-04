package net.gazeplay.games.room;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class Wall {

    // The size of the X of the cube(this is the length on the X axis)
    private static double xLength;
    // The size of the Y of the cube(this is the length on the Y axis)
    private static double yLength;

    // The short size of a side of the cube (this is the thickness of each side)
    private static final double thickness = 0.1;

    // The positions of the sides compared to the origin
    private static double positionWall;

    // Image rightImage = new Image("data/room/front.jpg", WIDTH, HEIGHT, true, true);

    Box box;
    String name;

    public Wall(String moveDirection, int positiveAxisMultiplier, String name, double xLength, double yLength) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.name = name;
        switch (moveDirection) {
        case "X":
            this.box = new Box(thickness, yLength, xLength);
            this.positionWall = xLength / 2;
            // this.box.setMaterial(new PhongMaterial(Color.TRANSPARENT, rightImage, rightImage, rightImage,
            // rightImage));
            this.box.setMaterial(new PhongMaterial(Color.RED));
            this.box.setTranslateX(positiveAxisMultiplier * positionWall);
            break;
        case "Y":
            this.box = new Box(xLength, thickness, xLength);
            this.positionWall = yLength / 2;
            // this.box.setMaterial(new PhongMaterial(Color.TRANSPARENT, rightImage, rightImage, rightImage,
            // rightImage));
            this.box.setMaterial(new PhongMaterial(Color.GREEN));
            this.box.setTranslateY(positiveAxisMultiplier * positionWall);
            break;
        case "Z":
            this.box = new Box(xLength, yLength, thickness);
            this.positionWall = xLength / 2;
            if (positiveAxisMultiplier == 1) {
                Image frontImage = new Image("data/room/front.jpg");
                this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, frontImage, frontImage, frontImage, frontImage));
            } else {
                this.box.setMaterial(new PhongMaterial(Color.BLUE));
            }
            this.box.setTranslateZ(positiveAxisMultiplier * positionWall);
            break;
        }

    }

    public Box getItem() {
        return box;
    }
}

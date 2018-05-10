package net.gazeplay.games.room;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Wall {

    // The short size of a side of the cube (this is the thickness of each side)
    private static final double thickness = 0.1;

    // The positions of the sides compared to the origin
    private double positionWall;

    // Image rightImage = new Image("data/room/front.jpg", WIDTH, HEIGHT, true, true);
    Box box;
    String name;

    public Wall(String moveDirection, int positiveAxisMultiplier, String name, double xLength, double yLength) {
        this.name = name;
        switch (moveDirection) {
        case "X":
            this.box = new Box(thickness, yLength, xLength);
            this.positionWall = xLength / 2;
            // this.box.setMaterial(new PhongMaterial(Color.TRANSPARENT, rightImage, rightImage, rightImage,
            // rightImage));
            if (positiveAxisMultiplier == 1) {
                Image rightImage = new Image("data/room/right.jpg");
                this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, rightImage, rightImage, rightImage, rightImage));
            } else {
                Image leftImage = new Image("data/room/left.jpg");
                this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, leftImage, leftImage, leftImage, leftImage));
            }
            this.box.setTranslateX(positiveAxisMultiplier * positionWall);
            break;
        case "Y":
            this.box = new Box(xLength, thickness, xLength);
            this.positionWall = yLength / 2;
            Image top_bottomImage = new Image("data/room/top_bottom.jpg");
            this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, top_bottomImage, top_bottomImage, top_bottomImage, top_bottomImage));
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
                Image backImage = new Image("data/room/back.jpg");
                this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, backImage, backImage, backImage, backImage));
            }
            this.box.setTranslateZ(positiveAxisMultiplier * positionWall);
            break;
        }
    }

    public Box getItem() {
        return box;
    }
}

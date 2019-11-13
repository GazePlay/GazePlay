package net.gazeplay.games.room;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Wall {

    Box box;
    String name;

    public Wall(MoveDirection moveDirection, int positiveAxisMultiplier, String name, double xLength, double yLength) {
        // The short size of a side of the cube (this is the thickness of each side)
        double wallThickness = 0.1;
        this.name = name;
        switch (moveDirection) {
        case X:
            this.box = new Box(wallThickness, yLength, xLength);
            // The positions of the sides compared to the origin
            double positionWall = xLength / 2;
            if (positiveAxisMultiplier == 1) {
                Image rightImage = new Image("data/room/right.jpg");
                this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, rightImage, rightImage, rightImage, rightImage));
            } else {
                Image leftImage = new Image("data/room/left.jpg");
                this.box.setMaterial(new PhongMaterial(Color.TRANSPARENT, leftImage, leftImage, leftImage, leftImage));
            }
            this.box.setTranslateX(positiveAxisMultiplier * positionWall);
            break;
        case Y:
            this.box = new Box(xLength, wallThickness, xLength);
            positionWall = yLength / 2;
            Image top_bottomImage = new Image("data/room/top_bottom.jpg");
            this.box.setMaterial(new PhongMaterial(Color.TRANSPARENT, top_bottomImage, top_bottomImage, top_bottomImage,
                    top_bottomImage));
            this.box.setTranslateY(positiveAxisMultiplier * positionWall);
            break;
        case Z:
            this.box = new Box(xLength, yLength, wallThickness);
            positionWall = xLength / 2;
            if (positiveAxisMultiplier == 1) {
                Image frontImage = new Image("data/room/front.jpg");
                this.box.setMaterial(
                        new PhongMaterial(Color.TRANSPARENT, frontImage, frontImage, frontImage, frontImage));
            } else {
                Image backImage = new Image("data/room/back.jpg");
                this.box.setMaterial(new PhongMaterial(Color.TRANSPARENT, backImage, backImage, backImage, backImage));
            }
            this.box.setTranslateZ(positiveAxisMultiplier * positionWall);
            break;
        }
    }

    public Box getItem() {
        return box;
    }
}

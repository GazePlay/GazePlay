package net.gazeplay.games.surviveAgainstRobots;


import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlayerEnum {

    RED(new ImagePattern(new Image("data/surviveAgainstRobots/player/Red.png"))),
    GREEN(new ImagePattern(new Image("data/surviveAgainstRobots/player/Green.png"))),
    ORANGE(new ImagePattern(new Image("data/surviveAgainstRobots/player/Orange.png"))),
    YELLOW(new ImagePattern(new Image("data/surviveAgainstRobots/player/Yellow.png")));

    @Getter
    private final ImagePattern image;
}

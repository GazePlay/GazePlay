package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BonusEnum {
    SLOW("Slow bonus", new ImagePattern(new Image("data/surviveAgainstRobots/slow.png")));
    //AUTOTARGET("Auto-target bonus", new ImagePattern(new Image("autotarget.png"))),
    //SPEED("Speed bonus", new ImagePattern(new Image("speed.png")));

    @Getter
    private final String name;

    @Getter
    private final ImagePattern imagePattern;


}

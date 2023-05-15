package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enum class representing the different types of bonuses in the Survive Against Robots game.
 */
@RequiredArgsConstructor
public enum BonusEnum {

    FIRERATE("FireRate bonus", new ImagePattern(new Image("data/surviveAgainstRobots/bonus/fire.png"))),
    SHIELD("Shield bonus", new ImagePattern(new Image("data/surviveAgainstRobots/bonus/shield.png"))),
    SLOW("Slow bonus", new ImagePattern(new Image("data/surviveAgainstRobots/bonus/slow.png")));
    //AUTOTARGET("Auto-target bonus", new ImagePattern(new Image("autotarget.png"))),
    //SPEED("Speed bonus", new ImagePattern(new Image("speed.png")));
    @Getter
    private final String name;

    @Getter
    private final ImagePattern imagePattern;


}

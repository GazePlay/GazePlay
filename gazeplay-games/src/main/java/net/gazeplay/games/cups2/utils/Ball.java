package net.gazeplay.games.cups2.utils;

import javafx.scene.shape.Circle;
import lombok.Getter;
import net.gazeplay.games.cups2.Config;

public class Ball extends Circle {

    @Getter
    Cup container;

    public Ball(Cup container) {
        super(Config.BALL_RADIUS, Config.BALL_COLOR);
        this.container = container;
        container.ball = this;
        setCenterX(container.getX() + container.getFitWidth() / 2);
        setCenterY(container.getY() + container.getFitHeight() * 0.8);
        setVisible(false);
    }

    public void dispose() {
    }

    public void update() {
        // Follows the same principle than Cup.update()
        setCenterX(Cup.computeX(container.getCurrentIndex()) + container.getFitWidth() / 2);
        setCenterY(container.getY() + container.getFitHeight() * 0.8);
        setTranslateX(0);
        setTranslateY(0);
    }
}

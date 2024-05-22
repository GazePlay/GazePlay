package net.gazeplay.games.cups2.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import lombok.Getter;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;

public class Cup extends ImageView {

    private final Callback<Void, Void> updateCallback;

    @Getter
    private int currentIndex;

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
        update();
    }

    @Getter
    Ball ball = null;

    public boolean hasBall() {
        return ball != null;
    }

    public Cup(int xIndex) {
        this.currentIndex = xIndex;
        this.updateCallback = unused -> {
            this.update();
            return null;
        };
        setImage(new Image(Config.CUP_IMAGE_PATH));
        setFitWidth(Config.CUP_WIDTH);
        setFitHeight(getImage().getHeight() * Config.CUP_WIDTH / getImage().getWidth());
        setX(computeX(xIndex));
        setY((CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getHeight() - getFitHeight()) / 2);
        Config.nbCupsSubscribe(updateCallback);
    }

    public void dispose() {
        Config.nbCupsUnsubscribe(updateCallback);
    }

    public void update() {
        setX(computeX(currentIndex));
        setTranslateX(0);
        if (hasBall())
            ball.update();
    }

    public static double computeX(int xIndex) {
        double width = CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getWidth();
        return Config.CUP_MARGIN + (xIndex + 0.5) * (width - 2 * Config.CUP_MARGIN) / Config.nbCups - Config.CUP_WIDTH / 2;
    }

    public static int indexDistance(Cup cupA, Cup cupB) {
        return Math.abs(cupA.getCurrentIndex() - cupB.getCurrentIndex());
    }

    public static void swapBall(Cup from, Cup to) {
        to.ball = from.ball;
        from.ball = null;
        to.ball.container = to;
    }
}

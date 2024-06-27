package net.gazeplay.games.cups2.action;

import javafx.animation.PathTransition;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.utils.Cup;

public interface Action {
    enum Type {
        EXCHANGE, FAKE_EXCHANGE, TRICK, FAKE_TRICK, CYCLE, FAKE_CYCLE, REVEAL, REVEAL_ALL
    }

    static Type getFakeTypeOf(Type type) {
        return switch (type) {
            case EXCHANGE -> Type.FAKE_EXCHANGE;
            case TRICK -> Type.FAKE_TRICK;
            case CYCLE -> Type.FAKE_CYCLE;
            default -> null;
        };
    }

    Type getType();

    double getDifficulty();

    int simulate(int ballIndex);

    boolean hasBall();

    void execute();

    void execute(Callback<Void, Void> onFinish);

    static PathTransition smoothArcTransition(double time, Cup fromCup, Cup toCup, boolean upOrDown) {
        // Create an arc transition from fromCup to toCup positions with some fancy calculations
        if (upOrDown)
            fromCup.toBack();
        else
            fromCup.toFront();
        double maxHeight = CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getHeight();
        double heightSide = 0.5 + Math.pow(Cup.indexDistance(fromCup, toCup), 0.8) * (Config.CUP_MARGIN / 200) * (upOrDown ? -1 : 1);
        return new PathTransition(
            Duration.millis(Math.pow(Cup.indexDistance(fromCup, toCup), 0.25) * time / Config.getSpeedFactor()),
            new Path(
                new MoveTo(fromCup.getX() + fromCup.getFitWidth() / 2, fromCup.getY() + fromCup.getFitHeight() / 2),
                new CubicCurveTo(
                    fromCup.getX() + fromCup.getFitWidth() / 2, Math.max(0, Math.min(fromCup.getY() + fromCup.getFitHeight() * heightSide, maxHeight)),
                    toCup.getX() + toCup.getFitWidth() / 2, Math.max(0, Math.min(toCup.getY() + toCup.getFitHeight() * heightSide, maxHeight)),
                    toCup.getX() + toCup.getFitWidth() / 2, toCup.getY() + toCup.getFitHeight() / 2
                )
            ),
            fromCup
        );
    }

    static Callback<Void, Void> joiner(Callback<Void, Void> onFinish, int nbCalls) {
        return onFinish == null ? null : new Callback<>() {
            private int count = 0;

            @Override
            public synchronized Void call(Void param) {
                if (++count == nbCalls)
                    onFinish.call(null);
                return null;
            }
        };
    }
}

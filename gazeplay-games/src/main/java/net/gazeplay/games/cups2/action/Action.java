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
        EXCHANGE, REVEAL_ALL, CYCLE, REVEAL, FAKE_CYCLE, TRICK, FAKE_TRICK, FAKE_EXCHANGE
    }

    Type getType();

    int simulate(int ballIndex);

    void execute();

    void execute(Callback<Void, Void> onFinish);

    static PathTransition smoothArcTransition(double time, Cup fromCup, Cup toCup, boolean upOrDown) {
        if (upOrDown)
            fromCup.toBack();
        else
            fromCup.toFront();
        double maxHeight = CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getHeight();
        double heightSide = 0.5 + Math.pow(Cup.indexDistance(fromCup, toCup), 0.8) * (Config.CUP_MARGIN / 100) * (upOrDown ? -1 : 1);
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

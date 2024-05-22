package net.gazeplay.games.cups2.action;

import javafx.animation.PathTransition;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.utils.Cup;

public interface Action {
    enum Type {
        EXCHANGE, REVEAL_ALL, CYCLE, REVEAL, FAKE_CYCLE, TRICK, FAKE_TRICK, FAKE_EXCHANGE
    }

    Type getType();

    int simulate(int ballIndex);

    void execute();

    void execute(Callback<Void, Void> onFinish);

    static PathTransition smoothArcTransition(Duration duration, Cup fromCup, Cup toCup, boolean upOrDown) {
        double heightSide = 0.7;
        if (Cup.indexDistance(fromCup, toCup) > 1)
            heightSide = Math.floor(Math.sqrt(Cup.indexDistance(fromCup, toCup)) * 10) / 10;
        heightSide = 0.5 + heightSide * (upOrDown ? 1 : -1);
        return new PathTransition(
            duration,
            new Path(
                new MoveTo(fromCup.getX() + fromCup.getFitWidth() / 2, fromCup.getY() + fromCup.getFitHeight() / 2),
                new CubicCurveTo(
                    fromCup.getX() + fromCup.getFitWidth() / 2, fromCup.getY() + fromCup.getFitHeight() * heightSide,
                    toCup.getX() + toCup.getFitWidth() / 2, toCup.getY() + toCup.getFitHeight() * heightSide,
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

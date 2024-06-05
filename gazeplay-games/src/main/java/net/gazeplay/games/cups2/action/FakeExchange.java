package net.gazeplay.games.cups2.action;

import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class FakeExchange implements Action {
    private final List<Cup> cups;
    private final boolean direction;
    private final int indexA, indexB;

    public FakeExchange(List<Cup> cups, int indexA, int indexB) {
        this.cups = cups;
        this.direction = indexA < indexB;
        this.indexA = Math.min(indexA, indexB);
        this.indexB = Math.max(indexA, indexB);
    }

    @Override
    public Type getType() {
        return Type.FAKE_EXCHANGE;
    }

    @Override
    public double getDifficulty() {
        return Config.ACTION_FAKE_EXCHANGE_DIFFICULTY;
    }

    @Override
    public int simulate(int ballIndex) {
        return ballIndex;
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        Cup cupA = cups.get(indexA);
        Cup cupB = cups.get(indexB);

        Callback<Void, Void> joinCallback = Action.joiner(onFinish, 2);
        PathTransition pta = Action.smoothArcTransition(Config.ACTION_FAKE_EXCHANGE_TIME, cupA, cupB, direction);
        PathTransition ptb = Action.smoothArcTransition(Config.ACTION_FAKE_EXCHANGE_TIME, cupB, cupA, !direction);
        pta.setOnFinished(e -> joinCallback.call(null));
        ptb.setOnFinished(e -> joinCallback.call(null));
        pta.play();
        ptb.play();

        new Timeline(new KeyFrame(
            Duration.millis(pta.getDuration().toMillis() * 2 / 3),
            e -> {
                pta.setRate(-1);
                ptb.setRate(-1);
            }
        )).play();
    }
}

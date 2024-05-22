package net.gazeplay.games.cups2.action;

import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
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
    public int simulate(int ballIndex) {
        return ballIndex;
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        double time = Config.ACTION_FAKE_EXCHANGE_TIME / (1.2 * Config.speedFactor);
        Cup cupA = cups.get(indexA);
        Cup cupB = cups.get(indexB);

        Callback<Void, Void> joinCallback = Action.joiner(onFinish, 2);
        PathTransition pta = Action.smoothArcTransition(Duration.millis(time), cupA, cupB, direction);
        PathTransition ptb = Action.smoothArcTransition(Duration.millis(time), cupB, cupA, !direction);
        pta.setCycleCount(2);
        ptb.setCycleCount(2);
        pta.setAutoReverse(true);
        ptb.setAutoReverse(true);
        pta.setOnFinished(e -> joinCallback.call(null));
        ptb.setOnFinished(e -> joinCallback.call(null));
        pta.play();
        ptb.play();

        PauseTransition faker = new PauseTransition(Duration.millis(time * 0.6));
        faker.setOnFinished(e -> {
            pta.jumpTo(Duration.millis(time * 1.4));
            ptb.jumpTo(Duration.millis(time * 1.4));
        });
        faker.play();
    }
}

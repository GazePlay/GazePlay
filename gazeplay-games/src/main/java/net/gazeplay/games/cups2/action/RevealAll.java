package net.gazeplay.games.cups2.action;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class RevealAll implements Action {
    private final List<Cup> cups;

    public RevealAll(List<Cup> cups) {
        this.cups = cups;
    }

    @Override
    public Type getType() {
        return Type.REVEAL_ALL;
    }

    @Override
    public double getDifficulty() {
        return Config.ACTION_REVEAL_DIFFICULTY;
    }

    @Override
    public int simulate(int ballIndex) {
        return ballIndex;
    }

    public boolean hasBall() {
        return true;
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        Callback<Void, Void> joinCallback = Action.joiner(onFinish, cups.size() + 1);
        for (Cup cup : cups)
            (new Reveal(cup)).execute(joinCallback);
        new Timeline(new KeyFrame(
            Duration.millis(Config.ACTION_REVEAL_TIME + Config.ROUND_DELAY),
            e -> joinCallback.call(null)
        )).play();
    }
}

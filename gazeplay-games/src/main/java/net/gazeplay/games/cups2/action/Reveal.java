package net.gazeplay.games.cups2.action;

import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.utils.Cup;

public class Reveal implements Action {
    private final Cup cup;

    public Reveal(Cup cup) {
        this.cup = cup;
    }

    @Override
    public Type getType() {
        return Type.REVEAL;
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
        return cup.hasBall();
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        double thirdTime = Config.ACTION_REVEAL_TIME / 3;

        TranslateTransition tt1 = new TranslateTransition(Duration.millis(thirdTime), cup);
        tt1.setInterpolator(Interpolator.EASE_BOTH);
        tt1.setByY(-2 / 3d * cup.getFitHeight());
        TranslateTransition tt2 = new TranslateTransition(Duration.millis(thirdTime), cup);
        tt2.setInterpolator(Interpolator.EASE_BOTH);
        tt2.setByY( 2 / 3d * cup.getFitHeight());

        SequentialTransition st = new SequentialTransition(
            tt1,
            new PauseTransition(Duration.millis(thirdTime)),
            tt2
        );
        st.setOnFinished(e -> {
            if (cup.hasBall())
                cup.getBall().setVisible(false);
            if (onFinish != null)
                onFinish.call(null);
        });

        if (cup.hasBall()) {
            cup.getBall().setVisible(true);
            cup.getBall().toBack();
        }

        st.play();
    }
}

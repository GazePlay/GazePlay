package net.gazeplay.games.gazeplayEval;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.games.gazeplayEval.config.*;
import net.gazeplay.games.gazeplayEval.round.EvalRound;

import java.util.Iterator;
import java.util.function.Function;

@Slf4j
public class GazeplayEval implements GameLifeCycle {
    @Getter
    private final double gameSeed;

    private final Iterator<EvalRound> rounds;
    private final EvalConfig config;

    @Getter
    private EvalRound currentRound;

    public GazeplayEval(double gameSeed) {
        this.gameSeed = gameSeed;

        EvalState.gameContext.startScoreLimiter();
        EvalState.gameContext.startTimeLimiter();

        final Function<Void, Void> onRoundFinishDummy = (aVoid) -> {
            this.onRoundFinish();
            return null;
        };

        try {
            config = new EvalConfig(EvalState.gameVariant.getNameGame());
            rounds = config.getItems().map(item -> new EvalRound(item, onRoundFinishDummy)).iterator();
        } catch (Exception e) {
            log.error("Error while loading the configuration file for the game " + EvalState.gameVariant.getNameGame(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void launch() {
        if (currentRound != null) {
            log.error("Trying to launch a new round while the current one is still running");
            return;
        }
        log.info("Starting new round");

        currentRound = rounds.next();
    }

    @Override
    public void dispose() {
        currentRound = null;
    }

    private void onRoundFinish() {
    };
}

package net.gazeplay.games.gazeplayEval;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.gazeplayEval.config.*;

@Slf4j
public class GazeplayEval implements GameLifeCycle {
    private final IGameContext gameContext;
    private final GazeplayEvalGameVariant gameVariant;
    private final boolean fourThree;
    private final Stats stats;

    public GazeplayEval(final boolean fourThree, final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats) {
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.fourThree = fourThree;
        this.stats = stats;

        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();

        Configuration config = ActiveConfigurationContext.getInstance();
        try {
            EvalConfig evalConfig = new EvalConfig(config.getFileDir() + "\\evals\\" + gameVariant.getNameGame());
        } catch (Exception e) {
            log.error("Error while loading the configuration file for the game " + gameVariant.getNameGame(), e);
        }
    }

    public GazeplayEval(final boolean fourThree, final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.fourThree = fourThree;
        this.stats = stats;

        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();

        Configuration config = ActiveConfigurationContext.getInstance();
        try {
            EvalConfig evalConfig = new EvalConfig(config.getFileDir() + "\\evals\\" + gameVariant.getNameGame());
        } catch (Exception e) {
            log.error("Error while loading the configuration file for the game " + gameVariant.getNameGame(), e);
        }
    }

    @Override
    public void launch() {
    }

    @Override
    public void dispose() {

    }
}

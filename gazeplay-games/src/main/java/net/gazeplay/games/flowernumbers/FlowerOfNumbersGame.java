package net.gazeplay.games.flowernumbers;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class FlowerOfNumbersGame implements GameLifeCycle {
    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;

    private final Flower flower;

    FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;

        random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());
        flower = new Flower();
    }

    FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;

        random = new ReplayablePseudoRandom(gameSeed);
        flower = new Flower();
    }

    public void launch() {
        gameContext.setLimiterAvailable();

        flower.setPistil(random.nextInt(17) + 1);
        log.info("pistil = {}", flower.getPistil());

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose() {

    }
}

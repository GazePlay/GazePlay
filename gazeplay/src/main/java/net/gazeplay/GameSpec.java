package net.gazeplay;

import javafx.scene.Scene;
import lombok.Getter;
import net.gazeplay.commons.utils.stats.Stats;

public class GameSpec {

    public interface GameLauncher<T extends Stats> {

        T createNewStats(Scene scene);

        GameLifeCycle createNewGame(GameContext gameContext, T stats);

    }

    @Getter
    private GameSummary gameSummary;

    @Getter
    private final String variationHint;

    @Getter
    private final GameLauncher gameLauncher;

    public GameSpec(GameSummary gameSummary, String variationHint, GameLauncher gameLauncher) {
        this.gameSummary = gameSummary;
        this.variationHint = variationHint;
        this.gameLauncher = gameLauncher;
    }

    public GameSpec(GameSummary gameSummary, GameLauncher gameLauncher) {
        this(gameSummary, null, gameLauncher);
    }

}

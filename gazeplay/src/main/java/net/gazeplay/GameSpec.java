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
    private final String nameCode;

    @Getter
    private final String variationHint;

    @Getter
    private final GameLauncher gameLauncher;

    public GameSpec(String nameCode, String variationHint, GameLauncher gameLauncher) {
        this.nameCode = nameCode;
        this.variationHint = variationHint;
        this.gameLauncher = gameLauncher;
    }

    public GameSpec(String nameCode, GameLauncher gameLauncher) {
        this.nameCode = nameCode;
        this.variationHint = null;
        this.gameLauncher = gameLauncher;
    }

}

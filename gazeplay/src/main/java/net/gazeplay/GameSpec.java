package net.gazeplay;

import lombok.Getter;
import net.gazeplay.commons.utils.stats.Stats;

public class GameSpec {

    public interface GameLauncher {

        Stats launchGame(GameSpec gameSpec, GameContext gameContext);

    }

    @Getter
    private final String nameCode;

    @Getter
    private final String variationHint;

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

    public Stats launch(GameContext gameContext) {
        return gameLauncher.launchGame(this, gameContext);
    }
}

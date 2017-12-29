package net.gazeplay;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import lombok.Getter;
import net.gazeplay.commons.utils.stats.Stats;

public class GameSpec {

    public interface GameLauncher {

        Stats launchGame(GameSpec gameSpec, Scene scene, Group root, ChoiceBox<String> cbxGames);

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

    public Stats launch(Scene scene, Group root, ChoiceBox<String> cbxGames) {
        return gameLauncher.launchGame(this, scene, root, cbxGames);
    }
}

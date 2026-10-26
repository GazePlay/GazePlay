package net.gazeplay.games.trainSwitches;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "TrainSwitches" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class TrainSwitchesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return  new GameSpec(GameSummary.builder()
            .nameCode("TrainSwitches")
            .gameThumbnail("trainSwitches")
            .category(GameCategories.Category.ACTION_REACTION)
            .category(GameCategories.Category.SELECTION).build(),
            new TrainSwitchesGameVariantGenerator(), new TrainSwitchesGameLauncher()
        );
    }
}

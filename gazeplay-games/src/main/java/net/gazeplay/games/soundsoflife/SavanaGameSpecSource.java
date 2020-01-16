package net.gazeplay.games.soundsoflife;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SavanaGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Savanna").gameThumbnail("data/Thumbnails/savana.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION).build(),
            new SavannaGameLauncher());
    }
}

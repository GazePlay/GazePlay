package net.gazeplay.games.whereisitconfigurable;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class WhereIsItConfigurableGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsItConfigurable").gameThumbnail("data/Thumbnails/whereisit.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(), new WhereIsItConfigurableGameLauncher());
    }
}

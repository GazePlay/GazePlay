package net.gazeplay.games.soundsoflife;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SoundsOfLifeGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SoundsOfLife").gameThumbnail("data/Thumbnails/soundsOfLife.png")
                .category(GameCategories.Category.SELECTION)
                .build(),
            new SoundsOfLifeGameVariantGenerator(), new SoundsOfLifeGameLauncher());
    }
}

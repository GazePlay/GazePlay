package net.gazeplay.games.TowerDefense;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class TowerDefenseGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(GameSummary.builder()
            .nameCode("TowerDefense")
            .gameThumbnail("data/Thumbnails/towerDefense.png")
            .category(GameCategories.Category.ACTION_REACTION).build(),
            new TowerDefenseVariantGenerator(),
            new TowerDefenseGameLauncher());
    }

}

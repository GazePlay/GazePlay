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
            .gameThumbnail("towerDefense")
            .category(GameCategories.Category.ACTION_REACTION).build(),
            new TowerDefenseGameLauncher());
    }

}

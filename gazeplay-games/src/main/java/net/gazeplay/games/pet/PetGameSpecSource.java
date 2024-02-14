package net.gazeplay.games.pet;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class PetGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Pet").gameThumbnail("pet")
                .category(GameCategories.Category.SELECTION).build(),
            new PetGameLauncher());
    }
}

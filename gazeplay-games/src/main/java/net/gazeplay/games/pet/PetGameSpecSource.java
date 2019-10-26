package net.gazeplay.games.pet;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class PetGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Pet").gameThumbnail("data/Thumbnails/pet.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new PetGameLauncher());
    }
}

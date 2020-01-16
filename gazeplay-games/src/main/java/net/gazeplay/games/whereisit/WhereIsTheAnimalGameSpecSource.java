package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class WhereIsTheAnimalGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheAnimal").gameThumbnail("data/Thumbnails/whereisanimal.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsItGameVariantGenerator(), new WhereIsTheAnimalGameLauncher());
    }
}

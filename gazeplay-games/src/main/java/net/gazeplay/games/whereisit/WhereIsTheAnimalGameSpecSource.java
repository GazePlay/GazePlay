package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class WhereIsTheAnimalGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheAnimal").gameThumbnail("data/Thumbnails/whereisanimal.png").category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheAnimalGameVariantGenerator(), new WhereIsTheAnimalGameLauncher());
    }
}

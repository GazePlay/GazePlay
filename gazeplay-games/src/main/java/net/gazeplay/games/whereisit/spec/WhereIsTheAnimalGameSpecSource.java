package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.WhereIsTheAnimalGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheAnimalGameLauncher;

public class WhereIsTheAnimalGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheAnimal").gameThumbnail("data/Thumbnails/whereisanimal.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheAnimalGameVariantGenerator(), new WhereIsTheAnimalGameLauncher());
    }
}

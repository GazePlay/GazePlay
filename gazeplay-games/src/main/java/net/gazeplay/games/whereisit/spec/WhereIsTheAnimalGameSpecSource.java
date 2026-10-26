package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheAnimalGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheAnimalGameLauncher;

/**
 * Declares the game "WhereIsTheAnimal" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class WhereIsTheAnimalGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheAnimal").gameThumbnail("whereIsTheAnimal")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheAnimalGameVariantGenerator(), new WhereIsTheAnimalGameLauncher());
    }
}

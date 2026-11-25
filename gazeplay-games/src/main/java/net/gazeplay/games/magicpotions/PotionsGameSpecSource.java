package net.gazeplay.games.magicpotions;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Potions" ("Recettes magiques") to the menu: its name, thumbnail, categories, variants and
 * launcher (see {@code GameSpecSource}).
 */
public class PotionsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Potions").gameThumbnail("potions")
                // "Recettes magiques" since 24/11/2026: find the pictures of the recipe among others (no more
                // colors to mix, nor recipe to remember)
                .category(GameCategories.Category.SELECTION).build(),
            new PotionsGameVariantGenerator(), new PotionsGameLauncher());
    }
}

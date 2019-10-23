package net.gazeplay.games.magicPotions;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class PotionsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Potions").gameThumbnail("data/Thumbnails/potions.jpg").category(GameCategories.Category.SELECTION).build(),
            new PotionsGameLauncher());
    }
}

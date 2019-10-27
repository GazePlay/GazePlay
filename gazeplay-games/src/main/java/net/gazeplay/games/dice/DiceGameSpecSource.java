package net.gazeplay.games.dice;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class DiceGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Dice").gameThumbnail("data/Thumbnails/dice.png").category(GameCategories.Category.MEMORIZATION).build(),
            new DiceGameVariantGenerator(), new DiceGameLauncher());
    }
}

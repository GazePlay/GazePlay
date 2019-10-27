package net.gazeplay.games.literacy;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class LettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("letters").gameThumbnail("data/Thumbnails/letters.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new LettersGameVariantGenerator(), new LettersGameLauncher());
    }
}

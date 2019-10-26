package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class OpenMemoryLettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("OpenMemoryLetters").gameThumbnail("data/Thumbnails/openMemoryLetters.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new OpenMemoryLettersGameVariantGenerator(), new OpenMemoryLettersGameLauncher());
    }
}

package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class OpenMemoryNumbersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("OpenMemoryNumbers").gameThumbnail("data/Thumbnails/openMemoryNumbers.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new OpenMemoryNumbersGameVariantGenerator(), new OpenMemoryNumbersGameLauncher());
    }
}

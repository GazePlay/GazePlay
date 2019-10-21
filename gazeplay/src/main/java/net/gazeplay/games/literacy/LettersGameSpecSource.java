package net.gazeplay.games.literacy;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class LettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("letters", "data/Thumbnails/letters.png", GameCategories.Category.ACTION_REACTION),
            new LettersGameVariantGenerator(), new LettersGameLauncher());
    }
}

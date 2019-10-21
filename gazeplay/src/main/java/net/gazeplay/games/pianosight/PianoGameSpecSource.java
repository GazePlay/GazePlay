package net.gazeplay.games.pianosight;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class PianoGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Piano", "data/Thumbnails/pianosight.png", GameCategories.Category.ACTION_REACTION),
            new PianoGameLauncher());
    }
}

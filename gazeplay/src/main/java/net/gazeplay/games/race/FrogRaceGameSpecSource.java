package net.gazeplay.games.race;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FrogRaceGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("FrogsRace", "data/Thumbnails/frogsrace.png", GameCategories.Category.SELECTION),
            new FrogsRaceGameLauncher());
    }
}

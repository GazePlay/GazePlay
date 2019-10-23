package net.gazeplay.games.race;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FrogRaceGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FrogsRace").gameThumbnail("data/Thumbnails/frogsrace.png").category(GameCategories.Category.SELECTION).build(),
            new FrogsRaceGameLauncher());
    }
}

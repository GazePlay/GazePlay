package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class WhereIsTheColorGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheColor").gameThumbnail("data/Thumbnails/whereiscolor.png").category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheColorGameVariantGenerator(), new WhereIsTheColorGameLauncher());
    }
}

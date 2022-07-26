package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheColorGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheColorGameLauncher;

public class WhereIsTheColorGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheColor").gameThumbnail("data/Thumbnails/whereIsTheColor.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheColorGameVariantGenerator(), new WhereIsTheColorGameLauncher());
    }
}

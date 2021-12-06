package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.WhereIsTheShapeGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheShapeGameLauncher;

public class WhereIsTheShapeGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheShape").gameThumbnail("data/Thumbnails/whereisshape.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheShapeGameVariantGenerator(), new WhereIsTheShapeGameLauncher());
    }
}

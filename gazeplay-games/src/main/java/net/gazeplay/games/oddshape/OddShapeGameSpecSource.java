package net.gazeplay.games.oddshape;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.trainSwitches.TrainSwitchesGameLauncher;
import net.gazeplay.games.trainSwitches.TrainSwitchesGameVariantGenerator;

public class OddShapeGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(GameSummary.builder()
            .nameCode("OddShape")
            .gameThumbnail("oddOne")
            //TODO: changer les catégories
            .category(GameCategories.Category.ACTION_REACTION)
            .category(GameCategories.Category.SELECTION).build(),
            new OddShapeVariantGenerator(),
            new OddShapeGameLauncher()
        );
    }
}

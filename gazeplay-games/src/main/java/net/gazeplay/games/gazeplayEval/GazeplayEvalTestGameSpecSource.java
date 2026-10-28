package net.gazeplay.games.gazeplayEval;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * GazePlay Eval Test (maintainer's request of 28/10/2026): plays an imported evaluation as GazePlay Eval does, without
 * the participant form and without recording anything, to check the calibration of the eye tracker. In the
 * "Evaluations" mode only, as GazePlay Eval.
 */
public class GazeplayEvalTestGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary
                .builder()
                .nameCode("GazePlayEvalTest")
                .gameThumbnail("whereIsIt")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION)
                .evaluation(true)
                .build(),
            new GazeplayEvalVariantGenerator(), new GazeplayEvalGameLauncher(true));
    }
}

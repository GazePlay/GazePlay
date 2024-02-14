package net.gazeplay.games.gazeplayEval.round;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.games.gazeplayEval.EvalState;
import net.gazeplay.games.gazeplayEval.config.ItemConfig;
import javafx.event.EventHandler;

import java.util.function.Function;


@Slf4j
public class EvalRound {
    private final ItemConfig config;
    private final PictureCard[][] pictures;

    public EvalRound(ItemConfig config, Function<Void, Void> onRoundFinish) {
        this.config = config;

        final Function<PictureCard, Void> onPictureCardSelectionDummy = (pictureCard) -> {
            this.onPictureCardSelection(pictureCard);
            return null;
        };
        GameSizing.computeGameSizing(config.getRowSize(), config.getColumnSize(), EvalState.gameContext.getGamePanelDimensionProvider().getDimension2D());
        this.pictures = new PictureCard[config.getRowSize()][config.getColumnSize()];
        for (int i = 0; i < config.getRowSize(); i++)
            for (int j = 0; j < config.getColumnSize(); j++)
                pictures[i][j] = new PictureCard(config, i, j, onPictureCardSelectionDummy);

        onRoundFinish.apply(null);
    }

    // TODO
    private void onPictureCardSelection (PictureCard pictureCard) {
        EvalState.stats.incrementNumberOfGoalsReached();
        EvalState.gameContext.updateScore(EvalState.stats, EvalState.evalInstance);

        if (EvalState.evalInstance.checkAllPictureCardChecked()) {
            Timeline transition = new Timeline();
            transition.getKeyFrames().add(new KeyFrame(new Duration(ActiveConfigurationContext.getInstance().getTransitionTime())));
            transition.setOnFinished(event -> {
                if (EvalState.evalInstance.increaseIndexFileImage()) {
                    EvalState.evalInstance.finalStats();
                    EvalState.gameContext.updateScore(EvalState.stats, EvalState.evalInstance);
                    EvalState.evalInstance.resetFromReplay();
                    EvalState.evalInstance.dispose();
                    EvalState.gameContext.clear();
                    EvalState.gameContext.showRoundStats(EvalState.stats, EvalState.evalInstance);
                } else {
                    EvalState.evalInstance.dispose();
                    EvalState.gameContext.clear();
                    EvalState.evalInstance.launch();
                }
            });
            EvalState.evalInstance.removeEventHandlerPictureCard();
            transition.playFromStart();
        }
    };
}

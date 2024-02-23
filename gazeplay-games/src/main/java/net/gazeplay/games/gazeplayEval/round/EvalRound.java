package net.gazeplay.games.gazeplayEval.round;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.games.gazeplayEval.GameState;
import net.gazeplay.games.gazeplayEval.config.ItemConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.gazeplay.games.gazeplayEval.config.AudioScheduleType.*;
import static net.gazeplay.games.gazeplayEval.config.Const.*;


@Slf4j
public class EvalRound {
    private final ItemConfig config;
    private final PictureCard[][] pictures;
    private final Function<Void, Void> onRoundFinish;
    private int nbPictureCardsSelected = 0;

    private Long startTime;

    public EvalRound(ItemConfig config, Function<Void, Void> onRoundFinish) {
        this.config = config;
        this.onRoundFinish = onRoundFinish;

        GameSizing.computeGameSizing(config.getRowSize(), config.getColumnSize(), GameState.context.getGamePanelDimensionProvider().getDimension2D());
        this.pictures = new PictureCard[config.getRowSize()][config.getColumnSize()];
        this.buildPictureGrid();
    }

    private void buildPictureGrid() {
        ArrayList<Integer> xOrder = new ArrayList<>(config.getRowSize());
        ArrayList<Integer> yOrder = new ArrayList<>(config.getColumnSize());

        for (int i = 0; i < config.getRowSize(); i++)
            xOrder.add(i);
        for (int j = 0; j < config.getColumnSize(); j++)
            yOrder.add(j);

        if (config.isGridRandomized()) {
            GameState.eval.getRandom().shuffle(xOrder);
            GameState.eval.getRandom().shuffle(yOrder);
        }

        for (int i = 0; i < config.getRowSize(); i++)
            for (int j = 0; j < config.getColumnSize(); j++)
                if (config.getGrid(i, j).isFile())
                    pictures[xOrder.get(i)][yOrder.get(j)] = new PictureCard(config, xOrder.get(i), yOrder.get(j), this::onPictureCardSelection);
    }

    private Void onPictureCardSelection(PictureCard pictureCard) {
        nbPictureCardsSelected++;
        GameState.stats.incrementNumberOfGoalsReached();
        GameState.context.updateScore(GameState.stats, GameState.eval);

        if (nbPictureCardsSelected >= config.getSelectionsRequired())
            onRoundFinish.apply(null);

        return null;  // Make Void happy
    }

    private Stream<PictureCard> getFlatPictures() {
        return Arrays.stream(pictures).flatMap(Arrays::stream).filter(Objects::nonNull);
    }

    public void launch() {
        startTime = System.currentTimeMillis();

        GameState.context.getChildren().addAll(this.getFlatPictures().toList());

        this.getFlatPictures().forEach(PictureCard::show);

//        If you need the TargetAOI list for this round, here it is
//        getFlatPictures().map(PictureCard::getTargetAOI).collect(Collectors.toCollection(ArrayList::new);

        GameState.stats.notifyNewRoundReady();
        GameState.context.onGameStarted(ActiveConfigurationContext.getInstance().getDelayBeforeSelectionTime());

        if (config.getAudioSchedule() == BEFORE) {
            this.playSound();
        } else {
            Timeline transition = new Timeline();
            transition.getKeyFrames().add(new KeyFrame(new Duration(ActiveConfigurationContext.getInstance().getDelayBeforeSelectionTime())));
            transition.setOnFinished((event) -> this.playSound());
            transition.playFromStart();
        }
    }

    public void dispose() {
        getFlatPictures().forEach(PictureCard::dispose);
    }

    public void playSound() {
        if (ActiveConfigurationContext.getInstance().isSoundEnabled() && config.getAudioFile().isFile()) {
            GameState.context.getSoundManager().add(config.getAudioFile().getPath());
        }
    }
}

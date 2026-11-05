package net.gazeplay.games.opinions;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Game "Opinions" (reviewed on 2/11/2026, rules of the maintainer): the images given by the user (folder
 * images/Opinions, in the order of their names) are shown one by one in the middle; for each, the person says whether
 * they like it or not by a fixation on "I like it" (left) or "I don't like it" (right). The answers are recorded and
 * written with the statistics ({@link OpinionsGameStats}: image and choice). After the last image: "Bravo", then the
 * statistics.
 */
@Slf4j
public class OpinionsGame implements GameLifeCycle {

    /**
     * How long the answer stays shown before the next image.
     */
    static final Duration ANSWER_SHOWN = Duration.millis(700);

    private final IGameContext gameContext;
    private final OpinionsGameStats stats;

    /**
     * The images of the game, in the order they are shown.
     */
    @Getter
    private final List<File> images;

    @Getter
    private final List<OpinionButton> buttons = new ArrayList<>();

    private final PauseTransition next = new PauseTransition(ANSWER_SHOWN);

    private Group layer;
    private Group picture;
    private Group counter;
    private Dimension2D dimension;

    /**
     * The index of the image shown.
     */
    @Getter
    private int index;

    public OpinionsGame(final IGameContext gameContext, final OpinionsGameStats stats) {
        this(gameContext, stats, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game (the images come in the order
     *                 of their names: the seed is only kept with the statistics)
     */
    public OpinionsGame(final IGameContext gameContext, final OpinionsGameStats stats, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        if (gameSeed < 0) {
            stats.setGameSeed(new ReplayablePseudoRandom().getSeed());
        }
        images = UserImagesVariant.images(OpinionsGameVariant.USER_IMAGES_FOLDER);
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension.getWidth();
        final double height = dimension.getHeight();

        final Rectangle background = new Rectangle(width, height);
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);
        layer = new Group(background);
        gameContext.getChildren().add(layer);

        // The answers on each side of the image, in the middle of the height (away from the bottom left corner)
        final double buttonWidth = Math.min(width * 0.17, height * 0.3);
        final double buttonHeight = Math.min(height * 0.42, buttonWidth * 1.4);
        final double margin = width * 0.03;
        for (final OpinionChoice choice : OpinionChoice.values()) {
            final OpinionButton button = new OpinionButton(gameContext, choice, buttonWidth, buttonHeight,
                () -> answer(choice));
            button.setLayoutX(choice == OpinionChoice.LIKE ? margin : width - margin - buttonWidth);
            button.setLayoutY((height - buttonHeight) / 2);
            buttons.add(button);
            layer.getChildren().add(button);
        }

        index = 0;
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
        if (images.isEmpty()) {
            buttons.forEach(button -> button.setEnabled(false));
            final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                height * 0.035, width * 0.5);
            message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
            message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
            layer.getChildren().add(message);
            return;
        }
        showImage();
    }

    /**
     * Shows the image of {@link #index} in a card in the middle, as large as the space between the answers allows,
     * with the count of the images above it.
     */
    private void showImage() {
        if (picture != null) {
            layer.getChildren().remove(picture);
        }
        final double width = dimension.getWidth();
        final double height = dimension.getHeight();
        final double band = height * 0.1;
        final double sideSpace = buttons.getFirst().getBoundsInLocal().getWidth() + width * 0.06;
        final double cardWidth = width - 2 * sideSpace;
        final double cardHeight = height - 2 * band;
        final double padding = Math.min(cardWidth, cardHeight) * 0.03;

        final File file = images.get(index);
        final Image image = new Image(file.toURI().toString(), cardWidth * 1.5, cardHeight * 1.5, true, true);
        final ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(cardWidth - 2 * padding);
        view.setFitHeight(cardHeight - 2 * padding);
        final double shownWidth = image.getWidth() == 0 ? cardWidth - 2 * padding
            : Math.min(cardWidth - 2 * padding, image.getWidth() * (cardHeight - 2 * padding) / image.getHeight());
        final double shownHeight = image.getWidth() == 0 ? cardHeight - 2 * padding
            : Math.min(cardHeight - 2 * padding, image.getHeight() * (cardWidth - 2 * padding) / image.getWidth());
        final double cardX = (width - shownWidth) / 2 - padding;
        final double cardY = band + (cardHeight - shownHeight) / 2 - padding;
        final Rectangle card = GameStyle.targetCard(cardX, cardY, shownWidth + 2 * padding,
            shownHeight + 2 * padding);
        view.setLayoutX(cardX + padding);
        view.setLayoutY(cardY + padding);
        view.setMouseTransparent(true);
        picture = new Group(card, view);
        picture.setMouseTransparent(true);
        layer.getChildren().add(1, picture);

        if (counter != null) {
            layer.getChildren().remove(counter);
        }
        counter = GameStyle.pill((index + 1) + " / " + images.size(), height * 0.032);
        counter.setLayoutX((width - counter.getBoundsInLocal().getWidth()) / 2);
        counter.setLayoutY((band - counter.getBoundsInLocal().getHeight()) / 2);
        counter.setMouseTransparent(true);
        layer.getChildren().add(counter);

        final FadeTransition fade = new FadeTransition(Duration.millis(250), picture);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        buttons.forEach(button -> {
            button.setChosen(false);
            button.setEnabled(true);
        });
        stats.incrementNumberOfGoalsToReach();
    }

    /**
     * An answer given on the image shown: recorded, shown a moment, then the next image (or the end).
     */
    void answer(final OpinionChoice choice) {
        if (index >= images.size() || buttons.stream().noneMatch(OpinionButton::isEnabled)) {
            return;
        }
        final String name = images.get(index).getName();
        log.info("{}: {}", name, choice.getWritten());
        stats.addAnswer(name, choice);
        stats.incrementNumberOfGoalsReached();
        buttons.forEach(button -> {
            button.setEnabled(false);
            button.setChosen(button.getChoice() == choice);
        });
        next.setOnFinished(event -> {
            index++;
            if (index < images.size()) {
                showImage();
            } else {
                stats.takeScreenShot();
                gameContext.updateScore(stats, this);
                gameContext.playWinTransition(500, end -> gameContext.showRoundStats(stats, this));
            }
        });
        next.playFromStart();
    }

    @Override
    public void dispose() {
        next.stop();
        buttons.forEach(OpinionButton::dispose);
        buttons.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
        picture = null;
        counter = null;
    }
}

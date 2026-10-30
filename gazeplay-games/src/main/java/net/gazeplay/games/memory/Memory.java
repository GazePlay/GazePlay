package net.gazeplay.games.memory;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Game "Memory" (reviewed on 30/10/2026, REVUE_DES_JEUX.md; one card in the menu instead of six): pairs of cards
 * showing pictures, letters, digits or the pictures of the user, face down or face up. A fixation chooses a card; two
 * cards of a pair are framed in green and removed; two cards of different pairs are framed in the color "danger", then
 * turn back. When all the pairs are found: "Bravo", then a new round. With the dynamic difficulty, the size of the
 * grid changes every 3 rounds with the number of tries.
 * <p>
 * Statistics: a goal to reach for each pair of a round, reached when the pair is found.
 */
@Slf4j
public class Memory implements GameLifeCycle {

    /**
     * How long two cards of different pairs stay shown before they turn back (maintainer's choice of 30/10/2026: it
     * used to be 1 s).
     */
    static final Duration WRONG_PAIR_SHOWN = Duration.millis(1500);

    /**
     * How long a pair found stays framed in green before it is removed.
     */
    static final Duration RIGHT_PAIR_SHOWN = Duration.millis(700);

    private static final double CARD_RATIO = 0.75;

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;

    @Getter
    private final MemoryContent content;

    /**
     * Face up cards (the former "Open Memory").
     */
    @Getter
    private final boolean faceUp;

    @Getter
    private final boolean dynamic;

    @Getter
    private int nbLines;

    @Getter
    private int nbColumns;

    /**
     * Level of the dynamic difficulty: the number of pairs (2 to 10).
     */
    @Getter
    private int level = 2;

    @Getter
    private final List<MemoryCard> cards = new ArrayList<>();

    /**
     * Tries (pairs turned over) of each round, for the dynamic difficulty.
     */
    @Getter
    private final List<Integer> roundTries = new ArrayList<>();

    private final List<Animation> pauses = new ArrayList<>();

    private Group layer;
    private Group pairsPill;
    private Dimension2D dimension;
    private MemoryCard firstCard;

    @Getter
    private boolean comparing;

    @Getter
    private int pairs;

    @Getter
    private int pairsFound;

    private int tries;

    public Memory(final IGameContext gameContext, final Stats stats, final GroupedGameVariant variant) {
        this(gameContext, stats, variant, new ReplayablePseudoRandom());
        this.gameContext.startTimeLimiter();
        this.stats.setGameSeed(random.getSeed());
    }

    public Memory(final IGameContext gameContext, final Stats stats, final GroupedGameVariant variant,
                  final double gameSeed) {
        this(gameContext, stats, variant, new ReplayablePseudoRandom(gameSeed));
        this.gameContext.startTimeLimiter();
    }

    private Memory(final IGameContext gameContext, final Stats stats, final GroupedGameVariant variant,
                   final ReplayablePseudoRandom random) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.random = random;
        this.content = MemoryContent.ofKey(variant.getGroup());
        this.faceUp = MemoryGameVariantGenerator.VISIBLE.equals(variant.getSubGroup());
        this.dynamic = !variant.isSize();
        if (dynamic) {
            adaptLevel();
        } else {
            this.nbLines = variant.getLines();
            this.nbColumns = variant.getColumns();
        }
        if ((nbLines * nbColumns) % 2 != 0) {
            throw new IllegalArgumentException("Cards count must be an even number in this game");
        }
    }

    @Override
    public void launch() {
        cards.clear();
        pauses.clear();
        firstCard = null;
        comparing = false;
        pairsFound = 0;
        tries = 0;
        gameContext.setLimiterAvailable();
        dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Rectangle background = new Rectangle(dimension.getWidth(), dimension.getHeight());
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);
        layer = new Group(background);
        gameContext.getChildren().add(layer);

        pairs = nbLines * nbColumns / 2;
        final List<Image> images = content == MemoryContent.LETTERS || content == MemoryContent.NUMBERS
            ? null : pickImages(pairs);
        if (images != null && images.isEmpty()) {
            // The pictures of the user, none given (game started without the menu, which asks for them)
            final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                dimension.getHeight() * 0.04, dimension.getWidth() * 0.6);
            message.setLayoutX((dimension.getWidth() - message.getBoundsInLocal().getWidth()) / 2);
            message.setLayoutY((dimension.getHeight() - message.getBoundsInLocal().getHeight()) / 2);
            layer.getChildren().add(message);
            return;
        }
        final List<String> texts = images == null
            ? pickTexts(content == MemoryContent.LETTERS ? LETTERS : DIGITS, pairs) : null;
        createCards(images, texts);
        updatePairsPill();

        stats.incrementNumberOfGoalsToReach(pairs);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.onGameStarted(3000);
    }

    @Override
    public void dispose() {
        pauses.forEach(Animation::stop);
        pauses.clear();
        cards.forEach(MemoryCard::release);
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Cards

    /**
     * @return the pictures of the pairs, drawn at random: the default pictures, or the pictures of the user (completed
     *   with default pictures when there are not enough; empty when there are none)
     */
    private List<Image> pickImages(final int count) {
        final List<Image> result = new ArrayList<>();
        if (content == MemoryContent.USER_IMAGES) {
            final List<File> files = new ArrayList<>(UserImagesVariant.images(MemoryContent.USER_IMAGES_FOLDER));
            if (files.isEmpty()) {
                return result;
            }
            while (!files.isEmpty() && result.size() < count) {
                final File file = files.remove(random.nextInt(files.size()));
                result.add(new Image(file.toURI().toString(), 600, 600, true, true));
            }
        }
        if (result.size() < count) {
            final ImageLibrary library = ImageUtils.createDefaultImageLibrary(null, random);
            result.addAll(library.pickMultipleRandomDistinctImages(count - result.size()));
        }
        return result;
    }

    /**
     * @return letters or digits of the pairs, distinct, drawn at random
     */
    private List<String> pickTexts(final String alphabet, final int count) {
        final List<String> left = new ArrayList<>();
        for (final char character : alphabet.toCharArray()) {
            left.add(String.valueOf(character));
        }
        final List<String> result = new ArrayList<>();
        while (result.size() < count && !left.isEmpty()) {
            result.add(left.remove(random.nextInt(left.size())));
        }
        return result;
    }

    /**
     * The cards of the grid, two per picture (or text), at random places, with room for the label of the pairs at the
     * top and for the button of the settings at the bottom.
     */
    private void createCards(final List<Image> images, final List<String> texts) {
        final List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            ids.add(i);
            ids.add(i);
        }
        // Shuffled with the generator of the game, for the replay
        for (int i = ids.size() - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final int swap = ids.get(i);
            ids.set(i, ids.get(j));
            ids.set(j, swap);
        }

        final double top = dimension.getHeight() * 0.1;
        final double bottom = dimension.getHeight() * 0.1;
        final double cellWidth = dimension.getWidth() * 0.96 / nbColumns;
        final double cellHeight = (dimension.getHeight() - top - bottom) / nbLines;
        final double cardHeight = Math.min(cellHeight * 0.9, cellWidth * 0.9 / CARD_RATIO);
        // At least 3/4 of the height wide, up to square when there is room (larger pictures)
        final double cardWidth = Math.max(cardHeight * CARD_RATIO, Math.min(cardHeight, cellWidth * 0.88));
        final double left = dimension.getWidth() * 0.02;
        int index = 0;
        for (int line = 0; line < nbLines; line++) {
            for (int column = 0; column < nbColumns; column++) {
                final int id = ids.get(index++);
                final double x = left + column * cellWidth + (cellWidth - cardWidth) / 2;
                final double y = top + line * cellHeight + (cellHeight - cardHeight) / 2;
                final MemoryCard card = new MemoryCard(x, y, cardWidth, cardHeight, id,
                    images == null ? null : images.get(id), texts == null ? null : texts.get(id), faceUp, this,
                    gameContext);
                cards.add(card);
            }
        }
        layer.getChildren().addAll(cards);
    }

    private void updatePairsPill() {
        if (pairsPill != null && layer != null) {
            layer.getChildren().remove(pairsPill);
        }
        pairsPill = GameStyle.pill(gameContext.getTranslator().translate("MemoryPairs") + " " + pairsFound + " / "
            + pairs, dimension.getHeight() * 0.032);
        pairsPill.setLayoutX((dimension.getWidth() - pairsPill.getBoundsInLocal().getWidth()) / 2);
        pairsPill.setLayoutY(dimension.getHeight() * 0.02);
        pairsPill.setMouseTransparent(true);
        layer.getChildren().add(pairsPill);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Turns

    /**
     * @return whether a card can be chosen now: not while two cards are compared, nor a card chosen or found
     */
    boolean canChoose(final MemoryCard card) {
        return !comparing && !card.isSelected() && !card.isFound();
    }

    /**
     * A card chosen by a fixation: the first one of a try waits for the second one; then both are compared.
     */
    void cardFixated(final MemoryCard card) {
        if (!canChoose(card)) {
            return;
        }
        card.select();
        if (firstCard == null) {
            firstCard = card;
            return;
        }
        final MemoryCard other = firstCard;
        firstCard = null;
        comparing = true;
        tries++;
        if (other.getPairId() == card.getPairId()) {
            pairsFound++;
            stats.incrementNumberOfGoalsReached();
            updatePairsPill();
            other.showRight();
            card.showRight();
            later(RIGHT_PAIR_SHOWN, () -> {
                other.remove();
                card.remove();
                comparing = false;
                if (pairsFound == pairs) {
                    roundWon();
                }
            });
        } else {
            other.showWrong();
            card.showWrong();
            later(WRONG_PAIR_SHOWN, () -> {
                other.reset(!faceUp);
                card.reset(!faceUp);
                comparing = false;
            });
        }
    }

    /**
     * All the pairs are found: the dynamic difficulty is updated, then "Bravo" and a new round.
     */
    private void roundWon() {
        if (dynamic) {
            stats.getLevelsReport().addRoundLevel(level);
            roundTries.add(tries);
            if (roundTries.size() % 3 == 0) {
                updateLevel();
                stats.getLevelsReport().addRoundLevel(level);
            }
            adaptLevel();
        }
        gameContext.updateScore(stats, this);
        gameContext.playWinTransition(500, event -> {
            dispose();
            gameContext.clear();
            launch();
        });
    }

    /**
     * Changes the level after 3 rounds: up when the 3 rounds took at most 2 tries per pair, down when they took at
     * least 2.5 (30/10/2026: the same round used to be counted 3 times).
     */
    void updateLevel() {
        int compare = 0;
        for (final int roundTry : roundTries.subList(roundTries.size() - 3, roundTries.size())) {
            if (roundTry <= 2 * level && nbColumns <= 6) {
                compare++;
            }
            if (roundTry >= 2.5 * level && nbColumns > 2) {
                compare--;
            }
        }
        if (compare == 3) {
            level += level == 6 ? 2 : 1;
        } else if (compare == -3) {
            level -= level == 8 ? 2 : 1;
        }
        level = Math.max(2, Math.min(10, level));
    }

    /**
     * Sets the size of the grid for the current level: 2 lines of {@code level} cards, or 4 x 3, 4 x 4, 6 x 3 and
     * 5 x 4 for the levels 6, 8, 9 and 10.
     */
    void adaptLevel() {
        if (level == 6) {
            nbColumns = 4;
            nbLines = 3;
        } else if (level == 8) {
            nbColumns = 4;
            nbLines = 4;
        } else if (level == 9) {
            nbColumns = 6;
            nbLines = 3;
        } else if (level == 10) {
            nbColumns = 5;
            nbLines = 4;
        } else {
            nbColumns = level;
            nbLines = 2;
        }
    }

    /**
     * For the tests: the level of the dynamic difficulty.
     */
    void setLevel(final int level) {
        this.level = level;
    }

    private void later(final Duration delay, final Runnable action) {
        final PauseTransition pause = new PauseTransition(delay);
        pause.setOnFinished(e -> {
            pauses.remove(pause);
            action.run();
        });
        pauses.add(pause);
        pause.play();
    }
}

package net.gazeplay.games.magiccards;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Cartes magiques" (reviewed on 12/11/2026, maintainer's rules): the grids in the screen, the delay, the fixation, a
 * wrong card (mistake, stays turned over), the picture found (reward, next round), the magic (shown, turned over,
 * shuffled, nothing chosen meanwhile), the end, the variants and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class MagicCardsTest {

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    private final Configuration configuration = mock(Configuration.class, withSettings().stubOnly());

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 900));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getGameStartDelay()).thenReturn(0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private MagicCards launched(final MagicCardsMode mode, final MagicCardsCount count, final MagicCardsEnd end)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final MagicCards game = new MagicCards(gameContext, stats, mode, MagicCardsPictures.ANIMALS, count, end, 4);
            game.setManual();
            launchedGames.add(game).launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final MagicCards game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static MagicCards.Card wrongCard(final MagicCards game) {
        return game.getCards().stream().filter(card -> !game.getWinners().contains(card) && !card.isTurned())
            .findFirst().orElseThrow();
    }

    /**
     * Steps through the show and the shuffle, until the cards can be chosen.
     */
    private static void untilPlay(final MagicCards game) {
        for (int i = 0; i < 5000 && game.getPhase() != MagicCards.Phase.PLAY; i++) {
            game.step(0.02);
        }
        assertEquals(MagicCards.Phase.PLAY, game.getPhase());
    }

    /**
     * Rule of 17/11/2026: half of the cards (rounded down) have a picture, each its own, all shown face up first.
     */
    @Test
    void shouldHideHalfOfTheCardsRoundedDown() throws Exception {
        assertEquals(List.of(1, 2, 3, 4), List.of(MagicCardsCount.THREE.getToFind(), MagicCardsCount.FOUR.getToFind(),
            MagicCardsCount.SIX.getToFind(), MagicCardsCount.NINE.getToFind()));
        for (final MagicCardsCount count : MagicCardsCount.values()) {
            final MagicCards game = launched(MagicCardsMode.MAGIC, count, MagicCardsEnd.ENDLESS);
            fx(() -> {
                assertEquals(count.getToFind(), game.getWinners().size(), count.toString());
                assertTrue(game.getWinners().stream().allMatch(MagicCards.Card::isFaceUp));
                assertEquals(count.getToFind(), game.getCards().stream().filter(MagicCards.Card::isFaceUp).count());
                verify(stats).incrementNumberOfGoalsToReach(count.getToFind());
                return null;
            });
            clearInvocations(stats);
        }
    }

    /**
     * Tester's feedback of 20/11/2026, point 29: the face of a card is white in both themes, and the whole card is
     * chosen with the mouse, not only the drawing.
     */
    @Test
    void shouldPutThePicturesOnAWhiteCardChosenEverywhere() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.FOUR, MagicCardsEnd.ENDLESS);
        fx(() -> {
            for (final MagicCards.Card card : game.getCards()) {
                final javafx.scene.Group face = (javafx.scene.Group) card.getNode().getChildren().get(0);
                final javafx.scene.shape.Rectangle surface = (javafx.scene.shape.Rectangle) face.getChildren().get(0);
                assertTrue(surface.getStyle().contains("-fx-fill: #FFFFFF"), surface.getStyle());
                assertFalse(surface.isMouseTransparent());
            }
            return null;
        });
    }

    @Test
    void shouldPutEveryGridInTheScreenWithoutOverlap() throws Exception {
        for (final MagicCardsCount count : MagicCardsCount.values()) {
            final MagicCards game = launched(MagicCardsMode.MAGIC, count, MagicCardsEnd.ENDLESS);
            fx(() -> {
                assertEquals(count.getCards(), game.getCards().size());
                final double h = game.getCardHeight();
                final double w = h * 0.75;
                for (int i = 0; i < count.getCards(); i++) {
                    final double[] a = game.getSlot(i);
                    assertTrue(a[0] - w / 2 >= 0 && a[0] + w / 2 <= 1600, count + " " + i);
                    assertTrue(a[1] - h / 2 >= 900 * 0.12 - 1 && a[1] + h / 2 <= 900, count + " " + i);
                    for (int j = i + 1; j < count.getCards(); j++) {
                        final double[] b = game.getSlot(j);
                        assertTrue(Math.abs(a[0] - b[0]) >= w || Math.abs(a[1] - b[1]) >= h, count + " " + i + " " + j);
                    }
                }
                return null;
            });
        }
    }

    @Test
    void shouldTurnACardOverAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.FOUR, MagicCardsEnd.ENDLESS);
        fx(() -> {
            // Nothing during the delay
            game.lookAt(game.getCards().get(0));
            assertNull(game.getLooked());
            steps(game, 1.02);
            assertEquals(MagicCards.Phase.SHOW, game.getPhase());
            untilPlay(game);
            final MagicCards.Card card = wrongCard(game);
            game.lookAt(card);
            steps(game, 0.3);
            game.lookAway(card);
            steps(game, 0.5);
            assertFalse(card.isTurned());
            game.lookAt(card);
            steps(game, 0.52);
            assertTrue(card.isTurned());
            return null;
        });
    }

    /**
     * Point 22 (23/11/2026): when the shuffled cards can be chosen, the delay of the settings is asked; a card looked
     * at meanwhile is not turned over, then after the fixation counted from the end of the delay.
     */
    @Test
    void shouldWaitForTheDelayWhenTheCardsCanBeChosen() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.FOUR, MagicCardsEnd.ENDLESS);
        fx(() -> {
            clearInvocations(gameContext);
            when(gameContext.isSelectionBlocked()).thenReturn(true);
            untilPlay(game);
            verify(gameContext).onGameStarted();
            final MagicCards.Card card = wrongCard(game);
            game.lookAt(card);
            steps(game, 2);
            assertFalse(card.isTurned(), "nothing chosen during the delay");
            when(gameContext.isSelectionBlocked()).thenReturn(false);
            steps(game, 0.4);
            assertFalse(card.isTurned(), "the fixation counts from the end of the delay");
            steps(game, 0.15);
            assertTrue(card.isTurned());
            return null;
        });
    }

    @Test
    void shouldCountAWrongCardThatStaysTurnedOver() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.SIX, MagicCardsEnd.ENDLESS);
        fx(() -> {
            untilPlay(game);
            final MagicCards.Card card = wrongCard(game);
            game.choose(card);
            steps(game, MagicCards.FLIP + 0.1);
            assertTrue(card.isWrong());
            assertTrue(card.isFaceUp());
            assertEquals(0.55, card.getNode().getOpacity(), 1e-9);
            verify(stats).incrementNumberOfMistakes();
            verify(gameContext.getSoundManager()).add("data/dice/sounds/wrong.wav");
            // Already seen: it cannot be chosen again
            game.lookAt(card);
            assertNull(game.getLooked());
            game.choose(card);
            verify(stats, times(1)).incrementNumberOfMistakes();
            assertEquals(MagicCards.Phase.PLAY, game.getPhase());
            return null;
        });
    }

    /**
     * 4 cards, 2 pictures (17/11/2026): a picture found stays face up and the round goes on; both found, the others
     * fade, a star and "Bravo !", then another round with its show.
     */
    @Test
    void shouldFindAllThePicturesBeforeTheNextRound() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.FOUR, MagicCardsEnd.ENDLESS);
        fx(() -> {
            untilPlay(game);
            final List<MagicCards.Card> winners = List.copyOf(game.getWinners());
            assertEquals(2, winners.size());
            game.choose(winners.get(0));
            steps(game, MagicCards.FLIP + 0.05);
            assertEquals(MagicCards.Phase.PLAY, game.getPhase(), "one more to find");
            assertEquals(1, game.getFoundInRound());
            assertTrue(winners.get(0).isFaceUp());
            assertFalse(winners.get(0).isWrong());
            assertEquals(0, game.getFound());
            // Found: it cannot be chosen again
            game.choose(winners.get(0));
            assertEquals(1, game.getFoundInRound());
            game.choose(winners.get(1));
            assertEquals(MagicCards.Phase.REVEAL, game.getPhase());
            assertEquals(1, game.getFound());
            verify(stats, times(2)).incrementNumberOfGoalsReached();
            steps(game, MagicCards.FLIP + MagicCards.GROW + 0.05);
            assertTrue(winners.stream().allMatch(MagicCards.Card::isFaceUp));
            assertEquals(2, game.getReward().getChildren().size());
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            game.getCards().stream().filter(card -> !winners.contains(card))
                .forEach(card -> assertEquals(0, card.getNode().getOpacity(), 1e-9));
            steps(game, MagicCards.REWARD);
            // Another round, its pictures shown
            assertEquals(MagicCards.Phase.SHOW, game.getPhase());
            assertTrue(game.getWinners().stream().noneMatch(winners::contains));
            assertTrue(game.getReward().getChildren().isEmpty());
            verify(stats, times(2)).incrementNumberOfGoalsToReach(2);
            return null;
        });
    }

    /**
     * 3 cards, 1 picture: found, it comes to the middle and grows (the 3 cards in a row are already large).
     */
    @Test
    void shouldGrowASinglePictureFound() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.THREE, MagicCardsEnd.ENDLESS);
        fx(() -> {
            untilPlay(game);
            final MagicCards.Card winner = game.getWinners().get(0);
            game.choose(winner);
            assertEquals(MagicCards.Phase.REVEAL, game.getPhase());
            steps(game, MagicCards.FLIP + MagicCards.GROW + 0.05);
            assertEquals(800, winner.getNode().getLayoutX(), 1e-6, "in the middle");
            assertTrue(winner.getNode().getScaleY() >= 1);
            return null;
        });
    }

    @Test
    void shouldShowThePictureThenShuffleTheCardsInTheMagicMode() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.THREE, MagicCardsEnd.ENDLESS);
        fx(() -> {
            final MagicCards.Card winner = game.getWinners().get(0);
            assertTrue(winner.isFaceUp());
            assertEquals(MagicCards.Phase.SHOW, game.getPhase());
            // Nothing can be chosen while it is shown, nor while the cards move
            game.lookAt(wrongCard(game));
            assertNull(game.getLooked());
            steps(game, MagicCards.SHOW + MagicCards.FLIP + 0.1);
            assertFalse(winner.isFaceUp());
            assertEquals(MagicCards.Phase.SHUFFLE, game.getPhase());
            game.choose(winner);
            assertEquals(0, game.getFound());
            final Set<Integer> slots = new HashSet<>();
            final double duration = MagicCardsCount.THREE.getSwaps() * (MagicCards.SWAP + MagicCards.SWAP_PAUSE) + 0.2;
            for (double t = 0; t < duration; t += 0.02) {
                game.step(0.02);
                slots.add(winner.getSlot());
            }
            assertEquals(MagicCards.Phase.PLAY, game.getPhase());
            verify(gameContext.getSoundManager(), times(MagicCardsCount.THREE.getSwaps()))
                .add("data/cups/sounds/slide.wav");
            // Each card at its own place, at the center of it
            final Set<Integer> places = new HashSet<>();
            for (final MagicCards.Card card : game.getCards()) {
                assertTrue(places.add(card.getSlot()));
                assertEquals(game.getSlot(card.getSlot())[0], card.getNode().getLayoutX(), 1e-6);
                assertEquals(game.getSlot(card.getSlot())[1], card.getNode().getLayoutY(), 1e-6);
            }
            game.choose(winner);
            assertEquals(1, game.getFound());
            return null;
        });
    }

    /**
     * 3 lives (maintainer's request of 16/11/2026): a wrong card costs one, the third loses the game ("Lost!", then the
     * statistics); without end too since 17/11/2026, never with "without hearts".
     */
    @Test
    void shouldLoseALifeAtEachWrongCardThenTheGame() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.NINE, MagicCardsEnd.ENDLESS);
        fx(() -> {
            assertTrue(game.hasLives());
            untilPlay(game);
            for (int mistake = 1; mistake <= 3; mistake++) {
                game.choose(wrongCard(game));
                steps(game, MagicCards.FLIP + 0.1);
                assertEquals(MagicCards.LIVES - mistake, game.getLives());
            }
            assertEquals(MagicCards.Phase.LOST, game.getPhase());
            verify(gameContext, never()).showRoundStats(any(), any());
            steps(game, MagicCards.LOST_SHOWN + 0.04);
            assertEquals(MagicCards.Phase.OVER, game.getPhase());
            verify(gameContext).showRoundStats(stats, game);
            return null;
        });
        final MagicCards without = fx(() -> {
            gameArea.getChildren().clear();
            final MagicCards noHearts = new MagicCards(gameContext, stats, MagicCardsMode.MAGIC,
                MagicCardsPictures.ANIMALS, MagicCardsCount.NINE, MagicCardsEnd.ENDLESS, 4);
            noHearts.setWithLives(false);
            noHearts.setManual();
            launchedGames.add(noHearts).launch();
            noHearts.step(0.02);
            return noHearts;
        });
        fx(() -> {
            assertFalse(without.hasLives());
            untilPlay(without);
            // The 5 cards without a picture, all wrong: never lost
            for (int mistake = 1; mistake <= 5; mistake++) {
                without.choose(wrongCard(without));
                steps(without, MagicCards.FLIP + 0.1);
            }
            assertEquals(MagicCards.Phase.PLAY, without.getPhase());
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheRoundsOfTheVariant() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.FOUR, MagicCardsEnd.PICTURES_3);
        fx(() -> {
            for (int round = 0; round < 3; round++) {
                untilPlay(game);
                for (final MagicCards.Card winner : List.copyOf(game.getWinners())) {
                    game.choose(winner);
                }
                steps(game, MagicCards.FLIP + MagicCards.GROW + MagicCards.REWARD + 0.1);
            }
            assertEquals(3, game.getFound());
            assertEquals(MagicCards.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.NINE, MagicCardsEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getCards().isEmpty());
            assertEquals(MagicCards.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Set<IGameVariant> variants = new MagicCardsGameVariantGenerator().getVariants();
        // One mode (the search removed on 17/11/2026), 2 kinds of pictures, each end with or without hearts, 4 counts
        assertEquals(2 * 4 * 2 * 4, variants.size());
        assertEquals(List.of(MagicCardsMode.MAGIC), List.of(MagicCardsMode.values()));
        assertTrue(MagicCardsGameLauncher.create(gameContext,
            new GroupedGameVariant("MagicShuffle", "MagicAnimals", "MagicCards9", null, "MagicEndless",
                LivesVariant.WITH), stats, 1).hasLives());
        // A variant of the search of before: played in the magic
        assertEquals(MagicCardsMode.MAGIC, MagicCardsGameLauncher.create(gameContext,
            new GroupedGameVariant("MagicSearch", "MagicAnimals", "MagicCards4", null, "MagicEnd3"), stats, 1)
            .getMode());
        assertTrue(MagicCardsGameLauncher.create(gameContext,
            new GroupedGameVariant("MagicShuffle", "MagicAnimals", "MagicCards9", null, "MagicEnd5",
                LivesVariant.WITH), stats, 1).hasLives());
        assertFalse(MagicCardsGameLauncher.create(gameContext,
            new GroupedGameVariant("MagicShuffle", "MagicAnimals", "MagicCards9", null, "MagicEnd5",
                LivesVariant.WITHOUT), stats, 1).hasLives());
        final MagicCards grouped = MagicCardsGameLauncher.create(gameContext,
            new GroupedGameVariant("MagicShuffle", "MagicMyImages", "MagicCards9", "images", "MagicEnd5"), stats, 1);
        assertEquals(MagicCardsMode.MAGIC, grouped.getMode());
        assertEquals(MagicCardsPictures.MY_IMAGES, grouped.getPictures());
        assertEquals(MagicCardsCount.NINE, grouped.getCount());
        assertEquals(MagicCardsEnd.PICTURES_5, grouped.getEnd());
        for (final int[] grid : List.of(new int[]{2, 2, 4}, new int[]{2, 3, 6}, new int[]{3, 2, 6}, new int[]{3, 3, 9})) {
            final MagicCards old = MagicCardsGameLauncher.create(gameContext,
                new DimensionGameVariant(grid[0], grid[1]), stats, 1);
            assertEquals(grid[2], old.getCount().getCards());
            assertEquals(MagicCardsMode.MAGIC, old.getMode());
            assertEquals(MagicCardsPictures.ANIMALS, old.getPictures());
            assertEquals(MagicCardsEnd.ENDLESS, old.getEnd());
        }
    }
}

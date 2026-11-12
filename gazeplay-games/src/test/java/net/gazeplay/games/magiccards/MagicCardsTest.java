package net.gazeplay.games.magiccards;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
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
            game.launch();
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
        return game.getCards().stream().filter(card -> card != game.getWinner() && !card.isTurned()).findFirst()
            .orElseThrow();
    }

    @Test
    void shouldPutEveryGridInTheScreenWithoutOverlap() throws Exception {
        for (final MagicCardsCount count : MagicCardsCount.values()) {
            final MagicCards game = launched(MagicCardsMode.SEARCH, count, MagicCardsEnd.ENDLESS);
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
        final MagicCards game = launched(MagicCardsMode.SEARCH, MagicCardsCount.FOUR, MagicCardsEnd.ENDLESS);
        fx(() -> {
            final MagicCards.Card card = wrongCard(game);
            // Nothing during the delay
            game.lookAt(card);
            assertNull(game.getLooked());
            steps(game, 1.02);
            assertEquals(MagicCards.Phase.PLAY, game.getPhase());
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

    @Test
    void shouldCountAWrongCardThatStaysTurnedOver() throws Exception {
        final MagicCards game = launched(MagicCardsMode.SEARCH, MagicCardsCount.SIX, MagicCardsEnd.ENDLESS);
        fx(() -> {
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

    @Test
    void shouldShowThePictureFoundThenAnotherRound() throws Exception {
        final MagicCards game = launched(MagicCardsMode.SEARCH, MagicCardsCount.FOUR, MagicCardsEnd.ENDLESS);
        fx(() -> {
            final MagicCards.Card winner = game.getWinner();
            game.choose(winner);
            assertEquals(MagicCards.Phase.REVEAL, game.getPhase());
            assertEquals(1, game.getFound());
            verify(stats).incrementNumberOfGoalsReached();
            steps(game, MagicCards.FLIP + MagicCards.GROW + 0.05);
            assertTrue(winner.isFaceUp());
            assertTrue(winner.getNode().getScaleY() > 1.5);
            assertEquals(2, game.getReward().getChildren().size());
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            game.getCards().stream().filter(card -> card != winner)
                .forEach(card -> assertEquals(0, card.getNode().getOpacity(), 1e-9));
            steps(game, MagicCards.REWARD);
            // Another round, all face down
            assertEquals(MagicCards.Phase.PLAY, game.getPhase());
            assertNotSame(winner, game.getWinner());
            assertTrue(game.getCards().stream().noneMatch(MagicCards.Card::isFaceUp));
            assertTrue(game.getReward().getChildren().isEmpty());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldShowThePictureThenShuffleTheCardsInTheMagicMode() throws Exception {
        final MagicCards game = launched(MagicCardsMode.MAGIC, MagicCardsCount.THREE, MagicCardsEnd.ENDLESS);
        fx(() -> {
            final MagicCards.Card winner = game.getWinner();
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

    @Test
    void shouldEndWithBravoAfterThePicturesOfTheVariant() throws Exception {
        final MagicCards game = launched(MagicCardsMode.SEARCH, MagicCardsCount.FOUR, MagicCardsEnd.PICTURES_3);
        fx(() -> {
            for (int round = 0; round < 3; round++) {
                game.choose(game.getWinner());
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
        assertEquals(2 * 2 * 4 * 4, variants.size());
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
            assertEquals(MagicCardsMode.SEARCH, old.getMode());
            assertEquals(MagicCardsPictures.ANIMALS, old.getPictures());
            assertEquals(MagicCardsEnd.ENDLESS, old.getEnd());
        }
    }
}

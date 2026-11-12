package net.gazeplay.games.scratchcard;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
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

import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Cartes à gratter" (reviewed on 12/11/2026, maintainer's rules): the start delay, the coating scratched all along the
 * way under the coin, the scraper lifted, the part scratched, the card uncovered at 90 % (melt, reward, next card), the
 * goals, the end,
 * the variants and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class ScratchCardTest {

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
        when(configuration.getGameStartDelay()).thenReturn(0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private ScratchCard launched(final ScratchCardBrush brush, final ScratchCardEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final ScratchCard game = new ScratchCard(gameContext, stats, ScratchCardPictures.ANIMALS, brush, end, 5);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static boolean clear(final ScratchCard game, final double x, final double y) {
        return game.getCoatingImage().getPixelReader().getColor((int) x, (int) y).getOpacity() < 0.05;
    }

    /**
     * Scratches the whole card, row by row, with the mouse (little smoothing), until it is uncovered.
     */
    private static void scratchAll(final ScratchCard game) {
        final double w = game.getCoatingImage().getWidth();
        final double h = game.getCoatingImage().getHeight();
        final double r = game.getRadius();
        for (double y = r / 2; y < h && game.getPhase() == ScratchCard.Phase.PLAY; y += r) {
            game.step(1);
            for (double x = 0; x <= w && game.getPhase() == ScratchCard.Phase.PLAY; x += r / 2) {
                game.scratch(game.getMousePen(), x, y, 1);
            }
        }
    }

    @Test
    void shouldScratchNothingDuringTheStartDelay() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final ScratchCard game = launched(ScratchCardBrush.MEDIUM, ScratchCardEnd.ENDLESS);
        fx(() -> {
            assertEquals(ScratchCard.Phase.WAIT, game.getPhase());
            game.scratch(game.getGazePen(), 200, 200, ScratchCard.GAZE_SMOOTHING);
            assertEquals(0, game.getScratchedCells());
            for (int i = 0; i < 60; i++) {
                game.step(0.02);
            }
            assertEquals(ScratchCard.Phase.PLAY, game.getPhase());
            game.scratch(game.getGazePen(), 200, 200, ScratchCard.GAZE_SMOOTHING);
            assertTrue(game.getScratchedCells() > 0);
            assertTrue(clear(game, 200, 200));
            return null;
        });
    }

    @Test
    void shouldScratchAllAlongTheWaySmoothed() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.SMALL, ScratchCardEnd.ENDLESS);
        fx(() -> {
            final ScratchCard.Pen pen = game.getGazePen();
            game.scratch(pen, 100, 200, ScratchCard.GAZE_SMOOTHING);
            game.scratch(pen, 300, 200, ScratchCard.GAZE_SMOOTHING);
            // Only part of the way, but all along it
            assertEquals(100 + 200 * ScratchCard.GAZE_SMOOTHING, pen.x, 1e-9);
            assertTrue(clear(game, 130, 200));
            assertTrue(clear(game, 160, 200));
            assertFalse(clear(game, 260, 200));
            // The coating stays under the scraper's way: the scraper is round, not an oval
            final double r = game.getRadius();
            assertTrue(clear(game, 100, 200 + r * 0.9));
            assertFalse(clear(game, 100, 200 + r * 1.2));
            return null;
        });
    }

    @Test
    void shouldLiftTheScraperWhenTheSourceIsSilent() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.SMALL, ScratchCardEnd.ENDLESS);
        fx(() -> {
            final ScratchCard.Pen pen = game.getGazePen();
            game.scratch(pen, 100, 200, ScratchCard.GAZE_SMOOTHING);
            game.step(ScratchCard.LIFT_AFTER + 0.05);
            game.scratch(pen, 600, 200, ScratchCard.GAZE_SMOOTHING);
            assertEquals(600, pen.x, 1e-9);
            assertFalse(clear(game, 350, 200));
            return null;
        });
    }

    @Test
    void shouldUncoverTheCardAtNinetyPercentThenShowAnother() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.LARGE, ScratchCardEnd.ENDLESS);
        fx(() -> {
            verify(stats, times(1)).incrementNumberOfGoalsToReach();
            assertEquals(0.9, ScratchCard.TO_UNCOVER, 1e-9);
            scratchAll(game);
            assertEquals(ScratchCard.Phase.REVEAL, game.getPhase());
            assertTrue(game.scratchedPart() >= ScratchCard.TO_UNCOVER);
            assertTrue(game.scratchedPart() < 1, "not the whole card: " + game.scratchedPart());
            assertFalse(game.getCoin().isVisible());
            assertEquals(1, game.getUncovered());
            verify(stats, times(1)).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/scratchcard/sounds/reveal.wav");
            // Nothing more is scratched: the rest melts
            final int cells = game.getScratchedCells();
            game.scratch(game.getMousePen(), 10, 10, 1);
            assertEquals(cells, game.getScratchedCells());
            game.step(ScratchCard.MELT / 2);
            assertEquals(0.5, game.getCoating().getOpacity(), 0.01);
            assertTrue(game.getReward().getChildren().isEmpty());
            game.step(ScratchCard.MELT / 2 + 0.01);
            assertEquals(0, game.getCoating().getOpacity(), 1e-9);
            assertEquals(2, game.getReward().getChildren().size());
            for (int i = 0; i < 25; i++) {
                game.step(0.1);
            }
            // Another card, all covered
            assertEquals(ScratchCard.Phase.PLAY, game.getPhase());
            assertEquals(0, game.getScratchedCells());
            assertEquals(1, game.getCoating().getOpacity(), 1e-9);
            assertTrue(game.getReward().getChildren().isEmpty());
            assertTrue(game.getCoin().isVisible());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldKeepTheCardCoveredAtSixtyPercent() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.LARGE, ScratchCardEnd.ENDLESS);
        fx(() -> {
            final double w = game.getCoatingImage().getWidth();
            final double h = game.getCoatingImage().getHeight();
            final double r = game.getRadius();
            for (double y = r / 2; y < h && game.scratchedPart() < 0.6; y += r) {
                game.step(1);
                for (double x = 0; x <= w; x += r / 2) {
                    game.scratch(game.getMousePen(), x, y, 1);
                }
            }
            assertTrue(game.scratchedPart() >= 0.6 && game.scratchedPart() < ScratchCard.TO_UNCOVER);
            assertEquals(ScratchCard.Phase.PLAY, game.getPhase());
            assertEquals(0, game.getUncovered());
            return null;
        });
    }

    @Test
    void shouldMoveTheCoinWithTheScraper() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.MEDIUM, ScratchCardEnd.ENDLESS);
        fx(() -> {
            final double left = game.getCoating().getLayoutX();
            final double top = game.getCoating().getLayoutY();
            final double w = game.getCoatingImage().getWidth();
            final double h = game.getCoatingImage().getHeight();
            // It waits in the middle of the card
            assertTrue(game.getCoin().isVisible());
            assertEquals(left + w / 2, game.getCoin().getLayoutX(), 1e-9);
            assertEquals(top + h / 2, game.getCoin().getLayoutY(), 1e-9);
            final ScratchCard.Pen pen = game.getGazePen();
            game.scratch(pen, 100, 150, ScratchCard.GAZE_SMOOTHING);
            assertEquals(left + 100, game.getCoin().getLayoutX(), 1e-9);
            assertEquals(top + 150, game.getCoin().getLayoutY(), 1e-9);
            game.scratch(pen, 300, 150, ScratchCard.GAZE_SMOOTHING);
            // Where the scraper is, smoothed, and as large as it
            assertEquals(left + pen.x, game.getCoin().getLayoutX(), 1e-9);
            assertEquals(2 * game.getRadius(), game.getCoin().getChildren().get(0).getBoundsInLocal().getWidth(), 1);
            return null;
        });
    }

    @Test
    void shouldPlayTheScratchSoundNowAndThen() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.SMALL, ScratchCardEnd.ENDLESS);
        fx(() -> {
            for (int x = 100; x < 400; x += 20) {
                game.scratch(game.getMousePen(), x, 200, 1);
                game.step(0.02);
            }
            // 0.3 s of scratching: a sound every 0.2 s
            verify(gameContext.getSoundManager(), times(2)).add("data/scratchcard/sounds/scratch.wav");
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheCardsOfTheVariant() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.LARGE, ScratchCardEnd.CARDS_3);
        fx(() -> {
            for (int card = 0; card < 3; card++) {
                scratchAll(game);
                for (int i = 0; i < 30; i++) {
                    game.step(0.1);
                }
            }
            assertEquals(3, game.getUncovered());
            assertEquals(ScratchCard.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            verify(stats, times(3)).incrementNumberOfGoalsReached();
            verify(stats, times(3)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final ScratchCard game = launched(ScratchCardBrush.MEDIUM, ScratchCardEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(ScratchCard.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Set<IGameVariant> variants = new ScratchCardGameVariantGenerator().getVariants();
        assertEquals(2 * 4 * 3, variants.size());
        assertTrue(variants.stream().map(GroupedGameVariant.class::cast)
            .allMatch(v -> (v.getGroup().equals("ScratchMyImages")) == (v.getUserImagesFolder() != null)));
        final ScratchCard grouped = ScratchCardGameLauncher.create(gameContext,
            new GroupedGameVariant("ScratchMyImages", "ScratchEnd5", "ScratchBrushLarge", "images"), stats, 1);
        assertEquals(ScratchCardPictures.MY_IMAGES, grouped.getPictures());
        assertEquals(ScratchCardBrush.LARGE, grouped.getBrush());
        assertEquals(ScratchCardEnd.CARDS_5, grouped.getEnd());
        // Before the review: no variant
        final ScratchCard old = ScratchCardGameLauncher.create(gameContext, null, stats, 1);
        assertEquals(ScratchCardPictures.ANIMALS, old.getPictures());
        assertEquals(ScratchCardBrush.MEDIUM, old.getBrush());
        assertEquals(ScratchCardEnd.ENDLESS, old.getEnd());
    }
}

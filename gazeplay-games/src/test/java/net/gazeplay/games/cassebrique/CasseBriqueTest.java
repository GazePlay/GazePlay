package net.gazeplay.games.cassebrique;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
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
 * "Casse-brique" (reviewed on 3/11/2026, maintainer's rules): three lives, the win when all the bricks are broken
 * (once only: the loop of before started it again and again), the levels and their hard bricks, the paddle following
 * the mouse or the gaze inside the screen, the drawings and the variants.
 */
@ExtendWith(ApplicationExtension.class)
class CasseBriqueTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private CasseBrique launched(final CasseBriqueMap map, final CasseBriqueLevel level) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final CasseBrique game = new CasseBrique(gameContext, stats, map, level, CasseBriqueBackground.DRAWN, 5);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final CasseBrique game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    @Test
    void shouldLoseALifeWhenTheBallFallsThenLoseAfterThree() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.SMILEY, CasseBriqueLevel.EASY);
        fx(() -> {
            assertEquals(3, game.getLives());
            // A ball going down, far from the paddle
            game.aimAt(100);
            steps(game, 0.1);
            game.throwBall(game.getWidth() - 50, game.getHeight() * 0.8, 0, 1);
            steps(game, 1);
            assertEquals(2, game.getLives());
            assertTrue(game.getWaiting() > 0, "the ball back on the paddle");
            game.fall();
            game.fall();
            return null;
        });
        assertEquals(0, game.getLives());
        assertEquals(CasseBrique.Phase.OVER, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldSendTheBallBackWithThePaddle() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.SMILEY, CasseBriqueLevel.EASY);
        fx(() -> {
            game.aimAt(game.getWidth() / 2);
            steps(game, 0.1);
            game.throwBall(game.getWidth() / 2, game.getHeight() * 0.8, 0, 0.6);
            steps(game, 0.5);
            assertEquals(3, game.getLives(), "not fallen");
            assertTrue(game.getBallY() < game.getHeight() * 0.85, "going up again");
            return null;
        });
    }

    /**
     * The ball goes up to the top of the screen before it bounces (it used to bounce under the lives and the score).
     */
    @Test
    void shouldBounceOnTheTopOfTheScreen() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.SMILEY, CasseBriqueLevel.EASY);
        fx(() -> {
            // Straight up in a column without bricks (the smile has holes)
            final double margin = game.getHeight() * 0.03;
            double x = margin;
            while (x < game.getWidth() - margin) {
                final double cx = x;
                if (game.getBricks().stream().noneMatch(b -> b.getX() - margin < cx
                    && cx < b.getX() + b.getWidth() + margin)) {
                    break;
                }
                x += 2;
            }
            assertTrue(x < game.getWidth() - margin, "a column without bricks");
            game.throwBall(x, game.getHeight() * 0.5, 0, -1);
            double highest = game.getBallY();
            for (int i = 0; i < 200 && highest > 0; i++) {
                game.step(0.01);
                highest = Math.min(highest, game.getBallY());
            }
            assertTrue(highest < game.getHeight() * 0.03, "up to the top: " + highest);
            return null;
        });
    }

    @Test
    void shouldBreakTheBricksAndWinOnceWhenAllAreBroken() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.SMILEY, CasseBriqueLevel.NORMAL);
        fx(() -> {
            final int total = game.getToBreak();
            assertEquals(13, total, "the bricks of the smile");
            for (final CasseBrique.Brick brick : java.util.List.copyOf(game.getBricks())) {
                game.hit(brick);
            }
            assertEquals(total, game.getBroken());
            // Steps after the win: no other win
            steps(game, 1);
            return null;
        });
        assertEquals(CasseBrique.Phase.OVER, game.getPhase());
        verify(gameContext, times(1)).playWinTransition(anyLong(), any());
        verify(stats, times(13)).incrementNumberOfGoalsToReach();
        verify(stats, times(13)).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldHaveBricksToHitTwiceInTheHardLevelOnly() throws Exception {
        final CasseBrique hard = launched(CasseBriqueMap.BLOC, CasseBriqueLevel.HARD);
        fx(() -> {
            final CasseBrique.Brick strong = hard.getBricks().stream().filter(b -> b.getHits() == 2).findFirst()
                .orElseThrow();
            hard.hit(strong);
            assertTrue(hard.getBricks().contains(strong), "cracked, not broken");
            hard.hit(strong);
            assertFalse(hard.getBricks().contains(strong));
            return null;
        });
        final CasseBrique easy = launched(CasseBriqueMap.BLOC, CasseBriqueLevel.EASY);
        assertTrue(easy.getBricks().stream().allMatch(b -> b.getHits() == 1));
        assertTrue(easy.getPaddleWidth() > hard.getPaddleWidth());
        assertTrue(CasseBriqueLevel.EASY.getBallSpeed() < CasseBriqueLevel.HARD.getBallSpeed());
    }

    @Test
    void shouldBreakABrickHitByTheBall() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.BLOC, CasseBriqueLevel.NORMAL);
        fx(() -> {
            final CasseBrique.Brick low = game.getBricks().stream()
                .max(java.util.Comparator.comparingDouble(CasseBrique.Brick::getY)).orElseThrow();
            game.throwBall(low.getX() + 20, game.getHeight() * 0.8, 0, -0.75);
            steps(game, 1.5);
            assertFalse(game.getBricks().contains(low), "broken");
            return null;
        });
        assertTrue(game.getBroken() >= 1);
    }

    @Test
    void shouldFollowTheMouseInsideTheScreen() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.BLOC, CasseBriqueLevel.EASY);
        fx(() -> {
            final Node layer = gameArea.getChildren().get(0);
            Event.fireEvent(layer, new MouseEvent(MouseEvent.MOUSE_MOVED, 400, 300, 400, 300, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null));
            game.step(0.02);
            assertEquals(400, game.getPaddleX(), 1e-9);
            game.aimAt(5000);
            game.step(0.02);
            assertEquals(game.getWidth() - game.getPaddleWidth() / 2, game.getPaddleX(), 1e-9, "inside the screen");
            return null;
        });
    }

    @Test
    void shouldGiveTheVariantsWithMyImagesAndReadOnesOfBefore() {
        final Set<IGameVariant> variants = new CasseBriqueGameVariantGenerator().getVariants();
        assertEquals(CasseBriqueBackground.values().length * CasseBriqueLevel.values().length
            * CasseBriqueMap.values().length, variants.size());
        final GroupedGameVariant mine = new GroupedGameVariant(CasseBriqueBackground.MY_IMAGES.getKey(),
            CasseBriqueLevel.HARD.getKey(), CasseBriqueMap.ARC.getKey(), CasseBriqueBackground.USER_IMAGES_FOLDER);
        assertTrue(variants.contains(mine));
        assertEquals("images", UserImagesVariant.folderOf(mine), "the images shared by all the games");
        assertEquals(CasseBriqueMap.ARC, CasseBriqueGameLauncher.mapOf(mine));
        assertEquals(CasseBriqueLevel.HARD, CasseBriqueGameLauncher.levelOf(mine));
        assertEquals(CasseBriqueBackground.MY_IMAGES, CasseBriqueGameLauncher.backgroundOf(mine));
        final IGameVariant old = new IntGameVariant(1);
        assertEquals(CasseBriqueMap.BLOC, CasseBriqueGameLauncher.mapOf(old));
        assertEquals(CasseBriqueLevel.NORMAL, CasseBriqueGameLauncher.levelOf(old));
        assertEquals(CasseBriqueBackground.DRAWN, CasseBriqueGameLauncher.backgroundOf(null));
        for (final CasseBriqueMap map : CasseBriqueMap.values()) {
            final int length = map.getRows()[0].length();
            for (final String row : map.getRows()) {
                assertEquals(length, row.length(), map + " has rows of one length");
            }
        }
    }

    /**
     * "My images": a light veil hides the image of the user and fades as the bricks break (maintainer's request of
     * 3/11/2026).
     */
    @Test
    void shouldFadeTheVeilOverTheImageOfTheUserAsTheBricksBreak() throws Exception {
        final java.io.File folder = UserImagesVariant.directory(CasseBriqueBackground.USER_IMAGES_FOLDER);
        final java.io.File image = new java.io.File(folder, "test-casse-brique.png");
        try (java.io.InputStream in = getClass().getClassLoader()
            .getResourceAsStream("data/surviveAgainstRobots/images/sky.png")) {
            java.nio.file.Files.copy(in, image.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            final CasseBrique game = fx(() -> {
                gameArea.getChildren().clear();
                final CasseBrique g = new CasseBrique(gameContext, stats, CasseBriqueMap.SMILEY,
                    CasseBriqueLevel.NORMAL, CasseBriqueBackground.MY_IMAGES, 5);
                g.setManual();
                g.launch();
                return g;
            });
            fx(() -> {
                assertNotNull(game.getVeil());
                assertEquals(CasseBrique.VEIL_OPACITY, game.getVeil().getOpacity(), 1e-9);
                final java.util.List<CasseBrique.Brick> all = java.util.List.copyOf(game.getBricks());
                for (int i = 0; i < all.size() / 2; i++) {
                    game.hit(all.get(i));
                }
                final double half = game.getVeil().getOpacity();
                assertTrue(half < CasseBrique.VEIL_OPACITY && half > 0, "fading: " + half);
                for (int i = all.size() / 2; i < all.size(); i++) {
                    game.hit(all.get(i));
                }
                assertEquals(0, game.getVeil().getOpacity(), 1e-9, "the image whole at the end");
                return null;
            });
            // Without images of the user, no veil
            assertNull(launched(CasseBriqueMap.SMILEY, CasseBriqueLevel.NORMAL).getVeil());
        } finally {
            assertTrue(image.delete());
        }
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final CasseBrique game = launched(CasseBriqueMap.BLOC, CasseBriqueLevel.HARD);
        fx(() -> {
            steps(game, 4);
            game.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
        assertTrue(game.getBricks().isEmpty());
    }
}

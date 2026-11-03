package net.gazeplay.games.space;

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
 * "Space Game" (reviewed on 3/11/2026, maintainer's rules): three lives, the boss after the time or the aliens
 * destroyed (15, 30 or 50 points of life), none without end, the rocket following the mouse or the gaze, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class SpaceGameTest {

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

    private SpaceGame launched(final SpaceLevel level, final SpaceEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final SpaceGame game = new SpaceGame(gameContext, stats, level, end, 5);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final SpaceGame game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    @Test
    void shouldLoseALifeWhenTouchedThenBlinkAndLoseAfterThreeLives() throws Exception {
        final SpaceGame game = launched(SpaceLevel.EASY, SpaceEnd.ENDLESS);
        fx(() -> {
            game.hurt();
            game.hurt();
            return null;
        });
        assertEquals(2, game.getLives(), "blinking: nothing touches it");
        fx(() -> {
            game.getShots().clear();
            game.getAliens().clear();
            steps(game, SpaceGame.INVINCIBLE + 0.1);
            game.hurt();
            return null;
        });
        assertEquals(1, game.getLives());
        fx(() -> {
            steps(game, SpaceGame.INVINCIBLE + 0.1);
            game.hurt();
            return null;
        });
        assertEquals(0, game.getLives());
        assertEquals(SpaceGame.Phase.OVER, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldShootUpAndDestroyAnAlien() throws Exception {
        final SpaceGame game = launched(SpaceLevel.EASY, SpaceEnd.ENDLESS);
        fx(() -> {
            game.getAliens().clear();
            game.addAlien(game.getShipX(), game.getHeight() * 0.3);
            steps(game, 1.5);
            return null;
        });
        assertTrue(game.getDestroyed() >= 1, "the alien above the rocket is shot");
        verify(stats, atLeastOnce()).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldCallTheBossAfterTheAliensThenWinWhenItIsBeaten() throws Exception {
        for (final SpaceLevel level : SpaceLevel.values()) {
            final SpaceGame game = launched(level, SpaceEnd.ALIENS_20);
            fx(() -> {
                for (int i = 0; i < 19; i++) {
                    game.destroy(game.addAlien(100, 300));
                }
                return null;
            });
            assertEquals(SpaceGame.Phase.PLAY, game.getPhase());
            assertNull(game.getBoss());
            fx(() -> {
                game.destroy(game.addAlien(100, 300));
                return null;
            });
            assertEquals(SpaceGame.Phase.BOSS, game.getPhase());
            assertTrue(game.getAliens().isEmpty(), "the other aliens go");
            final SpaceGame.Boss boss = game.getBoss();
            assertEquals(level.getBossLife(), boss.getLife());
            fx(() -> {
                // Not hit while it comes
                game.hit(boss);
                assertEquals(level.getBossLife(), boss.getLife());
                steps(game, SpaceGame.BOSS_ARRIVAL + 0.1);
                for (int i = 0; i < level.getBossLife() - 1; i++) {
                    game.hit(boss);
                }
                return null;
            });
            assertEquals(SpaceGame.Phase.BOSS, game.getPhase());
            fx(() -> {
                game.hit(boss);
                return null;
            });
            assertEquals(SpaceGame.Phase.OVER, game.getPhase());
        }
        verify(gameContext, times(3)).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldCallTheBossAfterOneMinuteAndNeverWithoutEnd() throws Exception {
        final SpaceGame timed = launched(SpaceLevel.EASY, SpaceEnd.TIME_1);
        fx(() -> {
            steps(timed, 59);
            assertNull(timed.getBoss());
            // The lives do not matter here
            steps(timed, 1.5);
            return null;
        });
        assertTrue(timed.getPhase() != SpaceGame.Phase.PLAY);
        if (timed.getPhase() == SpaceGame.Phase.BOSS) {
            assertNotNull(timed.getBoss());
        }

        final SpaceGame endless = launched(SpaceLevel.EASY, SpaceEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < 60; i++) {
                endless.destroy(endless.addAlien(100, 300));
            }
            endless.callTheBoss();
            return null;
        });
        assertNull(endless.getBoss(), "no boss without end");
        assertEquals(SpaceGame.Phase.PLAY, endless.getPhase());
    }

    @Test
    void shouldBringTheAliensOfTheLevel() throws Exception {
        for (final SpaceLevel level : SpaceLevel.values()) {
            final SpaceGame game = launched(level, SpaceEnd.ENDLESS);
            fx(() -> {
                steps(game, 10);
                return null;
            });
            assertTrue(game.getAliens().size() <= level.getAliens(), level.name());
        }
        assertEquals(15, SpaceLevel.EASY.getBossLife());
        assertEquals(30, SpaceLevel.NORMAL.getBossLife());
        assertEquals(50, SpaceLevel.HARD.getBossLife());
    }

    @Test
    void shouldFollowTheMouseAlongTheBottom() throws Exception {
        final SpaceGame game = launched(SpaceLevel.EASY, SpaceEnd.ENDLESS);
        final double y = game.getShipY();
        fx(() -> {
            final Node layer = gameArea.getChildren().get(0);
            Event.fireEvent(layer, new MouseEvent(MouseEvent.MOUSE_MOVED, 300, 100, 300, 100, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null));
            steps(game, 2);
            return null;
        });
        assertEquals(300, game.getShipX(), 5);
        assertEquals(y, game.getShipY(), 0.001, "it stays at the bottom");
        fx(() -> {
            game.aimAt(1100);
            steps(game, 2);
            return null;
        });
        assertEquals(1100, game.getShipX(), 5);
    }

    @Test
    void shouldGiveTheVariantsAndReadOnesOfBefore() {
        final Set<IGameVariant> variants = new SpaceGameVariantGenerator().getVariants();
        assertEquals(SpaceLevel.values().length * SpaceEnd.values().length, variants.size());
        final GroupedGameVariant variant = new GroupedGameVariant(SpaceGameVariantGenerator.GROUP,
            SpaceLevel.HARD.getKey(), SpaceEnd.TIME_3.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(SpaceLevel.HARD, SpaceGameLauncher.levelOf(variant));
        assertEquals(SpaceEnd.TIME_3, SpaceGameLauncher.endOf(variant));
        assertEquals(SpaceLevel.NORMAL, SpaceGameLauncher.levelOf(new IntGameVariant(1)));
        assertEquals(SpaceEnd.ENDLESS, SpaceGameLauncher.endOf(null));
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final SpaceGame game = launched(SpaceLevel.HARD, SpaceEnd.ENDLESS);
        fx(() -> {
            steps(game, 5);
            game.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
        assertTrue(game.getAliens().isEmpty());
        assertTrue(game.getShots().isEmpty());
    }
}

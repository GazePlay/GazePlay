package net.gazeplay.games.surviveAgainstRobots;

import javafx.geometry.Dimension2D;
import javafx.event.Event;
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
 * "Survive Against Robots" (reviewed on 3/11/2026, maintainer's rules): three lives, the ends (never, 1 or 3 minutes,
 * 20 or 50 robots), the robots coming and shot, the bonuses, the gaze or the mouse, the shots forwards, the scenes, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class SurviveAgainstRobotsTest {

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

    private SurviveAgainstRobots launched(final SurviveLevel level, final SurviveShot shot,
                                          final SurviveEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final SurviveAgainstRobots game = new SurviveAgainstRobots(gameContext, stats, level, shot, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private SurviveAgainstRobots launched(final SurviveLevel level, final SurviveShot shot,
                                          final SurviveEnd end) throws Exception {
        return launched(level, shot, end, 5);
    }

    private static void steps(final SurviveAgainstRobots game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    @Test
    void shouldLoseALifeWhenTouchedThenBlinkAndLoseAfterThreeLives() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC, SurviveEnd.ENDLESS);
        assertEquals(3, game.getLives());
        fx(() -> {
            game.hurt();
            return null;
        });
        assertEquals(2, game.getLives());
        assertTrue(game.getInvincible() > 0);
        // Blinking: nothing touches it
        fx(() -> {
            game.hurt();
            return null;
        });
        assertEquals(2, game.getLives());
        fx(() -> {
            steps(game, SurviveAgainstRobots.INVINCIBLE + 0.1);
            game.hurt();
            steps(game, SurviveAgainstRobots.INVINCIBLE + 0.1);
            return null;
        });
        assertEquals(1, game.getLives());
        assertEquals(SurviveAgainstRobots.Phase.PLAY, game.getPhase());
        fx(() -> {
            game.hurt();
            return null;
        });
        assertEquals(0, game.getLives());
        assertEquals(SurviveAgainstRobots.Phase.OVER, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldLoseALifeWhenARobotTouchesTheBiboule() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC, SurviveEnd.ENDLESS);
        fx(() -> {
            game.addRobot(game.getPlayerX(), game.getPlayerY(), false);
            // Harmless while it comes
            game.step(0.02);
            assertEquals(3, game.getLives());
            steps(game, SurviveAgainstRobots.APPEARING + 0.1);
            return null;
        });
        assertEquals(2, game.getLives());
    }

    @Test
    void shouldShootTheNearestRobotByItselfAndCountIt() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC,
            SurviveEnd.ROBOTS_20);
        fx(() -> {
            game.getRobots().clear();
            game.addRobot(game.getPlayerX() + game.getHeight() * 0.4, game.getPlayerY(), false);
            steps(game, 3);
            return null;
        });
        assertTrue(game.getDestroyed() >= 1, "the robot is shot");
        verify(stats, atLeastOnce()).incrementNumberOfGoalsReached();
        verify(stats, atLeastOnce()).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldEndWithBravoAfterTheRobotsToDestroy() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC,
            SurviveEnd.ROBOTS_20);
        fx(() -> {
            for (int i = 0; i < 19; i++) {
                game.destroy(game.addRobot(100, 300, false));
            }
            return null;
        });
        assertEquals(19, game.getDestroyed());
        assertEquals(SurviveAgainstRobots.Phase.PLAY, game.getPhase());
        fx(() -> {
            game.destroy(game.addRobot(100, 300, false));
            return null;
        });
        assertEquals(SurviveAgainstRobots.Phase.OVER, game.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndWithBravoAfterOneMinuteAndNeverWithoutEnd() throws Exception {
        final SurviveAgainstRobots timed = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC,
            SurviveEnd.TIME_1);
        fx(() -> {
            steps(timed, 59);
            return null;
        });
        assertEquals(SurviveAgainstRobots.Phase.PLAY, timed.getPhase());
        fx(() -> {
            steps(timed, 1.5);
            return null;
        });
        assertEquals(SurviveAgainstRobots.Phase.OVER, timed.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());

        final SurviveAgainstRobots endless = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC,
            SurviveEnd.ENDLESS);
        fx(() -> {
            steps(endless, 120);
            return null;
        });
        assertEquals(SurviveAgainstRobots.Phase.PLAY, endless.getPhase());
    }

    @Test
    void shouldBringMoreRobotsUpToTheLevelAndOnlyRedShootersOnHard() throws Exception {
        final SurviveAgainstRobots easy = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC,
            SurviveEnd.ENDLESS);
        assertEquals(1, easy.wantedRobots());
        fx(() -> {
            for (int i = 0; i < 30; i++) {
                easy.destroy(easy.addRobot(100, 300, false));
            }
            steps(easy, 5);
            return null;
        });
        assertEquals(2, easy.wantedRobots());
        assertTrue(easy.getRobots().size() <= 2);
        assertTrue(easy.getRobots().stream().noneMatch(SurviveAgainstRobots.Robot::isShooter), "none shoots");

        final SurviveAgainstRobots hard = launched(SurviveLevel.HARD, SurviveShot.AUTOMATIC,
            SurviveEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < 30; i++) {
                hard.destroy(hard.addRobot(100, 300, false));
            }
            return null;
        });
        assertEquals(4, hard.wantedRobots());
    }

    @Test
    void shouldHurtTheBibouleWithTheShotsOfARedRobot() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.HARD, SurviveShot.FORWARD,
            SurviveEnd.ENDLESS);
        fx(() -> {
            // On the left: the Biboule shoots forwards, to the right, and does not destroy it
            game.addRobot(game.getPlayerX() - game.getHeight() * 0.5, game.getPlayerY(), true);
            steps(game, 4);
            return null;
        });
        assertTrue(game.getLives() < 3, "hit by a shot");
    }

    @Test
    void shouldTakeTheBonuses() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC, SurviveEnd.ENDLESS);
        fx(() -> {
            game.hurt();
            steps(game, SurviveAgainstRobots.INVINCIBLE + 0.1);
            game.addBonus(SurviveAgainstRobots.BonusKind.HEART, game.getPlayerX(), game.getPlayerY());
            game.step(0.02);
            return null;
        });
        assertEquals(3, game.getLives());
        fx(() -> {
            game.addBonus(SurviveAgainstRobots.BonusKind.SHIELD, game.getPlayerX(), game.getPlayerY());
            game.step(0.02);
            game.hurt();
            return null;
        });
        assertEquals(3, game.getLives(), "the shield protects");
        assertTrue(game.getEffects().containsKey(SurviveAgainstRobots.BonusKind.SHIELD));
        fx(() -> {
            steps(game, SurviveAgainstRobots.BONUS_EFFECT + 0.1);
            return null;
        });
        assertFalse(game.getEffects().containsKey(SurviveAgainstRobots.BonusKind.SHIELD));
        // A bonus not taken goes away
        final SurviveAgainstRobots.Bonus fire = fx(() -> {
            final SurviveAgainstRobots.Bonus bonus = game.addBonus(SurviveAgainstRobots.BonusKind.FIRE, 100, 700);
            steps(game, SurviveAgainstRobots.BONUS_LIFETIME - 1);
            assertTrue(game.getBonuses().contains(bonus));
            steps(game, 1.1);
            return bonus;
        });
        assertFalse(game.getBonuses().contains(fire));
    }

    @Test
    void shouldFollowTheMouseOrTheGazeAndShootForwards() throws Exception {
        // The mouse moving on the game
        final SurviveAgainstRobots mouse = launched(SurviveLevel.EASY, SurviveShot.FORWARD, SurviveEnd.ENDLESS);
        fx(() -> {
            final Node layer = gameArea.getChildren().get(0);
            Event.fireEvent(layer, new MouseEvent(MouseEvent.MOUSE_MOVED, 1100, 300, 1100, 300, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null));
            steps(mouse, 3);
            return null;
        });
        assertEquals(1100, mouse.getPlayerX(), 15);
        assertEquals(300, mouse.getPlayerY(), 15);

        // The gaze (or the last that moved)
        final SurviveAgainstRobots gaze = launched(SurviveLevel.EASY, SurviveShot.FORWARD, SurviveEnd.ENDLESS);
        fx(() -> {
            gaze.aimAt(200, 600);
            steps(gaze, 3);
            return null;
        });
        assertEquals(200, gaze.getPlayerX(), 15);
        assertEquals(600, gaze.getPlayerY(), 15);
        // Got there, the gaze on it: it still shoots, forwards (the way it came: to the left)
        final List<SurviveAgainstRobots.Shot> sparkles = fx(() -> {
            gaze.getRobots().clear();
            gaze.getShots().clear();
            steps(gaze, 0.6);
            return gaze.getShots().stream().filter(SurviveAgainstRobots.Shot::isFromPlayer).toList();
        });
        assertFalse(sparkles.isEmpty());
        assertTrue(sparkles.stream().allMatch(shot -> shot.getX() < 200), "forwards, to the left");
    }

    @Test
    void shouldDrawTheSceneAtRandomAmongTheThree() throws Exception {
        final Set<String> scenes = new HashSet<>();
        // Seeds like those of the clock (small ones all give the same first number)
        for (int i = 1; i <= 30; i++) {
            scenes.add(launched(SurviveLevel.EASY, SurviveShot.AUTOMATIC, SurviveEnd.ENDLESS, 12_345_678.0 * i)
                .getScene());
        }
        assertEquals(Set.of(SurviveAgainstRobots.SCENES), scenes);
    }

    @Test
    void shouldGiveTheVariantsAndReadOnesOfBefore() {
        final Set<IGameVariant> variants = new SurviveAgainstRobotsVariantGenerator().getVariants();
        assertEquals(SurviveLevel.values().length * SurviveShot.values().length * SurviveEnd.values().length,
            variants.size());
        final GroupedGameVariant variant = new GroupedGameVariant(SurviveLevel.HARD.getKey(),
            SurviveShot.FORWARD.getKey(), SurviveEnd.ROBOTS_50.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(SurviveLevel.HARD, SurviveAgainstRobotsLauncher.levelOf(variant));
        assertEquals(SurviveShot.FORWARD, SurviveAgainstRobotsLauncher.shotOf(variant));
        assertEquals(SurviveEnd.ROBOTS_50, SurviveAgainstRobotsLauncher.endOf(variant));
        for (final IGameVariant old : List.of(new IntGameVariant(1))) {
            assertEquals(SurviveLevel.NORMAL, SurviveAgainstRobotsLauncher.levelOf(old));
            assertEquals(SurviveShot.AUTOMATIC, SurviveAgainstRobotsLauncher.shotOf(old));
            assertEquals(SurviveEnd.ENDLESS, SurviveAgainstRobotsLauncher.endOf(old));
        }
        assertEquals(SurviveEnd.ENDLESS, SurviveAgainstRobotsLauncher.endOf(null));
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final SurviveAgainstRobots game = launched(SurviveLevel.HARD, SurviveShot.AUTOMATIC,
            SurviveEnd.ENDLESS);
        fx(() -> {
            steps(game, 5);
            game.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
        assertTrue(game.getRobots().isEmpty());
        assertTrue(game.getShots().isEmpty());
    }
}

package net.gazeplay.games.shooter;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The shooting game (reviewed on 2/11/2026, maintainer's rules): the ends (never, 50 or 100 targets, 1 or 3 minutes),
 * shot at once or after a fixation, the targets flying out of the box, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class ShooterGameTest {

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
        when(configuration.getFixationLength()).thenReturn(150);
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private ShooterGame launched(final ShooterTheme theme, final ShooterShot shot, final ShooterEnd end)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final ShooterGame game = new ShooterGame(gameContext, stats, theme, shot, end, 5);
            game.setManual();
            game.launch();
            return game;
        });
    }

    /**
     * Shoots targets one after the other (each brought out of the box first).
     */
    private void shootTargets(final ShooterGame game, final int count) throws Exception {
        fx(() -> {
            for (int i = 0; i < count; i++) {
                final ShooterGame.Target target = game.getTargets().get(i % ShooterGame.TARGETS);
                target.start(0);
                game.shoot(target);
            }
            return null;
        });
    }

    /**
     * 50 targets: "Bravo" and the statistics at the 50th, not before.
     */
    @Test
    void shouldEndAfterTheTargetsOfTheVariant() throws Exception {
        final ShooterGame game = launched(ShooterTheme.ROBOTS, ShooterShot.AT_ONCE, ShooterEnd.SCORE_50);
        shootTargets(game, 49);
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        assertEquals(49, (int) fx(game::getScore));
        shootTargets(game, 1);
        verify(gameContext).playWinTransition(anyLong(), any());
        assertEquals(ShooterGame.Phase.OVER, fx(game::getPhase));
        verify(stats, times(50)).incrementNumberOfGoalsReached();
    }

    /**
     * Without end: still playing after 120 targets.
     */
    @Test
    void shouldNeverEndWithoutAnEnd() throws Exception {
        final ShooterGame game = launched(ShooterTheme.BIBOULES, ShooterShot.AT_ONCE, ShooterEnd.ENDLESS);
        shootTargets(game, 120);
        assertEquals(ShooterGame.Phase.PLAY, fx(game::getPhase));
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    /**
     * 1 minute: the time runs with the game, then "Bravo" and the statistics.
     */
    @Test
    void shouldEndAfterTheTimeOfTheVariant() throws Exception {
        final ShooterGame game = launched(ShooterTheme.BIBOULES, ShooterShot.AT_ONCE, ShooterEnd.TIME_1);
        fx(() -> {
            assertEquals(60, game.getTimeLeft(), 1e-9);
            for (int i = 0; i < 1180; i++) {
                game.step(0.05);
            }
            assertEquals(ShooterGame.Phase.PLAY, game.getPhase());
            for (int i = 0; i < 30; i++) {
                game.step(0.05);
            }
            assertEquals(ShooterGame.Phase.OVER, game.getPhase());
            return null;
        });
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    /**
     * Shot at once: the gaze entering a target shoots it. After a fixation: only once the fixation is over.
     */
    @Test
    void shouldShootAtOnceOrAfterAFixation() throws Exception {
        final ShooterGame atOnce = launched(ShooterTheme.BIBOULES, ShooterShot.AT_ONCE, ShooterEnd.ENDLESS);
        fx(() -> {
            atOnce.getTargets().getFirst().node().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            assertEquals(1, atOnce.getScore());
            return null;
        });
        final ShooterGame fixation = launched(ShooterTheme.ROBOTS, ShooterShot.FIXATION, ShooterEnd.ENDLESS);
        fx(() -> {
            final ShooterGame.Target target = fixation.getTargets().getFirst();
            target.node().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            assertEquals(0, fixation.getScore(), "not at once");
            final double x = target.getX();
            fixation.step(0.1);
            assertEquals(x, target.getX(), 1e-9, "it waits while looked at");
            return null;
        });
        WaitForAsyncUtils.sleep(450, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, (int) fx(fixation::getScore), "after the fixation");
    }

    /**
     * The targets fly out of the box and grow; the game removes all it showed at the end.
     */
    @Test
    void shouldSendTheTargetsOutOfTheBox() throws Exception {
        final ShooterGame game = launched(ShooterTheme.ROBOTS, ShooterShot.AT_ONCE, ShooterEnd.ENDLESS);
        fx(() -> {
            final ShooterGame.Target target = game.getTargets().getFirst();
            final double x = target.getX();
            final double y = target.getY();
            final double size = target.node().getBoundsInLocal().getWidth();
            game.step(1);
            assertTrue(Math.hypot(target.getX() - x, target.getY() - y) > 1, "it flies");
            assertTrue(target.node().getBoundsInLocal().getWidth() > size, "it grows");
            game.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
    }

    /**
     * 20 variants: 5 ends, for each scene and each way of shooting; without a variant, the game of before.
     */
    @Test
    void shouldOfferTheEndsTheScenesAndTheWaysOfShooting() {
        final List<IGameVariant> variants = new ArrayList<>(new ShooterGameVariantGenerator().getVariants());
        assertEquals(20, variants.size());
        final GroupedGameVariant first = (GroupedGameVariant) variants.getFirst();
        assertEquals("ShooterAtOnce", first.getGroup());
        assertEquals("ShooterBiboules", first.getSubGroup());
        assertEquals("ShooterEndless", first.getValue());
        assertEquals(ShooterEnd.TIME_3, ShooterGameLauncher.endOf(variants.get(4)));
        final IGameVariant none = new IntGameVariant(1);
        assertEquals(ShooterTheme.BIBOULES, ShooterGameLauncher.themeOf(none));
        assertEquals(ShooterShot.AT_ONCE, ShooterGameLauncher.shotOf(none));
        assertEquals(ShooterEnd.ENDLESS, ShooterGameLauncher.endOf(none));
        assertEquals(180, ShooterEnd.TIME_3.getSeconds());
        assertEquals(100, ShooterEnd.SCORE_100.getTargets());
    }

    /**
     * The music is delivered with the game (4/11/2026): it used to be downloaded, so that there was none without internet.
     */
    @Test
    void shouldHaveItsMusicWithoutInternet() throws Exception {
        final String music = new ShooterGameSpecSource().getGameSpec().getGameSummary().getBackgroundMusicUrl();
        org.junit.jupiter.api.Assertions.assertNotNull(music);
        org.junit.jupiter.api.Assertions.assertFalse(music.startsWith("http"), music);
        try (java.io.InputStream stream = new java.net.URI(music).toURL().openStream()) {
            org.junit.jupiter.api.Assertions.assertTrue(stream.read() >= 0, "the file can be read");
        }
    }
}

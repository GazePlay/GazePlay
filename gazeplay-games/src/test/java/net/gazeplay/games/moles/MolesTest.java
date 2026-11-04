package net.gazeplay.games.moles;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
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

import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The moles game (reviewed on 4/11/2026, maintainer's rules): the targets of the level out at the same time, a time out
 * always longer than the fixation, hit at once or after the fixation of the settings, the ends, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class MolesTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    /**
     * Read at each step: it keeps no trace of its calls.
     */
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
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getFixationLength()).thenReturn(500);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Moles launched(final MolesLevel level, final MolesHit hit, final MolesEnd end) throws Exception {
        return launched(MolesTarget.MOLES, level, hit, end);
    }

    private Moles launched(final MolesTarget target, final MolesLevel level, final MolesHit hit, final MolesEnd end)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Moles game = new Moles(gameContext, stats, target, level, hit, end, 5);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final Moles game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void stepUntilATargetIsOut(final Moles game) {
        for (int i = 0; i < 300 && firstOut(game) == null; i++) {
            game.step(0.02);
        }
        assertNotNull(firstOut(game), "a target comes out");
    }

    private static Moles.Hole firstOut(final Moles game) {
        return game.getHoles().stream().filter(hole -> hole.getState() == Moles.State.RISING
            || hole.getState() == Moles.State.UP).findFirst().orElse(null);
    }

    private static void enter(final Moles.Hole hole) {
        Event.fireEvent(hole.getZone(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
    }

    private static void exit(final Moles.Hole hole) {
        Event.fireEvent(hole.getZone(), new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
    }

    @Test
    void shouldHaveTheHolesAndTheTargetsOfTheLevel() throws Exception {
        for (final MolesLevel level : MolesLevel.values()) {
            final Moles game = launched(level, MolesHit.FIXATION, MolesEnd.ENDLESS);
            fx(() -> {
                assertEquals(level.getHoles(), game.getHoles().size());
                int most = 0;
                for (int i = 0; i < 1000; i++) {
                    game.step(0.02);
                    most = Math.max(most, game.targetsOut());
                }
                assertEquals(level.getTargets(), most, level + " at the same time");
                return null;
            });
        }
    }

    /**
     * The holes and the place of the targets are inside the screen, under nothing but the score.
     */
    @Test
    void shouldKeepTheHolesAndTheTargetsInsideTheScreen() throws Exception {
        for (final MolesLevel level : MolesLevel.values()) {
            final Moles game = launched(level, MolesHit.FIXATION, MolesEnd.ENDLESS);
            for (final Moles.Hole hole : game.getHoles()) {
                assertTrue(hole.getX() - hole.getSize() * 0.63 >= 0, level + " left");
                assertTrue(hole.getX() + hole.getSize() * 0.63 <= game.getWidth(), level + " right");
                assertTrue(hole.getY() - hole.getSize() >= 0, level + " top");
                assertTrue(hole.getY() + hole.getSize() * 0.27 <= game.getHeight(), level + " bottom");
            }
        }
    }

    /**
     * Maintainer's remark of 4/11/2026: on a hole of the row above, a target of the row below could be selected.
     */
    private static void assertZonesDoNotMeet(final Moles game) {
        final java.util.List<Moles.Hole> holes = game.getHoles();
        for (int i = 0; i < holes.size(); i++) {
            for (int j = i + 1; j < holes.size(); j++) {
                final javafx.scene.shape.Rectangle a = holes.get(i).getZone();
                final javafx.scene.shape.Rectangle b = holes.get(j).getZone();
                final double ax = holes.get(i).getX() + a.getX();
                final double ay = holes.get(i).getY() + a.getY();
                final double bx = holes.get(j).getX() + b.getX();
                final double by = holes.get(j).getY() + b.getY();
                final boolean meet = ax < bx + b.getWidth() && bx < ax + a.getWidth()
                    && ay < by + b.getHeight() && by < ay + a.getHeight();
                assertFalse(meet, game.getTarget() + " " + game.getLevel() + ": zones " + i + " and " + j);
            }
        }
    }

    @Test
    void shouldKeepTheZoneOfAHoleAwayFromTheOtherHoles() throws Exception {
        for (final MolesLevel level : MolesLevel.values()) {
            assertZonesDoNotMeet(launched(MolesTarget.MOLES, level, MolesHit.AT_ONCE, MolesEnd.ENDLESS));
            assertZonesDoNotMeet(launched(MolesTarget.BIBOULES, level, MolesHit.AT_ONCE, MolesEnd.ENDLESS));
        }
    }

    /**
     * It used to stay out 2 to 4.5 s, whatever the fixation: with 4.5 s or more in the settings, no target could be hit.
     */
    @Test
    void shouldStayOutForTheFixationAndTheTimeOfTheLevel() throws Exception {
        when(configuration.getFixationLength()).thenReturn(6000);
        final Moles game = launched(MolesLevel.EASY, MolesHit.FIXATION, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            final Moles.Hole hole = firstOut(game);
            steps(game, 6 + 3);
            assertNotEquals(Moles.State.FALLING, hole.getState(), "still out");
            steps(game, Moles.RISE + 0.1);
            assertNotEquals(Moles.State.UP, hole.getState(), "gone back after its time");
            return null;
        });
    }

    @Test
    void shouldStayOutForTheTimeOfTheLevelOnlyWhenHitAtOnce() throws Exception {
        when(configuration.getFixationLength()).thenReturn(6000);
        final Moles game = launched(MolesLevel.HARD, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            final Moles.Hole hole = firstOut(game);
            steps(game, MolesLevel.HARD.getStay() + Moles.RISE + 0.1);
            assertNotEquals(Moles.State.UP, hole.getState());
            return null;
        });
    }

    @Test
    void shouldHitAtOnceAndCountAPoint() throws Exception {
        final Moles game = launched(MolesLevel.EASY, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            final Moles.Hole hole = firstOut(game);
            enter(hole);
            game.step(0.02);
            exit(hole);
            assertEquals(1, game.getHits());
            assertEquals(Moles.State.FALLING, hole.getState());
            return null;
        });
        verify(stats, times(1)).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add(contains("data/whackmole/sounds/hit"));
    }

    @Test
    void shouldHitAfterTheFixationOfTheSettingsWithTheRing() throws Exception {
        final Moles game = launched(MolesLevel.EASY, MolesHit.FIXATION, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            final Moles.Hole hole = firstOut(game);
            enter(hole);
            steps(game, 0.2);
            assertEquals(0, game.getHits());
            assertTrue(hole.getRing().isVisible());
            assertEquals(0.4, hole.getRing().getProgress(), 0.1);
            steps(game, 0.4);
            assertEquals(1, game.getHits());
            assertFalse(hole.getRing().isVisible());
            return null;
        });
    }

    /**
     * Maintainer's rule of 4/11/2026: the length is that of the settings, changed by the gear during a game.
     */
    @Test
    void shouldReadTheLengthOfTheFixationDuringTheGame() throws Exception {
        when(configuration.getFixationLength()).thenReturn(9000);
        final Moles game = launched(MolesLevel.EASY, MolesHit.FIXATION, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            enter(firstOut(game));
            steps(game, 1);
            assertEquals(0, game.getHits());
            when(configuration.getFixationLength()).thenReturn(500);
            game.step(0.02);
            assertEquals(1, game.getHits());
            return null;
        });
    }

    @Test
    void shouldForgiveAShortLookAwayOnly() throws Exception {
        final Moles game = launched(MolesLevel.EASY, MolesHit.FIXATION, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            final Moles.Hole hole = firstOut(game);
            enter(hole);
            steps(game, 0.2);
            final double reached = hole.getFixationSeconds();
            exit(hole);
            steps(game, Moles.FIXATION_GRACE / 2);
            assertEquals(reached, hole.getFixationSeconds(), 1e-9);
            steps(game, Moles.FIXATION_GRACE);
            assertEquals(0, hole.getFixationSeconds(), 1e-9);
            assertFalse(hole.getRing().isVisible());
            return null;
        });
    }

    /**
     * A look on a hole counts as soon as the target shows, not before: nothing is hit in an empty hole.
     */
    @Test
    void shouldCountALookAlreadyOnTheHoleOnlyWhenTheTargetShows() throws Exception {
        final Moles game = launched(MolesLevel.EASY, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        fx(() -> {
            game.getHoles().forEach(MolesTest::enter);
            assertEquals(0, game.getHits());
            for (int i = 0; i < 200 && game.getHits() == 0; i++) {
                game.step(0.02);
            }
            assertEquals(1, game.getHits(), "as soon as it shows");
            return null;
        });
    }

    @Test
    void shouldEndOnceAfterTheTargetsOfTheVariant() throws Exception {
        final Moles game = launched(MolesLevel.HARD, MolesHit.AT_ONCE, MolesEnd.HITS_20);
        fx(() -> {
            game.getHoles().forEach(MolesTest::enter);
            for (int i = 0; i < 3000 && game.getPhase() == Moles.Phase.PLAY; i++) {
                game.step(0.02);
            }
            steps(game, 1);
            return null;
        });
        assertEquals(20, game.getHits());
        assertEquals(Moles.Phase.OVER, game.getPhase());
        verify(gameContext, times(1)).playWinTransition(anyLong(), any());
        verify(stats, times(20)).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldEndAfterTheTimeOfTheVariant() throws Exception {
        final Moles game = launched(MolesLevel.EASY, MolesHit.FIXATION, MolesEnd.TIME_1);
        fx(() -> {
            assertEquals(60, game.getTimeLeft());
            steps(game, 59);
            assertEquals(Moles.Phase.PLAY, game.getPhase());
            steps(game, 1.1);
            assertEquals(Moles.Phase.OVER, game.getPhase());
            return null;
        });
        verify(gameContext, times(1)).playWinTransition(anyLong(), any());
    }

    /**
     * The limits of score and of time of the settings apply.
     */
    @Test
    void shouldStartTheLimitersOfTheSettings() throws Exception {
        launched(MolesLevel.EASY, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        verify(gameContext).startScoreLimiter();
        verify(gameContext).startTimeLimiter();
    }

    @Test
    void shouldGiveTheVariantsWithMyImagesAndReadOnesOfBefore() {
        final Set<IGameVariant> variants = new MolesGameVariantGenerator().getVariants();
        assertEquals(MolesTarget.values().length * MolesLevel.values().length * MolesHit.values().length
            * MolesEnd.values().length, variants.size());
        final GroupedGameVariant mine = new GroupedGameVariant(MolesTarget.MY_IMAGES.getKey(), MolesLevel.HARD.getKey(),
            MolesEnd.TIME_3.getKey(), MolesTarget.USER_IMAGES_FOLDER, MolesHit.FIXATION.getKey());
        assertTrue(variants.contains(mine));
        assertEquals("images", UserImagesVariant.folderOf(mine));
        assertEquals(MolesTarget.MY_IMAGES, WhacAMoleGameLauncher.targetOf(mine));
        assertEquals(MolesLevel.HARD, WhacAMoleGameLauncher.levelOf(mine));
        assertEquals(MolesEnd.TIME_3, WhacAMoleGameLauncher.endOf(mine));
        assertEquals(MolesHit.FIXATION, WhacAMoleGameLauncher.hitOf(mine));
        assertEquals(mine, IGameVariant.toGameVariant(mine.toString()));

        final IGameVariant old = new IntGameVariant(1);
        assertEquals(MolesTarget.MOLES, WhacAMoleGameLauncher.targetOf(old));
        assertEquals(MolesLevel.NORMAL, WhacAMoleGameLauncher.levelOf(old));
        assertEquals(MolesHit.AT_ONCE, WhacAMoleGameLauncher.hitOf(null));
        assertEquals(MolesEnd.ENDLESS, WhacAMoleGameLauncher.endOf(null));
    }

    @Test
    void shouldPlayWithTheBiboulesOfTheShootingGame() throws Exception {
        final Moles game = launched(MolesTarget.BIBOULES, MolesLevel.EASY, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            assertTrue(firstOut(game).isTargetShown());
            return null;
        });
    }

    /**
     * "My images": the images of the user are the targets; without any, a message and no target (the game used to take
     * the default images without saying so).
     */
    @Test
    void shouldPlayWithTheImagesOfTheUserOrSaySoWithoutAny() throws Exception {
        final java.io.File folder = UserImagesVariant.directory(MolesTarget.USER_IMAGES_FOLDER);
        final java.io.File[] others = folder.listFiles();
        org.junit.jupiter.api.Assumptions.assumeTrue(others == null || others.length == 0);
        final Moles without = launched(MolesTarget.MY_IMAGES, MolesLevel.EASY, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        assertEquals(Moles.Phase.OVER, without.getPhase());
        assertTrue(without.getHoles().isEmpty());
        verify(translator).translate("NoUserImages");

        final java.io.File image = new java.io.File(folder, "test-moles.png");
        try (java.io.InputStream in = getClass().getClassLoader()
            .getResourceAsStream("data/whackmole/images/mole.png")) {
            java.nio.file.Files.copy(in, image.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            assertZonesDoNotMeet(launched(MolesTarget.MY_IMAGES, MolesLevel.HARD, MolesHit.AT_ONCE, MolesEnd.ENDLESS));
            final Moles game = launched(MolesTarget.MY_IMAGES, MolesLevel.EASY, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
            assertZonesDoNotMeet(game);
            fx(() -> {
                stepUntilATargetIsOut(game);
                enter(firstOut(game));
                game.step(0.02);
                assertEquals(1, game.getHits());
                return null;
            });
        } finally {
            assertTrue(image.delete());
        }
    }

    @Test
    void shouldRemoveEverythingWhenDisposedAndGiveTheAreasOfInterest() throws Exception {
        final Moles game = launched(MolesLevel.NORMAL, MolesHit.AT_ONCE, MolesEnd.ENDLESS);
        fx(() -> {
            stepUntilATargetIsOut(game);
            game.dispose();
            game.step(1);
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
        assertEquals(Moles.Phase.OVER, game.getPhase());
        verify(stats).setTargetAOIList(any());
    }
}

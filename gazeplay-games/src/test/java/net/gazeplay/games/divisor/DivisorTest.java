package net.gazeplay.games.divisor;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Diviseur" (reviewed on 12/11/2026, maintainer's rules): one game with the rabbits as a theme; the levels (divisions,
 * speed, sizes); the two halves going apart; nothing bursting before the delay of the settings nor during the first
 * moments of a new target; burst at once or after the fixation of the settings, read at once; the end; the variants.
 */
@ExtendWith(ApplicationExtension.class)
class DivisorTest {

    private static final double WIDTH = 1600;
    private static final double HEIGHT = 900;

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
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(WIDTH, HEIGHT));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getGameStartDelay()).thenReturn(0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Divisor launched(final DivisorTheme theme, final DivisorTouch touch, final DivisorLevel level)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Divisor game = new Divisor(gameContext, stats, theme, touch, level, 5);
            game.setManual();
            game.launch();
            // The delay of the settings (none here)
            game.step(0.01);
            return game;
        });
    }

    private static void steps(final Divisor game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void enter(final Divisor.Target target) {
        Event.fireEvent(target.getNode(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
    }

    private static void exit(final Divisor.Target target) {
        Event.fireEvent(target.getNode(), new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
    }

    @Test
    void shouldCountTheTargetsOfEachLevel() {
        assertEquals(7, DivisorLevel.EASY.getTargets());
        assertEquals(15, DivisorLevel.NORMAL.getTargets());
        assertEquals(31, DivisorLevel.HARD.getTargets());
        assertEquals(5, DivisorLevel.EASY.getCrossingSeconds());
        assertEquals(3, DivisorLevel.NORMAL.getCrossingSeconds());
        assertEquals(2, DivisorLevel.HARD.getCrossingSeconds());
    }

    @Test
    void shouldStartWithOneBigTargetAndTheGoalsOfTheLevel() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.HARD);
        assertEquals(1, game.getTargets().size());
        assertEquals(HEIGHT / 6, game.getTargets().get(0).getRadius(), 0.01);
        verify(stats).incrementNumberOfGoalsToReach(31);
        verify(stats).notifyNewRoundReady();
    }

    /**
     * Smaller at each division, never under a tenth of the height of the screen across.
     */
    @Test
    void shouldKeepTheSmallestTargetsLargeEnoughToBeLookedAt() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.HARD);
        double previous = Double.MAX_VALUE;
        for (int division = 0; division <= DivisorLevel.HARD.getDivisions(); division++) {
            final double radius = game.radiusOf(division);
            assertTrue(radius <= previous);
            assertTrue(radius >= HEIGHT * 0.05 - 0.01, "division " + division + ": " + radius);
            previous = radius;
        }
        assertTrue(game.radiusOf(1) < game.radiusOf(0));
    }

    @Test
    void shouldGoThroughTheHeightOfTheScreenInTheSecondsOfTheLevel() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.NORMAL);
        fx(() -> {
            final Divisor.Target target = game.getTargets().get(0);
            final double x = target.getX();
            final double y = target.getY();
            game.step(0.01);
            assertEquals(HEIGHT / 3 * 0.01, Math.hypot(target.getX() - x, target.getY() - y), 0.5);
            return null;
        });
    }

    /**
     * Speeds in pixels before the review: twice as fast, as a part of the screen, on a screen twice as small.
     */
    @Test
    void shouldGoAtTheSameSpeedAsAPartOfTheScreenWhateverItsSize() throws Exception {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(800, 450));
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.NORMAL);
        fx(() -> {
            final Divisor.Target target = game.getTargets().get(0);
            final double x = target.getX();
            final double y = target.getY();
            game.step(0.01);
            assertEquals(450.0 / 3 * 0.01, Math.hypot(target.getX() - x, target.getY() - y), 0.5);
            return null;
        });
    }

    @Test
    void shouldKeepEveryTargetInsideTheScreen() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.HARD);
        fx(() -> {
            game.burst(game.getTargets().get(0));
            game.burst(game.getTargets().get(0));
            for (int i = 0; i < 400; i++) {
                game.step(0.03);
                for (final Divisor.Target target : game.getTargets()) {
                    final double r = target.getRadius();
                    assertTrue(target.getX() >= r - 0.01 && target.getX() <= WIDTH - r + 0.01, "x " + target.getX());
                    assertTrue(target.getY() >= r - 0.01 && target.getY() <= HEIGHT - r + 0.01, "y " + target.getY());
                }
            }
            return null;
        });
    }

    /**
     * The two new targets used to be born on each other, going in random directions.
     */
    @Test
    void shouldSendTheTwoHalvesApartInOppositeDirections() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.NORMAL);
        fx(() -> {
            final Divisor.Target first = game.getTargets().get(0);
            game.burst(first);
            assertEquals(2, game.getTargets().size());
            final Divisor.Target a = game.getTargets().get(0);
            final Divisor.Target b = game.getTargets().get(1);
            assertEquals(1, a.getDivision());
            assertTrue(a.getRadius() < first.getRadius());
            assertTrue(Math.hypot(a.getX() - b.getX(), a.getY() - b.getY()) > a.getRadius() * 0.5);
            assertEquals(-1, a.getDx() * b.getDx() + a.getDy() * b.getDy(), 1e-9);
            verify(stats).incrementNumberOfGoalsReached();
            return null;
        });
    }

    @Test
    void shouldBurstAtOnceWhenTouched() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
        fx(() -> {
            enter(game.getTargets().get(0));
            game.step(0.02);
            assertEquals(1, game.getBurst());
            assertEquals(2, game.getTargets().size());
            return null;
        });
    }

    /**
     * The gaze is still where the parent was: a new target waits before it can burst.
     */
    @Test
    void shouldNotBurstANewTargetDuringItsFirstMoments() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
        fx(() -> {
            game.burst(game.getTargets().get(0));
            final Divisor.Target child = game.getTargets().get(0);
            enter(child);
            steps(game, Divisor.ARMING - 0.1);
            assertEquals(1, game.getBurst());
            steps(game, 0.2);
            assertEquals(2, game.getBurst());
            return null;
        });
    }

    @Test
    void shouldNotBurstBeforeTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Divisor game = fx(() -> {
            gameArea.getChildren().clear();
            final Divisor g = new Divisor(gameContext, stats, DivisorTheme.RABBITS, DivisorTouch.AT_ONCE,
                DivisorLevel.EASY, 5);
            g.setManual();
            g.launch();
            return g;
        });
        fx(() -> {
            final Divisor.Target target = game.getTargets().get(0);
            final double x = target.getX();
            enter(target);
            steps(game, 0.9);
            assertEquals(0, game.getBurst());
            assertEquals(x, target.getX());
            assertEquals(Divisor.Phase.WAIT, game.getPhase());
            steps(game, 0.2);
            assertEquals(1, game.getBurst());
            return null;
        });
    }

    @Test
    void shouldBurstAfterTheFixationOfTheSettings() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.FIXATION, DivisorLevel.EASY);
        fx(() -> {
            enter(game.getTargets().get(0));
            steps(game, 0.4);
            assertEquals(0, game.getBurst());
            assertTrue(game.getRing().isVisible());
            steps(game, 0.15);
            assertEquals(1, game.getBurst());
            assertFalse(game.getRing().isVisible());
            return null;
        });
    }

    @Test
    void shouldReadTheFixationLengthAtEachStep() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.FIXATION, DivisorLevel.EASY);
        fx(() -> {
            enter(game.getTargets().get(0));
            steps(game, 0.3);
            when(configuration.getFixationLength()).thenReturn(2000);
            steps(game, 0.5);
            assertEquals(0, game.getBurst());
            when(configuration.getFixationLength()).thenReturn(700);
            steps(game, 0.1);
            assertEquals(1, game.getBurst());
            return null;
        });
    }

    @Test
    void shouldForgiveAShortLookAwayOnly() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.FIXATION, DivisorLevel.EASY);
        fx(() -> {
            final Divisor.Target target = game.getTargets().get(0);
            enter(target);
            steps(game, 0.3);
            exit(target);
            steps(game, 0.2);
            enter(target);
            steps(game, 0.15);
            assertEquals(0.45, game.getFixationSeconds(), 0.03);
            exit(target);
            steps(game, 0.4);
            assertEquals(0, game.getFixationSeconds());
            assertFalse(game.getRing().isVisible());
            assertEquals(0, game.getBurst());
            return null;
        });
    }

    @Test
    void shouldEndWithBravoWhenAllHaveBurst() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
        fx(() -> {
            steps(game, 2.5);
            while (!game.getTargets().isEmpty()) {
                game.burst(game.getTargets().get(0));
            }
            assertEquals(7, game.getBurst());
            assertEquals(Divisor.Phase.OVER, game.getPhase());
            assertTrue(game.getPlaySeconds() >= 2.4);
            verify(stats, times(7)).incrementNumberOfGoalsReached();
            verify(gameContext).playWinTransition(anyLong(), any());
            // The time taken, shown in the middle
            assertEquals(2, game.getHud().getChildren().size());
            return null;
        });
    }

    @Test
    void shouldShowTheRabbitsAsDrawn() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
        final Group drawing = (Group) game.getTargets().get(0).getNode().getChildren().get(1);
        final ImageView view = (ImageView) drawing.getChildren().get(0);
        assertTrue(view.isPreserveRatio());
        assertTrue(view.getImage().getUrl().contains("divisor/rabbit/images"));
    }

    @Test
    void shouldShowTheAnimalsInACircle() throws Exception {
        final Divisor game = launched(DivisorTheme.ANIMALS, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
        final Group drawing = (Group) game.getTargets().get(0).getNode().getChildren().get(1);
        final ImageView view = (ImageView) drawing.getChildren().get(0);
        assertNotNull(view.getClip());
        assertTrue(view.getImage().getUrl().contains("common/default/images"));
    }

    @Test
    void shouldSayWhenThereIsNoImageOfTheUser() throws Exception {
        assertEquals(0, ImageUtils.listImageFiles(GazePlayDirectories.getImagesDirectory()).size(),
            "no images left by another test");
        final Divisor game = launched(DivisorTheme.MY_IMAGES, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
        assertTrue(game.getTargets().isEmpty());
        assertEquals(1, game.getHud().getChildren().size());
        verify(translator).translate("NoUserImages");
    }

    @Test
    void shouldShowTheImagesOfTheUser() throws Exception {
        final File folder = GazePlayDirectories.getImagesDirectory();
        assertTrue(folder.isDirectory() || folder.mkdirs());
        final File image = new File(folder, "biscuit.jpg");
        final File source = new File(System.getProperty("user.dir"),
            "src/test/resources/images/portraits/biscuit.jpg");
        Files.copy(source.toPath(), image.toPath(), StandardCopyOption.REPLACE_EXISTING);
        try {
            final Divisor game = launched(DivisorTheme.MY_IMAGES, DivisorTouch.AT_ONCE, DivisorLevel.EASY);
            final Group drawing = (Group) game.getTargets().get(0).getNode().getChildren().get(1);
            final ImageView view = (ImageView) drawing.getChildren().get(0);
            assertTrue(view.getImage().getUrl().contains("biscuit"));
        } finally {
            assertTrue(image.delete());
        }
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final Divisor game = launched(DivisorTheme.RABBITS, DivisorTouch.FIXATION, DivisorLevel.EASY);
        fx(() -> {
            game.burst(game.getTargets().get(0));
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getTargets().isEmpty());
            assertEquals(Divisor.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldOfferEachThemeWayAndLevel() {
        final Set<IGameVariant> variants = new DivisorGameVariantGenerator().getVariants();
        assertEquals(3 * 2 * 3, variants.size());
        for (final IGameVariant variant : variants) {
            final GroupedGameVariant grouped = (GroupedGameVariant) variant;
            final boolean myImages = grouped.getGroup().equals(DivisorTheme.MY_IMAGES.getKey());
            assertEquals(myImages ? UserImagesVariant.IMAGES : null, grouped.getUserImagesFolder());
        }
    }

    @Test
    void shouldReadTheVariants() {
        final IGameVariant variant = new GroupedGameVariant("DivisorAnimals", "DivisorFixation", "Hard", null);
        assertEquals(DivisorTheme.ANIMALS, DivisorGameLauncher.themeOf(variant));
        assertEquals(DivisorTouch.FIXATION, DivisorGameLauncher.touchOf(variant));
        assertEquals(DivisorLevel.HARD, DivisorGameLauncher.levelOf(variant));
        // Without a variant, as the games "Divisor" and "Rabbits" before the review: 15 rabbits, at once
        final IGameVariant none = new IntGameVariant(1);
        assertEquals(DivisorTheme.RABBITS, DivisorGameLauncher.themeOf(none));
        assertEquals(DivisorTouch.AT_ONCE, DivisorGameLauncher.touchOf(none));
        assertEquals(DivisorLevel.NORMAL, DivisorGameLauncher.levelOf(null));
        assertEquals(List.of("DivisorRabbits", "DivisorAnimals", "DivisorMyImages"),
            List.of(DivisorTheme.values()).stream().map(DivisorTheme::getKey).toList());
    }
}

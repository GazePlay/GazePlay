package net.gazeplay.games.ladder;

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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Échelle" (reviewed on 13/11/2026): each start leads to its own picture, the delay and the fixation, the descent, the
 * watch, the aim (a wrong start, the hint), the end, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class LadderTest {

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

    private Ladder launched(final LadderMode mode, final LadderLevel level, final LadderEnd end, final double seed)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Ladder game = new Ladder(gameContext, stats, mode, level, end, LadderPictures.ANIMALS, seed);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final Ladder game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Chooses a start and waits for the Biboule at the bottom.
     */
    private static void go(final Ladder game, final int start) {
        final double time = game.runTime(start);
        game.choose(game.getStarts().get(start));
        steps(game, time + 0.1);
    }

    @Test
    void shouldLeadEachStartToItsOwnPicture() throws Exception {
        for (final LadderLevel level : LadderLevel.values()) {
            for (int seed = 1; seed <= 20; seed++) {
                final Ladder game = launched(LadderMode.WATCH, level, LadderEnd.ENDLESS, seed);
                fx(() -> {
                    final Set<Integer> arrivals = new HashSet<>();
                    for (int start = 0; start < level.getLines(); start++) {
                        arrivals.add(game.arrivalFrom(start));
                    }
                    assertEquals(level.getLines(), arrivals.size(), level.toString());
                    // The animals at the bottom are all different (each is twice in the default images)
                    assertEquals(level.getLines(), new HashSet<>(game.getAnimals()).size(), game.getAnimals()
                        .toString());
                    return null;
                });
            }
        }
    }

    @Test
    void shouldChooseNothingDuringTheDelayThenAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Ladder game = launched(LadderMode.WATCH, LadderLevel.EASY, LadderEnd.ENDLESS, 3);
        fx(() -> {
            final Ladder.Start start = game.getStarts().get(1);
            game.choose(start);
            assertEquals(Ladder.Phase.WAIT, game.getPhase());
            steps(game, 1.02);
            game.lookAt(start);
            steps(game, 0.3);
            game.lookAway(start);
            steps(game, 0.5);
            assertEquals(Ladder.Phase.CHOOSE, game.getPhase());
            game.lookAt(start);
            steps(game, 0.52);
            assertEquals(Ladder.Phase.RUN, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldGoDownToItsPictureThenGiveAnotherLadderInTheWatch() throws Exception {
        final Ladder game = launched(LadderMode.WATCH, LadderLevel.NORMAL, LadderEnd.ENDLESS, 5);
        fx(() -> {
            final int expected = game.arrivalFrom(2);
            game.choose(game.getStarts().get(2));
            // Nothing is chosen while the Biboule goes down
            game.choose(game.getStarts().get(0));
            steps(game, game.runTime(2) + 0.1);
            assertEquals(expected, game.getArrival());
            assertEquals(Ladder.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getGames());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            steps(game, Ladder.REWARD + 0.04);
            assertEquals(Ladder.Phase.CHOOSE, game.getPhase());
            assertEquals(-1, game.getArrival());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldCountAWrongStartThenFrameTheRightOneInTheAim() throws Exception {
        final Ladder game = launched(LadderMode.AIM, LadderLevel.HARD, LadderEnd.ENDLESS, 7);
        fx(() -> {
            final int target = game.getTarget();
            assertTrue(target >= 0);
            final int right = game.startFor(target);
            final int wrong = (right + 1) % LadderLevel.HARD.getLines();
            for (int attempt = 1; attempt <= 2; attempt++) {
                assertFalse(game.isHintShown());
                go(game, wrong);
                assertEquals(Ladder.Phase.RESULT, game.getPhase());
                assertNotEquals(target, game.getArrival());
                verify(stats, times(attempt)).incrementNumberOfMistakes();
                steps(game, Ladder.RESULT + 0.04);
                assertEquals(Ladder.Phase.CHOOSE, game.getPhase());
                assertEquals(target, game.getTarget());
            }
            assertTrue(game.isHintShown());
            go(game, right);
            assertEquals(Ladder.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getGames());
            verify(gameContext.getSoundManager(), times(2)).add("data/dice/sounds/wrong.wav");
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheGamesOfTheVariant() throws Exception {
        final Ladder game = launched(LadderMode.AIM, LadderLevel.EASY, LadderEnd.GAMES_3, 9);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                go(game, game.startFor(game.getTarget()));
                steps(game, Ladder.REWARD + 0.04);
            }
            assertEquals(3, game.getGames());
            assertEquals(Ladder.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final Ladder game = launched(LadderMode.WATCH, LadderLevel.NORMAL, LadderEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getStarts().isEmpty());
            assertEquals(Ladder.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndALaunchWithoutVariant() {
        final Set<IGameVariant> variants = new LadderGameVariantGenerator().getVariants();
        assertEquals(2 * 4 * 2 * 3, variants.size());
        final Ladder aim = LadderGameLauncher.create(gameContext,
            new GroupedGameVariant("LadderAim", "LadderEnd10", "LadderHard", "images", "LadderMyImages"), stats, 1);
        assertEquals(LadderMode.AIM, aim.getMode());
        assertEquals(LadderEnd.GAMES_10, aim.getEnd());
        assertEquals(LadderLevel.HARD, aim.getLevel());
        assertEquals(LadderPictures.MY_IMAGES, aim.getPictures());
        final Ladder before = LadderGameLauncher.create(gameContext, null, stats, 1);
        assertEquals(LadderMode.WATCH, before.getMode());
        assertEquals(LadderEnd.GAMES_3, before.getEnd());
    }
}

package net.gazeplay.games.simon;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Répète après moi" (name code Simon; reviewed on 10/11/2026, maintainer's rules): the melody played at the speed of
 * the settings, repeated with fixations of the length of the settings, a mistake shown at once, the lives, the three
 * modes, the ends, the layout, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class SimonTest {

    private static final double STEP = 1.0 / 60;

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
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getGameStartDelay()).thenReturn(0);
        when(configuration.getFixationLength()).thenReturn(1000);
        screen(1920, 1080);
    }

    private void screen(final double width, final double height) {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(width, height));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Simon launched(final SimonMode mode, final SimonEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Simon game = new Simon(gameContext, stats, mode, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final Simon game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    /**
     * Steps until the player can play (or the game is over), and returns the buttons lit meanwhile, in order.
     */
    private static List<Integer> listen(final Simon game) throws Exception {
        final List<Integer> heard = new ArrayList<>();
        int last = -1;
        for (int i = 0; i < 6000 && game.getPhase() != Simon.Phase.INPUT && game.getPhase() != Simon.Phase.ADD
            && game.getPhase() != Simon.Phase.SHOW && game.getPhase() != Simon.Phase.OVER; i++) {
            steps(game, STEP);
            final int lit = game.getLit();
            if (lit >= 0 && lit != last && game.getPhase() == Simon.Phase.LISTEN) {
                heard.add(lit);
            }
            last = lit;
        }
        return heard;
    }

    private static void press(final Simon game, final int button) throws Exception {
        fx(() -> {
            game.press(button);
            return null;
        });
    }

    /**
     * Repeats the melody heard, as at the end of fixations.
     */
    private static void repeat(final Simon game) throws Exception {
        for (final int button : new ArrayList<>(game.getMelody())) {
            press(game, button);
        }
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 3);
        steps(game, 1.5);
        assertEquals(Simon.Phase.WAIT, game.getPhase());
        assertTrue(game.getMelody().isEmpty());
        steps(game, 0.6);
        assertEquals(Simon.Phase.BEFORE, game.getPhase());
        assertEquals(1, game.getMelody().size());
        verify(stats).notifyNewRoundReady();
    }

    @Test
    void shouldPlayTheMelodyThenLetThePlayerRepeatItAndAddANote() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 5);
        steps(game, 0.05);
        assertEquals(game.getMelody(), listen(game));
        assertEquals(Simon.Phase.INPUT, game.getPhase());
        verify(gameContext.getSoundManager(), atLeastOnce()).add(startsWith("data/simonGame/sounds/note"));
        repeat(game);
        verify(stats).incrementNumberOfGoalsReached();
        assertEquals(2, game.getMelody().size());
        // The device plays both notes, in order, then the player repeats them
        assertEquals(game.getMelody(), listen(game));
        repeat(game);
        assertEquals(3, game.getMelody().size());
        assertEquals(2, game.getBest());
        verify(stats, times(3)).incrementNumberOfGoalsToReach();
    }

    private static String startsWith(final String prefix) {
        return org.mockito.ArgumentMatchers.startsWith(prefix);
    }

    @Test
    void shouldPlayEachNoteForLessThanASecondAtTheSpeedOfTheSettings() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 7);
        steps(game, 0.05);
        listen(game);
        repeat(game);
        listen(game);
        repeat(game);
        // Three notes: one silence before, then a note and a silence each
        assertEquals(3, game.getMelody().size());
        int steps = 0;
        while (game.getPhase() != Simon.Phase.INPUT && steps < 2000) {
            steps(game, STEP);
            steps++;
        }
        assertEquals(Simon.BEFORE + 3 * (Simon.NOTE + Simon.GAP), steps * STEP, 0.1);
        // Twice as fast with the speed of the settings
        repeat(game);
        when(configuration.getAnimationSpeedRatio()).thenReturn(2.0);
        steps = 0;
        while (game.getPhase() != Simon.Phase.INPUT && steps < 2000) {
            steps(game, STEP);
            steps++;
        }
        assertEquals((Simon.BEFORE + 4 * (Simon.NOTE + Simon.GAP)) / 2, steps * STEP, 0.1);
    }

    @Test
    void shouldChooseAButtonAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final Simon game = launched(SimonMode.FREE, SimonEnd.ENDLESS, 9);
        steps(game, 0.05);
        assertEquals(Simon.Phase.INPUT, game.getPhase());
        fx(() -> {
            game.lookAt(2);
            return null;
        });
        steps(game, 0.5);
        assertTrue(game.getMelody().isEmpty());
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertTrue(game.getMelody().isEmpty());
        steps(game, 0.07);
        assertEquals(List.of(2), game.getMelody());
        assertEquals(2, game.getLit());
    }

    @Test
    void shouldNotChooseWhileTheDevicePlays() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 11);
        steps(game, 0.05);
        fx(() -> {
            game.lookAt(0);
            return null;
        });
        steps(game, Simon.BEFORE + 0.3);
        assertEquals(Simon.Phase.LISTEN, game.getPhase());
        assertEquals(1, game.getMelody().size());
        press(game, 1);
        assertEquals(Simon.Phase.LISTEN, game.getPhase());
    }

    @Test
    void shouldShowAMistakeAtOnceCostALifeAndPlayTheMelodyAgain() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 13);
        steps(game, 0.05);
        listen(game);
        final int right = game.getMelody().get(0);
        final int wrong = (right + 1) % Simon.BUTTONS;
        press(game, wrong);
        assertEquals(Simon.Phase.MISTAKE, game.getPhase());
        assertEquals(Simon.LIVES - 1, game.getLives());
        verify(gameContext.getSoundManager()).add("data/simonGame/sounds/mistake.wav");
        // The right button blinks
        steps(game, 0.05);
        assertEquals(right, game.getLit());
        steps(game, Simon.MISTAKE / 6);
        assertEquals(-1, game.getLit());
        // Then the same melody again
        assertEquals(List.of(right), listen(game));
        assertEquals(1, game.getMelody().size());
    }

    @Test
    void shouldEndAfterThreeMistakesWithTheLongestMelodyAndNoBravo() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 15);
        steps(game, 0.05);
        listen(game);
        repeat(game);
        for (int mistake = 0; mistake < Simon.LIVES; mistake++) {
            listen(game);
            press(game, (game.getMelody().get(0) + 1) % Simon.BUTTONS);
            steps(game, Simon.MISTAKE + 0.05);
        }
        assertEquals(Simon.Phase.SHOW, game.getPhase());
        assertEquals(0, game.getLives());
        assertEquals(1, game.getBest());
        assertFalse(game.isWon());
        steps(game, Simon.SHOW + 0.05);
        assertEquals(Simon.Phase.OVER, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldEndWithBravoWhenTheMelodyIsComplete() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.NOTES_5, 17);
        steps(game, 0.05);
        for (int notes = 1; notes <= 5; notes++) {
            listen(game);
            assertEquals(notes, game.getMelody().size());
            repeat(game);
        }
        assertEquals(Simon.Phase.SHOW, game.getPhase());
        assertTrue(game.isWon());
        verify(gameContext.getSoundManager()).add("data/simonGame/sounds/success.wav");
        steps(game, Simon.SHOW + 0.05);
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldLetTwoPlayersRepeatAndAddInTurnAndGiveTheWinToTheOtherOneAtAMistake() throws Exception {
        final Simon game = launched(SimonMode.DUO, SimonEnd.ENDLESS, 19);
        steps(game, 0.05);
        // Player 1 adds the first note
        assertEquals(Simon.Phase.ADD, game.getPhase());
        assertEquals(1, game.getPlayer());
        press(game, 3);
        assertEquals(2, game.getPlayer());
        assertEquals(List.of(3), listen(game));
        // Player 2 repeats it, then adds one
        repeat(game);
        assertEquals(Simon.Phase.ADD, game.getPhase());
        press(game, 0);
        assertEquals(1, game.getPlayer());
        assertEquals(List.of(3, 0), listen(game));
        // Player 1 makes a mistake: player 2 wins
        press(game, 1);
        steps(game, Simon.MISTAKE + 0.05);
        assertEquals(Simon.Phase.SHOW, game.getPhase());
        assertEquals(2, game.getWinner());
        steps(game, Simon.SHOW + 0.05);
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldReplayTheWholeMelodyAfterEachNoteInFreeModeUntilItIsComplete() throws Exception {
        final Simon game = launched(SimonMode.FREE, SimonEnd.NOTES_5, 21);
        steps(game, 0.05);
        final int[] notes = {0, 2, 1, 3, 2};
        for (int k = 0; k < notes.length; k++) {
            press(game, notes[k]);
            if (k < notes.length - 1) {
                final List<Integer> heard = listen(game);
                assertEquals(game.getMelody(), heard);
                assertEquals(Simon.Phase.INPUT, game.getPhase());
            }
        }
        assertEquals(Simon.Phase.SHOW, game.getPhase());
        assertTrue(game.isWon());
        verify(stats, times(5)).incrementNumberOfGoalsReached();
        verify(stats, times(5)).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldPutTheDeviceInTheMiddleInsideTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1366, 768}, {1024, 768}, {800, 1000},
            {1280, 600}}) {
            screen(screen[0], screen[1]);
            final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 1);
            final double[] layout = game.layout();
            final String name = screen[0] + " x " + screen[1];
            assertEquals(screen[0] / 2, layout[0], 1e-6, name);
            assertTrue(layout[0] - layout[2] >= 0 && layout[0] + layout[2] <= screen[0], name);
            assertTrue(layout[1] - layout[2] >= screen[1] * 0.11 - 1 && layout[1] + layout[2] <= screen[1], name);
            // Each button is a large target
            assertTrue(game.buttonOf(0).getBoundsInLocal().getWidth() > layout[2] * 0.8, name);
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    @Test
    void shouldReplayTheSameMelody() throws Exception {
        final List<List<Integer>> melodies = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 23);
            steps(game, 0.05);
            listen(game);
            repeat(game);
            listen(game);
            repeat(game);
            melodies.add(new ArrayList<>(game.getMelody()));
        }
        assertEquals(melodies.get(0), melodies.get(1));
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final Simon game = launched(SimonMode.CLASSIC, SimonEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(Simon.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(Simon.BUTTONS)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayTheOldOnes() {
        final Set<IGameVariant> variants = new SimonGameVariantGenerator().getVariants();
        assertEquals(12, variants.size());
        final IGameVariant variant = new GroupedGameVariant(SimonGameVariantGenerator.GROUP, SimonMode.DUO.getKey(),
            SimonEnd.NOTES_12.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(SimonMode.DUO, SimonGameLauncher.modeOf(variant));
        assertEquals(SimonEnd.NOTES_12, SimonGameLauncher.endOf(variant));
        final IGameVariant hard = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.simon.SimonGameVariant:HARD_CLASSIC");
        assertEquals(SimonMode.CLASSIC, SimonGameLauncher.modeOf(hard));
        assertEquals(SimonEnd.ENDLESS, SimonGameLauncher.endOf(hard));
        final IGameVariant copy = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.simon.SimonGameVariant:MODE2");
        assertEquals(SimonMode.FREE, SimonGameLauncher.modeOf(copy));
    }

    @Test
    void shouldSaveTheStatisticsInTheirOwnFolderNoLongerWithThoseOfThePiano() {
        assertEquals("simon", SimonStats.NAME);
    }
}

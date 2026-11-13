package net.gazeplay.games.pianosight;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
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
 * The Piano (reviewed on 13/11/2026, maintainer's rules): the keyboard, the songs, the delay and the fixation, a song
 * followed to its end then played again, a wrong key, the free mode, the end, the variants and those of before.
 */
@ExtendWith(ApplicationExtension.class)
class PianoTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    private final Configuration configuration = mock(Configuration.class, withSettings().stubOnly());

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    /**
     * The notes played, instead of the synthesizer.
     */
    private final List<Integer> heard = new ArrayList<>();
    private boolean closed;

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
        heard.clear();
        closed = false;
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Piano launched(final PianoMode mode, final PianoEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Piano game = new Piano(gameContext, stats, mode, end, 8, () -> new NotePlayer() {
                @Override
                public void play(final int midiNote) {
                    heard.add(midiNote);
                }

                @Override
                public void close() {
                    closed = true;
                }
            });
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final Piano game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Plays the song of now to its end.
     */
    private static void playSong(final Piano game) {
        final Song song = game.getSong();
        for (final int note : song.getNotes()) {
            game.choose(game.key(note));
        }
    }

    /**
     * @return the seconds the song of now takes to be played again, and the wait after
     */
    private static double replayLength(final Song song) {
        double beats = 0;
        for (final double beat : song.getBeats()) {
            beats += beat;
        }
        return 0.6 + beats * Piano.BEAT + Piano.AFTER + 0.1;
    }

    @Test
    void shouldHaveAnOctaveOfThirteenKeysAndSongsWithinIt() throws Exception {
        final Piano game = launched(PianoMode.FREE, PianoEnd.FREE);
        fx(() -> {
            assertEquals(13, game.getKeys().size());
            for (int semitone = 0; semitone <= 12; semitone++) {
                assertNotNull(game.key(semitone));
            }
            return null;
        });
        for (final Song song : Song.values()) {
            assertTrue(song.size() >= 20, song.toString());
            for (final int note : song.getNotes()) {
                assertTrue(note >= 0 && note <= 12, song + " " + note);
            }
        }
        // Black keys in two songs: F sharp, B flat
        assertTrue(java.util.Arrays.stream(Song.AU_CLAIR_DE_LA_LUNE.getNotes()).anyMatch(n -> n == 6));
        assertTrue(java.util.Arrays.stream(Song.JOYEUX_ANNIVERSAIRE.getNotes()).anyMatch(n -> n == 10));
    }

    @Test
    void shouldPlayNothingDuringTheDelayThenAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Piano game = launched(PianoMode.FREE, PianoEnd.FREE);
        fx(() -> {
            final Piano.Key key = game.key(4);
            game.choose(key);
            assertTrue(heard.isEmpty());
            steps(game, 1.02);
            game.lookAt(key);
            steps(game, 0.3);
            game.lookAway(key);
            steps(game, 0.5);
            assertTrue(heard.isEmpty());
            game.lookAt(key);
            steps(game, 0.52);
            // Mi: four semitones above the middle C
            assertEquals(List.of(Piano.MIDDLE_C + 4), heard);
            return null;
        });
    }

    @Test
    void shouldFollowTheSongPlayItAgainThenAnother() throws Exception {
        final Piano game = launched(PianoMode.SONG, PianoEnd.ENDLESS);
        fx(() -> {
            final Song first = game.getSong();
            assertTrue(game.isHinted());
            // A wrong key plays its note, without a mistake, and the song does not go on
            final int right = first.getNotes()[0];
            final int wrong = right == 0 ? 2 : 0;
            game.choose(game.key(wrong));
            assertEquals(List.of(Piano.MIDDLE_C + wrong), heard);
            assertEquals(0, game.getNoteIndex());
            verify(stats, never()).incrementNumberOfMistakes();
            playSong(game);
            assertEquals(first.size(), game.getNoteIndex());
            assertEquals(Piano.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getSongs());
            assertFalse(game.isHinted());
            verify(stats, times(first.size() + 1)).incrementNumberOfGoalsReached();
            verify(stats).incrementNumberOfGoalsToReach(first.size() + 1);
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            // Played again in its rhythm: nothing can be played meanwhile
            heard.clear();
            game.choose(game.key(first.getNotes()[0]));
            assertTrue(heard.isEmpty());
            steps(game, replayLength(first));
            final List<Integer> tune = new ArrayList<>();
            for (final int note : first.getNotes()) {
                tune.add(Piano.MIDDLE_C + note);
            }
            assertEquals(tune, heard);
            assertEquals(Piano.Phase.PLAY, game.getPhase());
            assertNotEquals(first, game.getSong());
            assertEquals(0, game.getNoteIndex());
            assertTrue(game.isHinted());
            return null;
        });
    }

    @Test
    void shouldPlayEachKeyInTheFreeMode() throws Exception {
        final Piano game = launched(PianoMode.FREE, PianoEnd.SONGS_3);
        fx(() -> {
            assertEquals(PianoEnd.FREE, game.getEnd());
            assertNull(game.getSong());
            assertFalse(game.isHinted());
            for (int semitone = 0; semitone <= 12; semitone++) {
                game.choose(game.key(semitone));
                assertTrue(game.key(semitone).isPressed());
            }
            assertEquals(13, heard.size());
            assertEquals(Piano.MIDDLE_C + 12, heard.get(12));
            verify(stats, never()).incrementNumberOfGoalsReached();
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheSongsOfTheVariant() throws Exception {
        final Piano game = launched(PianoMode.SONG, PianoEnd.SONGS_3);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                final Song song = game.getSong();
                playSong(game);
                steps(game, replayLength(song));
            }
            assertEquals(3, game.getSongs());
            assertEquals(Piano.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopCloseTheSynthesizerAndLeaveTheGameArea() throws Exception {
        final Piano game = launched(PianoMode.SONG, PianoEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getKeys().isEmpty());
            assertTrue(closed);
            assertEquals(Piano.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Set<IGameVariant> variants = new PianoGameVariantGenerator().getVariants();
        assertEquals(5, variants.size());
        final Piano songs = PianoGameLauncher.create(gameContext,
            new GroupedGameVariant("PianoSongs", "PianoOctave", "PianoEnd5", null), stats, 1);
        assertEquals(PianoMode.SONG, songs.getMode());
        assertEquals(PianoEnd.SONGS_5, songs.getEnd());
        final Piano free = PianoGameLauncher.create(gameContext,
            new EnumGameVariant<>(PianoGameVariant.FREEPLAY, PianoGameVariant::getLabel), stats, 1);
        assertEquals(PianoMode.FREE, free.getMode());
        final Piano followed = PianoGameLauncher.create(gameContext,
            new EnumGameVariant<>(PianoGameVariant.NORMAL, PianoGameVariant::getLabel), stats, 1);
        assertEquals(PianoMode.SONG, followed.getMode());
        assertEquals(PianoEnd.ENDLESS, followed.getEnd());
    }
}

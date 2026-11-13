package net.gazeplay.games.blocs;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Blocs" (reviewed on 13/11/2026, maintainer's rules): the grids, a block removed at a look or after a fixation, the
 * help, a picture uncovered then another, the end, the variants and those of before.
 */
@ExtendWith(ApplicationExtension.class)
class BlocsTest {

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

    private Blocs launched(final BlocsMode mode, final BlocsLevel level, final BlocsEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Blocs game = new Blocs(gameContext, stats, mode, level, end, BlocsPictures.ANIMALS, 8);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final Blocs game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void removeAll(final Blocs game) {
        for (final Blocs.Block block : List.copyOf(game.getBlocks())) {
            game.lookAt(block);
        }
    }

    @Test
    void shouldCoverThePictureWithTheGridOfTheLevel() throws Exception {
        for (final BlocsLevel level : BlocsLevel.values()) {
            final Blocs game = launched(BlocsMode.GAZE, level, BlocsEnd.ENDLESS);
            fx(() -> {
                assertEquals(level.getSide() * level.getSide(), game.getBlocks().size());
                return null;
            });
        }
    }

    @Test
    void shouldRemoveABlockAtALookAfterTheDelayOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Blocs game = launched(BlocsMode.GAZE, BlocsLevel.EASY, BlocsEnd.ENDLESS);
        fx(() -> {
            final Blocs.Block block = game.getBlocks().get(0);
            game.lookAt(block);
            assertEquals(9, game.getBlocks().size());
            steps(game, 1.02);
            game.lookAt(block);
            assertEquals(8, game.getBlocks().size());
            verify(gameContext.getSoundManager()).add("data/blocs/sounds/pop.wav");
            return null;
        });
    }

    @Test
    void shouldRemoveABlockAfterTheFixationOnlyInTheFixationMode() throws Exception {
        final Blocs game = launched(BlocsMode.FIXATION, BlocsLevel.EASY, BlocsEnd.ENDLESS);
        fx(() -> {
            final Blocs.Block block = game.getBlocks().get(4);
            game.lookAt(block);
            steps(game, 0.3);
            game.lookAway(block);
            steps(game, 0.5);
            assertEquals(9, game.getBlocks().size());
            game.lookAt(block);
            steps(game, 0.52);
            assertEquals(8, game.getBlocks().size());
            assertFalse(game.getBlocks().contains(block));
            return null;
        });
    }

    @Test
    void shouldMakeTheBlocksLeftBlinkAfterAWhile() throws Exception {
        final Blocs game = launched(BlocsMode.GAZE, BlocsLevel.NORMAL, BlocsEnd.ENDLESS);
        fx(() -> {
            game.lookAt(game.getBlocks().get(0));
            steps(game, Blocs.HELP - 0.5);
            assertFalse(game.isHelping());
            steps(game, 0.6);
            assertTrue(game.isHelping());
            game.lookAt(game.getBlocks().get(0));
            assertFalse(game.isHelping());
            return null;
        });
    }

    @Test
    void shouldUncoverThePictureThenShowAnother() throws Exception {
        final Blocs game = launched(BlocsMode.GAZE, BlocsLevel.EASY, BlocsEnd.ENDLESS);
        fx(() -> {
            removeAll(game);
            assertEquals(Blocs.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getUncovered());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            steps(game, Blocs.REWARD + 0.04);
            assertEquals(Blocs.Phase.PLAY, game.getPhase());
            assertEquals(9, game.getBlocks().size());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterThePicturesOfTheVariant() throws Exception {
        final Blocs game = launched(BlocsMode.GAZE, BlocsLevel.EASY, BlocsEnd.PICTURES_3);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                removeAll(game);
                steps(game, Blocs.REWARD + 0.04);
            }
            assertEquals(3, game.getUncovered());
            assertEquals(Blocs.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final Blocs game = launched(BlocsMode.FIXATION, BlocsLevel.HARD, BlocsEnd.ENDLESS);
        fx(() -> {
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getBlocks().isEmpty());
            assertEquals(Blocs.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Set<IGameVariant> variants = new BlocsGameVariantGenerator().getVariants();
        assertEquals(2 * 4 * 2 * 3, variants.size());
        final Blocs mine = BlocsGameLauncher.create(gameContext,
            new GroupedGameVariant("BlocsFixation", "BlocsEnd5", "BlocsHard", "images", "BlocsMyImages"), stats, 1);
        assertEquals(BlocsMode.FIXATION, mine.getMode());
        assertEquals(BlocsEnd.PICTURES_5, mine.getEnd());
        assertEquals(BlocsLevel.HARD, mine.getLevel());
        assertEquals(BlocsPictures.MY_IMAGES, mine.getPictures());
        final Blocs nine = BlocsGameLauncher.create(gameContext, new DimensionGameVariant(9, 9), stats, 1);
        assertEquals(BlocsMode.GAZE, nine.getMode());
        assertEquals(BlocsLevel.HARD, nine.getLevel());
        assertEquals(BlocsEnd.ENDLESS, nine.getEnd());
        assertEquals(BlocsLevel.EASY, BlocsGameLauncher.create(gameContext, new DimensionGameVariant(2, 2), stats, 1)
            .getLevel());
    }
}

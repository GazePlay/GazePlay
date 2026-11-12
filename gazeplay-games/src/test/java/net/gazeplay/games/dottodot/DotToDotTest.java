package net.gazeplay.games.dottodot;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
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
 * "Point par point" (reviewed on 12/11/2026, maintainer's rules): the figures (named, dots of each level), the order
 * (the first dot first, a wrong dot a mistake, a dot joined left alone), the figure finished and named, the
 * progressive level, the proportions kept, the end, the variants and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class DotToDotTest {

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

    private DotToDot launched(final DotLevel level, final DotLabels labels, final DotEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final DotToDot game = new DotToDot(gameContext, stats, level, labels, end, 6);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final DotToDot game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void finish(final DotToDot game) {
        for (final DotToDot.Dot dot : List.copyOf(game.getDots())) {
            game.choose(dot);
        }
    }

    @Test
    void shouldHaveEightNamedFiguresOfEachLevelWithTheirDots() {
        final List<DotFigure> figures = DotFigure.all();
        assertEquals(24, figures.size());
        final Set<String> names = new HashSet<>();
        for (final DotFigure figure : figures) {
            assertTrue(names.add(figure.name()), figure.name());
            final int dots = figure.points().size();
            switch (figure.level()) {
                case "EASY" -> assertTrue(dots >= 4 && dots <= 7, figure.name());
                case "NORMAL" -> assertTrue(dots >= 8 && dots <= 12, figure.name());
                case "HARD" -> assertTrue(dots >= 13 && dots <= 20, figure.name());
                default -> fail(figure.level());
            }
            for (final double[] point : figure.points()) {
                assertTrue(point[0] >= 0 && point[0] <= 1 && point[1] >= 0 && point[1] <= 1, figure.name());
            }
        }
        for (final String level : List.of("EASY", "NORMAL", "HARD")) {
            assertEquals(8, figures.stream().filter(figure -> figure.level().equals(level)).count());
        }
    }

    @Test
    void shouldJoinTheFirstDotAfterTheFixationThenDrawALine() throws Exception {
        final DotToDot game = launched(DotLevel.NORMAL, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            final DotToDot.Dot first = game.getDots().get(0);
            Event.fireEvent(first.getZone(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
            steps(game, 0.4);
            assertFalse(first.isJoined());
            steps(game, 0.15);
            assertTrue(first.isJoined());
            assertEquals(1, game.getNext());
            game.choose(game.getDots().get(1));
            assertEquals(2, game.getNext());
            return null;
        });
    }

    /**
     * Before the review, a dot already joined counted a mistake, a wrong one showed nothing, and the game could start
     * at the dot 2.
     */
    @Test
    void shouldCountAMistakeForAWrongDotAndLeaveTheJoinedDotsAlone() throws Exception {
        final DotToDot game = launched(DotLevel.NORMAL, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            game.choose(game.getDots().get(1));
            assertFalse(game.getDots().get(1).isJoined());
            assertEquals(0, game.getNext());
            verify(stats).incrementNumberOfMistakes();
            game.choose(game.getDots().get(0));
            game.choose(game.getDots().get(0));
            verify(stats, times(1)).incrementNumberOfMistakes();
            assertEquals(1, game.getRoundMistakes());
            return null;
        });
    }

    @Test
    void shouldFinishTheFigureAndSayItsName() throws Exception {
        final DotToDot game = launched(DotLevel.EASY, DotLabels.LETTERS, DotEnd.ENDLESS);
        fx(() -> {
            final DotFigure figure = game.getFigure();
            assertEquals("A", DotLabels.LETTERS.label(0));
            finish(game);
            assertEquals(DotToDot.Phase.SHOW, game.getPhase());
            assertEquals(1, game.getDrawings());
            verify(stats).incrementNumberOfGoalsReached();
            verify(translator).translate(figure.name());
            verify(translator).translate("DotItIs");
            steps(game, DotToDot.SHOW + 0.05);
            assertEquals(DotToDot.Phase.PLAY, game.getPhase());
            assertNotSame(figure, game.getFigure());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldHelpWithTheNextDotOnlyAtTheEasyLevel() throws Exception {
        final DotToDot easy = launched(DotLevel.EASY, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            steps(easy, 0.3);
            assertTrue(easy.getDots().get(0).isHelped());
            assertFalse(easy.getDots().get(1).isHelped());
            return null;
        });
        final DotToDot hard = launched(DotLevel.HARD, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            steps(hard, 0.3);
            assertFalse(hard.getDots().get(0).isHelped());
            return null;
        });
    }

    @Test
    void shouldGoUpAfterThreeDrawingsWithoutAMistake() throws Exception {
        final DotToDot game = launched(DotLevel.PROGRESSIVE, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            assertEquals(DotLevel.EASY, game.getFigureLevel());
            // A mistake: the streak starts again
            game.choose(game.getDots().get(1));
            finish(game);
            steps(game, DotToDot.SHOW + 0.05);
            assertEquals(0, game.getStreak());
            for (int i = 0; i < 3; i++) {
                assertEquals(DotLevel.EASY, game.getFigureLevel());
                finish(game);
                steps(game, DotToDot.SHOW + 0.05);
            }
            assertEquals(DotLevel.NORMAL, game.getFigureLevel());
            assertEquals("NORMAL", game.getFigure().level());
            return null;
        });
    }

    @Test
    void shouldKeepTheProportionsOfTheFigure() throws Exception {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1024, 768));
        final DotToDot game = launched(DotLevel.HARD, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            final DotFigure figure = game.getFigure();
            final double minX = game.getDots().stream().mapToDouble(dot -> dot.getPoint().getX()).min().orElseThrow();
            final double maxX = game.getDots().stream().mapToDouble(dot -> dot.getPoint().getX()).max().orElseThrow();
            final double minY = game.getDots().stream().mapToDouble(dot -> dot.getPoint().getY()).min().orElseThrow();
            final double maxY = game.getDots().stream().mapToDouble(dot -> dot.getPoint().getY()).max().orElseThrow();
            final double fMinX = figure.points().stream().mapToDouble(p -> p[0]).min().orElseThrow();
            final double fMaxX = figure.points().stream().mapToDouble(p -> p[0]).max().orElseThrow();
            final double fMinY = figure.points().stream().mapToDouble(p -> p[1]).min().orElseThrow();
            final double fMaxY = figure.points().stream().mapToDouble(p -> p[1]).max().orElseThrow();
            final double expected = (fMaxX - fMinX) * figure.aspect() / (fMaxY - fMinY);
            assertEquals(expected, (maxX - minX) / (maxY - minY), 0.01);
            assertTrue(minX >= 0 && maxX <= 1024 && minY >= 0 && maxY <= 768);
            assertTrue(game.getDotRadius() > 0);
            return null;
        });
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final DotToDot game = launched(DotLevel.EASY, DotLabels.NUMBERS, DotEnd.ENDLESS);
        fx(() -> {
            Event.fireEvent(game.getDots().get(0).getZone(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
            steps(game, 0.9);
            assertFalse(game.getDots().get(0).isJoined());
            steps(game, 0.7);
            assertTrue(game.getDots().get(0).isJoined());
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheDrawingsOfTheVariant() throws Exception {
        final DotToDot game = launched(DotLevel.EASY, DotLabels.NUMBERS, DotEnd.DRAWINGS_3);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                finish(game);
                steps(game, DotToDot.SHOW + 0.05);
            }
            assertEquals(DotToDot.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final DotToDot game = launched(DotLevel.NORMAL, DotLabels.LETTERS, DotEnd.ENDLESS);
        fx(() -> {
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(DotToDot.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        assertEquals(2 * 4 * 4, new DotToDotGameVariantGenerator().getVariants().size());
        final DotToDot grouped = DotToDotGameLauncher.create(gameContext,
            new GroupedGameVariant("DotLetters", "DotEnd5", "DotProgressive", null), stats, 1);
        assertEquals(DotLevel.PROGRESSIVE, grouped.getLevel());
        assertEquals(DotLabels.LETTERS, grouped.getLabels());
        assertEquals(DotEnd.DRAWINGS_5, grouped.getEnd());
        final DotToDot dynamic = DotToDotGameLauncher.create(gameContext,
            new EnumGameVariant<>(DotToDotGameVariant.ORDER_DYNAMIC, DotToDotGameVariant::getLabel), stats, 1);
        assertEquals(DotLevel.PROGRESSIVE, dynamic.getLevel());
        final DotToDot numbers = DotToDotGameLauncher.create(gameContext,
            new EnumGameVariant<>(DotToDotGameVariant.NUMBERS, DotToDotGameVariant::getLabel), stats, 1);
        assertEquals(DotLevel.NORMAL, numbers.getLevel());
    }
}

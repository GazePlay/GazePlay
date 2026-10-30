package net.gazeplay.games.race;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The race of the frogs (reviewed on 30/10/2026): the lanes and the frogs, a bug caught makes the frog of the player
 * hop, the ranking at the end, the statistics.
 */
@ExtendWith(ApplicationExtension.class)
class RaceTest {

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
        when(gameContext.getRoot()).thenReturn(gameArea);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty()).thenReturn(new SimpleDoubleProperty(1));
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getBackgroundStyle()).thenReturn(BackgroundStyle.DARK);
        when(configuration.isBackgroundEnabled()).thenReturn(true);
    }

    /**
     * The race of the test, stopped at its end: its timers (bugs, frogs, ranking) must not run into the next tests.
     */
    private Race launched;

    @AfterEach
    void stopTheRace() throws Exception {
        if (launched != null) {
            WaitForAsyncUtils.asyncFx(launched::dispose).get();
        }
    }

    private Race launchedRace(RaceVariant variant) throws Exception {
        launched = WaitForAsyncUtils.asyncFx(() -> {
            Race race = new Race(gameContext, stats, variant, 1234);
            race.launch();
            return race;
        }).get();
        return launched;
    }

    /**
     * A new race, without the game area cleared between (the game context of the tests does not clear it), does not
     * add the game twice.
     */
    @Test
    void shouldStartANewRaceInTheSameGameArea() throws Exception {
        Race race = launchedRace(RaceVariant.EASY);

        WaitForAsyncUtils.asyncFx(() -> {
            race.dispose();
            race.launch();
        }).get();

        assertEquals(1, gameArea.getChildren().stream().filter(node -> node == race).count());
        assertEquals(Race.BUGS, race.getBugs().size());
    }

    private static MouseEvent mouseEntered() {
        return new MouseEvent(MouseEvent.MOUSE_ENTERED, 1, 1, 1, 1, MouseButton.NONE, 0, false, false, false, false,
            false, false, false, false, false, false, null);
    }

    /**
     * Four frogs on their lanes, the one of the player marked; eight bugs, each a goal to reach.
     */
    @Test
    void shouldStartARaceOfFourFrogsAndEightBugs() throws Exception {
        Race race = launchedRace(RaceVariant.EASY);

        assertEquals(Race.LANES, race.getRacers().size());
        assertEquals(1, race.getRacers().stream().filter(racer -> racer.player).count());
        assertTrue(race.getPlayer().view.getFitHeight() > race.getRacers().get(1).view.getFitHeight(),
            "the frog of the player is the largest");
        assertEquals(Race.BUGS, race.getBugs().size());
        verify(stats, times(Race.BUGS)).incrementNumberOfGoalsToReach();
        verify(stats, never()).incrementNumberOfGoalsReached();
        assertTrue(texts(race).anyMatch(text -> text.equals("RaceYou")));
        assertTrue(texts(race).anyMatch(text -> text.equals("RaceCaught 0")));
    }

    /**
     * A bug entered by the gaze or the mouse is caught once: a goal reached, and the frog of the player hops.
     */
    @Test
    void shouldCatchABugOnceAndMakeThePlayerHop() throws Exception {
        Race race = launchedRace(RaceVariant.EASY);
        Race.Bug bug = race.getBugs().get(0);

        WaitForAsyncUtils.asyncFx(() -> {
            bug.fireEvent(mouseEntered());
            bug.fireEvent(mouseEntered());
        }).get();

        assertEquals(1, race.getCaught());
        assertEquals(1, race.getPlayer().hops);
        verify(stats, times(1)).incrementNumberOfGoalsReached();
        verify(gameContext).updateScore(stats, race);
        assertTrue(texts(race).anyMatch(text -> text.equals("RaceCaught 1")));
    }

    /**
     * When the frog of the player reaches the finish, it wins: the ranking shows it first.
     */
    @Test
    void shouldShowThePlayerFirstWhenItWins() throws Exception {
        Race race = launchedRace(RaceVariant.EASY);

        WaitForAsyncUtils.asyncFx(() -> {
            for (int i = 0; i < Race.HOPS_TO_FINISH; i++) {
                race.hop(race.getPlayer());
            }
        }).get();

        assertTrue(race.isRaceOver());
        String ranking = texts(race).filter(text -> text.startsWith("RaceWon")).findFirst().orElseThrow();
        assertTrue(ranking.contains("\n1. RaceYou"), ranking);
        assertEquals(5, ranking.split("\n").length, ranking);
    }

    /**
     * When another frog arrives first, the race ends too (it used to start again without a word), and no bug can be
     * caught any more.
     */
    @Test
    void shouldEndTheRaceWhenAnotherFrogArrivesFirst() throws Exception {
        Race race = launchedRace(RaceVariant.HARD);
        Race.Racer blue = race.getRacers().get(1);

        WaitForAsyncUtils.asyncFx(() -> {
            race.hop(race.getPlayer());
            for (int i = 0; i < Race.HOPS_TO_FINISH; i++) {
                race.hop(blue);
            }
            race.getBugs().get(0).fireEvent(mouseEntered());
        }).get();

        assertTrue(race.isRaceOver());
        String ranking = texts(race).filter(text -> text.startsWith("RaceLost")).findFirst().orElseThrow();
        assertTrue(ranking.contains("\n1. RaceFrog_blue\n2. RaceYou"), ranking);
        assertEquals(0, race.getCaught());
        verify(stats, never()).incrementNumberOfGoalsReached();
    }

    /**
     * The other frogs are slower in the easy variant; a game saved before the variants is played in the easy one.
     */
    @Test
    void shouldMakeTheOtherFrogsSlowerInTheEasyVariant() {
        assertTrue(RaceVariant.EASY.getMinRaceSeconds() > RaceVariant.NORMAL.getMaxRaceSeconds());
        assertTrue(RaceVariant.NORMAL.getMinRaceSeconds() > RaceVariant.HARD.getMaxRaceSeconds());
        assertEquals(RaceVariant.EASY, FrogsRaceGameLauncher.variantOf(null));
        assertEquals(RaceVariant.HARD, FrogsRaceGameLauncher.variantOf(new EnumGameVariant<>(RaceVariant.HARD)));
    }

    /**
     * At the end of the game, the bugs no longer listen to the gaze and nothing is left.
     */
    @Test
    void shouldReleaseTheBugsWhenDisposed() throws Exception {
        Race race = launchedRace(RaceVariant.NORMAL);

        WaitForAsyncUtils.asyncFx(race::dispose).get();

        verify(gameContext.getGazeDeviceManager(), times(Race.BUGS)).removeEventFilter(any(Node.class));
        assertTrue(race.getBugs().isEmpty());
        assertTrue(race.getChildrenUnmodifiable().isEmpty());
    }

    private static Stream<String> texts(Race race) {
        return race.getChildrenUnmodifiable().stream().filter(Group.class::isInstance)
            .flatMap(group -> ((Group) group).getChildren().stream())
            .filter(Text.class::isInstance).map(node -> ((Text) node).getText());
    }
}

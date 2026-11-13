package net.gazeplay.games.room;

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

import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "La pièce" (reviewed on 13/11/2026, maintainer's rules): the rooms and their objects, the delay and the fixation, the
 * view turned wall by wall, an object found or another one, the hint, all the rooms, the exploration, the end, the
 * variants.
 */
@ExtendWith(ApplicationExtension.class)
class RoomTest {

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

    private Room launched(final RoomMode mode, final RoomPlace place, final RoomEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Room game = new Room(gameContext, stats, mode, place, end, RoomLevel.PICTURE, 8);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final Room game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Turns to the wall of the object asked, then chooses it.
     */
    private static void findTarget(final Room game) {
        while (game.getWall() != game.getTarget().wall()) {
            game.choose(game.getRight());
            steps(game, Room.TURN + 0.04);
        }
        game.choose(game.spot(game.getTarget()));
    }

    @Test
    void shouldHaveFourRoomsOfEightObjectsTwoPerWall() {
        for (final RoomPlace place : RoomPlace.values()) {
            if (place == RoomPlace.ALL) {
                continue;
            }
            assertEquals(8, place.objects().size(), place.toString());
            for (int wall = 0; wall < 4; wall++) {
                final int w = wall;
                assertEquals(2, place.objects().stream().filter(o -> o.wall() == w).count());
            }
        }
        assertEquals("RoomCouchName", RoomObject.COUCH.nameKey());
        assertEquals("RoomWhereTshirt", RoomObject.TSHIRT.questionKey());
    }

    @Test
    void shouldChooseNothingDuringTheDelayThenAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Room game = launched(RoomMode.EXPLORE, RoomPlace.LIVING, RoomEnd.ENDLESS);
        fx(() -> {
            game.choose(game.getRight());
            assertEquals(0, game.getWall());
            steps(game, 1.02);
            game.lookAt(game.getRight());
            steps(game, 0.3);
            game.lookAway(game.getRight());
            steps(game, 0.5);
            assertEquals(Room.Phase.PLAY, game.getPhase());
            game.lookAt(game.getRight());
            steps(game, 0.52);
            assertEquals(Room.Phase.TURN, game.getPhase());
            assertEquals(1, game.getWall());
            return null;
        });
    }

    @Test
    void shouldTurnWallByWallAndComeBackToTheFirst() throws Exception {
        final Room game = launched(RoomMode.EXPLORE, RoomPlace.BEDROOM, RoomEnd.ENDLESS);
        fx(() -> {
            game.choose(game.getLeft());
            assertEquals(3, game.getWall());
            // Nothing is chosen while the view slides
            game.choose(game.getLeft());
            assertEquals(3, game.getWall());
            steps(game, Room.TURN + 0.04);
            assertEquals(Room.Phase.PLAY, game.getPhase());
            assertEquals(RoomObject.BALL, game.getSpots().get(0).getObject());
            game.choose(game.getRight());
            steps(game, Room.TURN + 0.04);
            assertEquals(0, game.getWall());
            assertEquals(RoomObject.BED, game.getSpots().get(0).getObject());
            return null;
        });
    }

    @Test
    void shouldFindTheObjectOrCountAnotherOneAsAMistake() throws Exception {
        final Room game = launched(RoomMode.SEARCH, RoomPlace.KITCHEN, RoomEnd.ENDLESS);
        fx(() -> {
            final RoomObject first = game.getTarget();
            assertEquals(RoomPlace.KITCHEN, first.getPlace());
            final Room.Spot other = game.getSpots().stream()
                .filter(s -> s.getObject() != null && s.getObject() != first).findFirst().orElseThrow();
            game.choose(other);
            assertTrue(other.isShaking());
            assertTrue(other.isNameShown());
            verify(stats).incrementNumberOfMistakes();
            verify(gameContext.getSoundManager()).add("data/dice/sounds/wrong.wav");
            findTarget(game);
            assertEquals(Room.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getFound());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            steps(game, Room.REWARD + 0.04);
            assertEquals(Room.Phase.PLAY, game.getPhase());
            assertNotEquals(first, game.getTarget());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldBlinkTheArrowLeadingToTheObjectAfterAWhile() throws Exception {
        final Room game = launched(RoomMode.SEARCH, RoomPlace.BATHROOM, RoomEnd.ENDLESS);
        fx(() -> {
            // Away from the object: its wall seen from the opposite side
            while (game.getWall() == game.getTarget().wall()) {
                game.choose(game.getRight());
                steps(game, Room.TURN + 0.04);
            }
            steps(game, Room.HINT - 1);
            assertFalse(game.isHintShown());
            steps(game, 1.1);
            assertTrue(game.isHintShown());
            final int distance = (game.getTarget().wall() - game.getWall() + 4) % 4;
            assertEquals(distance != 3, game.isHintRight());
            return null;
        });
    }

    @Test
    void shouldGoToTheRoomOfEachQuestionWithAllTheRooms() throws Exception {
        final Room game = launched(RoomMode.SEARCH, RoomPlace.ALL, RoomEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < 6; i++) {
                assertEquals(game.getTarget().getPlace(), game.getPlace());
                findTarget(game);
                steps(game, Room.REWARD + Room.TURN + 0.1);
            }
            assertEquals(6, game.getFound());
            return null;
        });
    }

    @Test
    void shouldNameEachObjectInTheExploration() throws Exception {
        final Room game = launched(RoomMode.EXPLORE, RoomPlace.LIVING, RoomEnd.OBJECTS_5);
        fx(() -> {
            assertEquals(RoomEnd.ENDLESS, game.getEnd());
            assertNull(game.getTarget());
            final Room.Spot couch = game.spot(RoomObject.COUCH);
            game.choose(couch);
            assertTrue(couch.isJumping());
            assertEquals("RoomCouchName", couch.getName());
            steps(game, Room.NAME + 0.04);
            assertFalse(couch.isNameShown());
            verify(stats, never()).incrementNumberOfMistakes();
            verify(stats, never()).incrementNumberOfGoalsReached();
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheObjectsOfTheVariant() throws Exception {
        final Room game = launched(RoomMode.SEARCH, RoomPlace.LIVING, RoomEnd.OBJECTS_5);
        fx(() -> {
            for (int i = 0; i < 5; i++) {
                findTarget(game);
                steps(game, Room.REWARD + 0.04);
            }
            assertEquals(5, game.getFound());
            assertEquals(Room.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final Room game = launched(RoomMode.SEARCH, RoomPlace.LIVING, RoomEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(Room.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndALaunchWithoutVariant() {
        final Set<IGameVariant> variants = new RoomGameVariantGenerator().getVariants();
        assertEquals(4 * 2 * 5 + 4, variants.size());
        final Room search = RoomGameLauncher.create(gameContext,
            new GroupedGameVariant("RoomSearch", "RoomEnd10", "RoomAll", null, "RoomWordOnly"), stats, 1);
        assertEquals(RoomMode.SEARCH, search.getMode());
        assertEquals(RoomPlace.ALL, search.getChoice());
        assertEquals(RoomEnd.OBJECTS_10, search.getEnd());
        assertEquals(RoomLevel.WORD, search.getLevel());
        final Room before = RoomGameLauncher.create(gameContext, null, stats, 1);
        assertEquals(RoomMode.EXPLORE, before.getMode());
        assertEquals(RoomPlace.LIVING, before.getChoice());
    }
}

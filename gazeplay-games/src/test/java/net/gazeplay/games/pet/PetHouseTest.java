package net.gazeplay.games.pet;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
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

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pet (reviewed on 30/10/2026): the pictures, the gauges emptying, the sad Biboule and its bubble, and the activity of
 * each room filling its gauge. The ticks of the game are run by hand (speed 0 for its timer).
 */
@ExtendWith(ApplicationExtension.class)
class PetHouseTest {

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    private PetHouse game;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty()).thenReturn(new SimpleDoubleProperty(0));
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(100);
        game = WaitForAsyncUtils.asyncFx(() -> {
            gameArea.getChildren().clear();
            final PetHouse house = new PetHouse(gameContext, stats, 5);
            launchedGames.add(house).launch();
            return house;
        }).get();
    }

    private void fx(final Runnable action) throws Exception {
        WaitForAsyncUtils.asyncFx(action).get();
    }

    private void ticks(final int count) throws Exception {
        fx(() -> {
            for (int i = 0; i < count; i++) {
                game.tick();
            }
        });
    }

    private static Point2D center(final Bounds bounds) {
        return new Point2D(bounds.getCenterX(), bounds.getCenterY());
    }

    @Test
    void shouldHaveItsPictures() {
        for (final PetHouse.Room room : PetHouse.Room.values()) {
            assertNotNull(ClassLoader.getSystemResource("data/pet/images/" + room.picture), room.picture);
            assertNotNull(ClassLoader.getSystemResource("data/pet/images/" + room.object), room.object);
            assertNotNull(ClassLoader.getSystemResource("data/icons/" + room.icon + ".svg"), room.icon);
        }
        for (final String picture : new String[]{"bowl.png", "spoon-full.png", "sad.png", "body.png", "wing.png"}) {
            assertNotNull(ClassLoader.getSystemResource("data/pet/images/" + picture), picture);
        }
    }

    /**
     * A gauge empties in 2 minutes; each quarter lost is a goal to reach; under a quarter, the Biboule is sad and the
     * bubble shows.
     */
    @Test
    void shouldEmptyTheGaugesAndMakeTheBibouleSad() throws Exception {
        assertEquals(1.0, game.getLevels().get(PetHouse.Need.HUNGER));
        assertFalse(game.isBubbleShown());

        ticks(300); // 30 s
        assertEquals(0.75, game.getLevels().get(PetHouse.Need.HUNGER), 0.001);
        assertFalse(game.getPet().isSad());

        ticks(620); // 92 s in all
        assertTrue(game.getLevels().get(PetHouse.Need.HUNGER) < PetHouse.LOW);
        assertTrue(game.getPet().isSad());
        assertTrue(game.isBubbleShown());
        // 3 needs, 1 quarter lost at 30 s and 2 more before 92 s
        verify(stats, times(9)).incrementNumberOfGoalsToReach();

        ticks(400); // empty, never below 0
        assertEquals(0.0, game.getLevels().get(PetHouse.Need.HUNGER));
    }

    /**
     * In the kitchen: the gaze on the bowl fills the spoon, then on the mouth feeds the Biboule.
     */
    @Test
    void shouldFeedWithTheSpoon() throws Exception {
        ticks(600);
        fx(() -> game.open(PetHouse.Room.KITCHEN));
        fx(() -> game.pointerAt(center(game.getBowl().getBoundsInParent())));
        assertTrue(game.isSpoonFull());

        fx(() -> game.pointerAt(center(game.getPet().localToParent(game.getPet().getMouth().getBoundsInParent()))));
        assertFalse(game.isSpoonFull());
        assertEquals(0.75, game.getLevels().get(PetHouse.Need.HUNGER), 0.001);
        verify(stats).incrementNumberOfGoalsReached();
    }

    /**
     * In the bathroom: the shower above the Biboule for 3 s washes it.
     */
    @Test
    void shouldWashWithTheShower() throws Exception {
        ticks(600);
        final double before = game.getLevels().get(PetHouse.Need.CLEANLINESS);
        fx(() -> game.open(PetHouse.Room.BATHROOM));
        final Bounds pet = game.getPet().getBoundsInParent();
        fx(() -> game.pointerAt(new Point2D(pet.getCenterX(), pet.getMinY() - 20)));
        ticks(30);

        assertEquals(before - 30 * 100 / 120_000.0 + PetHouse.GAIN,
            game.getLevels().get(PetHouse.Need.CLEANLINESS), 0.001);
    }

    /**
     * In the garden: the ball on the Biboule makes it play.
     */
    @Test
    void shouldPlayWithTheBall() throws Exception {
        ticks(600);
        fx(() -> game.open(PetHouse.Room.GARDEN));
        fx(() -> game.pointerAt(center(game.getPet().getBoundsInParent())));
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0.75, game.getLevels().get(PetHouse.Need.PLAY), 0.001);
    }

    /**
     * Point 80: the pictures of the dishes and of the toys, and the sounds, are shipped.
     */
    @Test
    void shouldHaveThePicturesOfTheChoicesAndTheSounds() {
        for (final PetHouse.Food food : PetHouse.Food.values()) {
            assertNotNull(ClassLoader.getSystemResource("data/pet/images/" + food.dish), food.dish);
            assertNotNull(ClassLoader.getSystemResource("data/pet/images/" + food.held), food.held);
        }
        for (final PetHouse.Toy toy : PetHouse.Toy.values()) {
            assertNotNull(ClassLoader.getSystemResource("data/pet/images/" + toy.picture), toy.picture);
        }
        for (final String sound : new String[]{"stroke", "munch", "splash", "boing", "gain"}) {
            assertNotNull(ClassLoader.getSystemResource(PetHouse.SOUNDS + sound + ".wav"), sound);
        }
    }

    /**
     * Point 80: stroking the Biboule chirps; a bite crunches; a gauge filled rings.
     */
    @Test
    void shouldSoundWhatWorks() throws Exception {
        fx(() -> game.pointerAt(center(game.getPet().getBoundsInParent())));
        verify(gameContext.getSoundManager()).add(PetHouse.SOUNDS + "stroke.wav");
        ticks(600);
        fx(() -> game.open(PetHouse.Room.KITCHEN));
        fx(() -> game.pointerAt(center(game.getBowl().getBoundsInParent())));
        fx(() -> game.pointerAt(center(game.getPet().localToParent(game.getPet().getMouth().getBoundsInParent()))));
        verify(gameContext.getSoundManager()).add(PetHouse.SOUNDS + "munch.wav");
        verify(gameContext.getSoundManager()).add(PetHouse.SOUNDS + "gain.wav");
    }

    /**
     * Point 80: four dishes on the table; the gaze on one takes it, then on the mouth the Biboule eats it.
     */
    @Test
    void shouldFeedWithTheDishChosen() throws Exception {
        ticks(600);
        fx(() -> game.open(PetHouse.Room.KITCHEN));
        for (final PetHouse.Food food : PetHouse.Food.values()) {
            assertTrue(game.dish(food).isVisible(), food.name());
        }
        fx(() -> game.pointerAt(center(game.dish(PetHouse.Food.CAKE).getBoundsInParent())));
        assertEquals(PetHouse.Food.CAKE, game.getHeldFood());
        fx(() -> game.pointerAt(center(game.dish(PetHouse.Food.APPLE).getBoundsInParent())));
        assertEquals(PetHouse.Food.APPLE, game.getHeldFood(), "another dish taken instead");
        fx(() -> game.pointerAt(center(game.getPet().localToParent(game.getPet().getMouth().getBoundsInParent()))));
        assertNull(game.getHeldFood());
        assertEquals(0.75, game.getLevels().get(PetHouse.Need.HUNGER), 0.001);
        fx(() -> game.open(PetHouse.Room.HOME));
        assertFalse(game.dish(PetHouse.Food.APPLE).isVisible());
    }

    /**
     * Point 80: three toys in the garden; a fixation on one takes it; each plays with the Biboule and fills the gauge.
     */
    @Test
    void shouldChooseAToyByAFixationAndPlayWithIt() throws Exception {
        ticks(600);
        fx(() -> game.open(PetHouse.Room.GARDEN));
        assertEquals(PetHouse.Toy.BALL, game.getToy());
        fx(() -> game.pointerAt(game.toyCenter(PetHouse.Toy.HOOP)));
        assertEquals(PetHouse.Toy.BALL, game.getToy(), "not before the fixation");
        // The fixation of the settings: 100 ms, one tick
        ticks(1);
        assertEquals(PetHouse.Toy.HOOP, game.getToy());
        fx(() -> game.pointerAt(center(game.getPet().getBoundsInParent())));
        verify(gameContext.getSoundManager()).add(PetHouse.SOUNDS + "boing.wav");
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0.75, game.getLevels().get(PetHouse.Need.PLAY), 0.001);
        fx(() -> game.chooseToy(PetHouse.Toy.JUGGLING));
        fx(() -> game.pointerAt(new Point2D(0, 0)));
        fx(() -> game.pointerAt(center(game.getPet().getBoundsInParent())));
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1.0, game.getLevels().get(PetHouse.Need.PLAY), 0.001);
    }

    @Test
    void shouldStopFollowingTheGazeAtTheEnd() throws Exception {
        fx(game::dispose);
        verify(gameContext.getGazeDeviceManager(), atLeastOnce()).removeEventFilter(any());
    }
}

package net.gazeplay.games.spotthedifferences;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Spot The Differences (reviewed on 30/10/2026): the scenes and their levels, the fixation (without moving), the end of
 * a picture, the hint, the replay and the target areas.
 */
@ExtendWith(ApplicationExtension.class)
class SpotTheDifferencesTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    SpotTheDifferencesStats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(100);
    }

    /**
     * Each scene: its first picture, and for each level its second picture with 3, 5 or 7 differences inside the
     * picture.
     */
    @Test
    void shouldHaveTheFilesOfEachLevel() {
        final var root = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream("data/spotthedifferences/instances.json")), StandardCharsets.UTF_8))
            .getAsJsonArray();
        assertEquals(8, root.size());
        for (final JsonElement element : root) {
            final JsonObject scene = (JsonObject) element;
            assertNotNull(ClassLoader.getSystemResource("data/spotthedifferences/" + scene.get("image1").getAsString()));
            int count = 3;
            for (final SpotTheDifferencesVariant variant : SpotTheDifferencesVariant.values()) {
                final JsonObject level = scene.getAsJsonObject("levels").getAsJsonObject(variant.name());
                assertNotNull(ClassLoader.getSystemResource("data/spotthedifferences/" + level.get("image2").getAsString()));
                assertEquals(count, level.getAsJsonArray("differences").size(), scene + " " + variant);
                for (final JsonElement diff : level.getAsJsonArray("differences")) {
                    final JsonObject d = (JsonObject) diff;
                    assertTrue(d.get("x").getAsInt() > 0 && d.get("x").getAsInt() < 1000);
                    assertTrue(d.get("y").getAsInt() > 0 && d.get("y").getAsInt() < 1000);
                }
                count += 2;
            }
        }
    }

    private SpotTheDifferences launched(final SpotTheDifferencesVariant variant, final double seed) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            gameArea.getChildren().clear();
            final SpotTheDifferences game = new SpotTheDifferences(gameContext, stats, variant, seed);
            game.launch();
            return game;
        }).get();
    }

    @ParameterizedTest
    @EnumSource(SpotTheDifferencesVariant.class)
    void shouldHaveTheDifferencesOfTheVariant(final SpotTheDifferencesVariant variant) throws Exception {
        final SpotTheDifferences game = launched(variant, 3);

        assertEquals(variant.ordinal() * 2 + 3, game.getDifferences().size());
        verify(stats).incrementNumberOfGoalsToReach(game.getDifferences().size());
        // The circles on both pictures, at the same height, side by side
        final List<Circle> circles = game.getDifferences().get(0).getCircles();
        assertEquals(circles.get(0).getCenterY(), circles.get(1).getCenterY());
        assertTrue(circles.get(0).getCenterX() < 683 && circles.get(1).getCenterX() > 683);
    }

    /**
     * Looking at a difference without moving finds it after the fixation length (it used to need a move); leaving it
     * before does not.
     */
    @Test
    void shouldFindADifferenceByAFixationWithoutMoving() throws Exception {
        final SpotTheDifferences game = launched(SpotTheDifferencesVariant.EASY, 3);
        final Difference first = game.getDifferences().get(0);
        final Difference second = game.getDifferences().get(1);

        WaitForAsyncUtils.asyncFx(() -> {
            first.getCircles().get(1).fireEvent(mouse(MouseEvent.MOUSE_ENTERED));
            second.getCircles().get(0).fireEvent(mouse(MouseEvent.MOUSE_ENTERED));
            second.getCircles().get(0).fireEvent(mouse(MouseEvent.MOUSE_EXITED));
        }).get();
        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(first.isFound());
        assertFalse(second.isFound());
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/spotthedifferences/ding.wav");
    }

    /**
     * All found: "Bravo", then the next picture.
     */
    @Test
    void shouldGoToTheNextPictureWhenAllAreFound() throws Exception {
        final SpotTheDifferences game = launched(SpotTheDifferencesVariant.EASY, 3);
        final String scene = game.getScene();

        WaitForAsyncUtils.asyncFx(() -> new ArrayList<>(game.getDifferences()).forEach(Difference::found)).get();

        final ArgumentCaptor<javafx.event.EventHandler<javafx.event.ActionEvent>> next =
            ArgumentCaptor.forClass(javafx.event.EventHandler.class);
        verify(gameContext).playWinTransition(anyLong(), next.capture());
        verify(gameContext).updateScore(any(), any());
        WaitForAsyncUtils.asyncFx(() -> next.getValue().handle(null)).get();
        assertNotEquals(scene, game.getScene());
        assertEquals(3, game.getDifferences().stream().filter(d -> !d.isFound()).count());
    }

    /**
     * The hint makes a difference left blink; a found one never.
     */
    @Test
    void shouldShowAHintOnADifferenceLeft() throws Exception {
        final SpotTheDifferences game = launched(SpotTheDifferencesVariant.EASY, 3);
        WaitForAsyncUtils.asyncFx(() -> {
            game.getDifferences().get(0).found();
            game.getDifferences().get(1).found();
            game.showHint();
        }).get();

        final Circle hinted = game.getDifferences().get(2).getCircles().get(0);
        assertEquals("-fx-stroke: -gp-accent;", hinted.getStyle());
        assertEquals(20, SpotTheDifferences.HINT_DELAY.toSeconds());
    }

    /**
     * The replay, with the seed of the game, shows the pictures in the same order.
     */
    @Test
    void shouldShowThePicturesInTheSameOrderInTheReplay() throws Exception {
        assertEquals(scenes(42), scenes(42));
    }

    private List<String> scenes(final double seed) throws Exception {
        final SpotTheDifferences game = launched(SpotTheDifferencesVariant.NORMAL, seed);
        final List<String> scenes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            scenes.add(game.getScene());
            WaitForAsyncUtils.asyncFx(() -> {
                game.dispose();
                game.launch();
            }).get();
        }
        assertEquals(8, scenes.stream().distinct().count(), "each scene once");
        return scenes;
    }

    /**
     * Leaving a picture ends the target areas of the differences not found (they had no end).
     */
    @Test
    void shouldEndTheTargetAreasWhenThePictureIsLeft() throws Exception {
        final SpotTheDifferences game = launched(SpotTheDifferencesVariant.EASY, 3);
        WaitForAsyncUtils.asyncFx(game::dispose).get();

        final ArgumentCaptor<ArrayList<TargetAOI>> areas = ArgumentCaptor.forClass(ArrayList.class);
        verify(stats).setTargetAOIList(areas.capture());
        assertEquals(6, areas.getValue().size());
        assertTrue(areas.getValue().stream().allMatch(area -> area.getTimeEnded() > 0));
    }

    private static MouseEvent mouse(final javafx.event.EventType<MouseEvent> type) {
        return new MouseEvent(type, 1, 1, 1, 1, MouseButton.NONE, 0, false, false, false, false, false, false,
            false, false, false, false, null);
    }
}

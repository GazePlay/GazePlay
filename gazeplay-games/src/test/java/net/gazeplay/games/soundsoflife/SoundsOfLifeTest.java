package net.gazeplay.games.soundsoflife;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
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
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Sounds Of Life (reviewed on 30/10/2026): the scenes and their files, the place of the animals with or without the
 * background, the replay, a fixation playing a sound and showing the name.
 */
@ExtendWith(ApplicationExtension.class)
class SoundsOfLifeTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    @Mock
    SoundManager soundManager;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(gameContext.getSoundManager()).thenReturn(soundManager);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(100);
        when(configuration.isBackgroundEnabled()).thenReturn(true);
    }

    /**
     * Each scene: its background, the pictures and the sounds of its animals, and the key of the name of each one.
     */
    @ParameterizedTest
    @EnumSource(SoundsOfLifeGameVariant.class)
    void shouldHaveTheFilesOfEachScene(SoundsOfLifeGameVariant scene) {
        String path = "data/soundsoflife/" + scene.toString().toLowerCase() + "/";
        JsonObject root = (JsonObject) JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(path + "elements.json")), StandardCharsets.UTF_8));
        assertNotNull(ClassLoader.getSystemResource(path + root.get("background").getAsString()));
        assertTrue(root.getAsJsonArray("elements").size() >= 4);
        for (JsonElement element : root.getAsJsonArray("elements")) {
            JsonObject animal = (JsonObject) element;
            assertNotNull(ClassLoader.getSystemResource(path + animal.get("image").getAsString()), animal.toString());
            for (JsonElement sound : animal.getAsJsonArray("sounds")) {
                assertNotNull(ClassLoader.getSystemResource(path + sound.getAsString()), sound.toString());
            }
            assertTrue(animal.get("name").getAsString().startsWith("SoundsOfLife_"));
        }
    }

    private List<SoundMakingEntity> launched(boolean background, double seed) throws Exception {
        when(configuration.isBackgroundEnabled()).thenReturn(background);
        return WaitForAsyncUtils.asyncFx(() -> {
            gameArea.getChildren().clear();
            new SoundsOfLife(gameContext, stats, SoundsOfLifeGameVariant.FARM, seed).launch();
            return gameArea.getChildren().stream().filter(SoundMakingEntity.class::isInstance)
                .map(SoundMakingEntity.class::cast).toList();
        }).get();
    }

    /**
     * Without the background, the animals stay where they are with it (they used to be shifted).
     */
    @Test
    void shouldPlaceTheAnimalsTheSameWayWithoutTheBackground() throws Exception {
        List<Double> withBackground = launched(true, 7).stream()
            .map(entity -> entity.getBoundsInParent().getMinX()).toList();
        List<Double> withoutBackground = launched(false, 7).stream()
            .map(entity -> entity.getBoundsInParent().getMinX()).toList();

        assertEquals(6, withBackground.size());
        assertEquals(withBackground, withoutBackground);
    }

    /**
     * A fixation plays a sound of the animal and shows its name; the replay, with the seed of the game, plays the
     * same sounds (a new seed used to be drawn).
     */
    @Test
    void shouldPlayTheSameSoundsInTheReplay() throws Exception {
        List<String> first = played(launched(true, 1234));
        List<String> second = played(launched(true, 1234));

        assertEquals(first, second);
        verify(stats, times(12)).incrementNumberOfGoalsReached();
    }

    private List<String> played(List<SoundMakingEntity> animals) throws Exception {
        reset(soundManager);
        WaitForAsyncUtils.asyncFx(() -> animals.forEach(SoundMakingEntity::selected)).get();
        ArgumentCaptor<String> sounds = ArgumentCaptor.forClass(String.class);
        verify(soundManager, times(animals.size())).add(sounds.capture());
        WaitForAsyncUtils.sleep(300, java.util.concurrent.TimeUnit.MILLISECONDS);
        for (SoundMakingEntity animal : animals) {
            assertTrue(animal.getNameLabel().getOpacity() > 0, "the name is shown");
            Node text = animal.getNameLabel().getChildren().get(1);
            assertTrue(((javafx.scene.text.Text) text).getText().startsWith("SoundsOfLife_"));
        }
        return sounds.getAllValues();
    }
}

package net.gazeplay.ui.scenes.ingame;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The menu of a game screen (Esc, maquette A of 17/11/2026).
 */
@ExtendWith(ApplicationExtension.class)
class GameMenuViewTest {

    private final Translator translator = mock(Translator.class);

    private Configuration config;

    @BeforeEach
    void setUp() {
        // The keys stand for their texts
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        ActiveConfigurationContext.switchToDefaultUser();
        config = ActiveConfigurationContext.getInstance();
    }

    @AfterEach
    void tearDown() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    private static List<String> texts(final Node node) {
        final List<String> texts = new ArrayList<>();
        if (node instanceof Labeled labeled && labeled.getText() != null) {
            texts.add(labeled.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> texts.addAll(texts(child)));
        }
        return texts;
    }

    /**
     * A game: Resume, Restart, Quit, the sound, the settings of the games with their new names, the progress ring; no
     * background color nor full screen any more (decision of the maintainer of 17/11/2026).
     */
    @Test
    void shouldOfferEverythingButTheDisplayToAGame() {
        final AtomicBoolean resumed = new AtomicBoolean();
        final GameMenuView view = new GameMenuView(translator, Locale.FRENCH, config, null, GameMenuView.Kind.GAME,
            () -> resumed.set(true));

        final List<String> texts = texts(view.getCard());
        assertTrue(texts.containsAll(List.of("GameMenuTitle", "GameMenuTimeNote", "GameMenuResume", "GameMenuRestart",
            "GameMenuQuit", "GameMenuSound", "GameMenuMusicVolume", "GameMenuEffectsVolume", "GameMenuFixation",
            "GameMenuSpeed", "GameMenuSize", "GameMenuRing", "GameMenuRingColor", "GameMenuEscHint")), texts.toString());
        assertFalse(texts.contains("BackgroundColor"));
        assertNotNull(view.getFixationRow());

        view.getResumeButton().fire();
        assertTrue(resumed.get());
    }

    /**
     * An evaluation: only the volume, Resume and Quit (decision of the maintainer of 13/11/2026).
     */
    @Test
    void shouldOfferOnlyTheVolumeResumeAndQuitToAnEvaluation() {
        final GameMenuView view = new GameMenuView(translator, Locale.FRENCH, config, null,
            GameMenuView.Kind.EVALUATION, () -> { });

        final List<String> texts = texts(view.getCard());
        assertTrue(texts.containsAll(List.of("EvalMenuTitle", "GameMenuResume", "EvalMenuQuit", "GameMenuVolume",
            "EvalMenuEscHint")), texts.toString());
        assertFalse(texts.contains("GameMenuRestart"));
        assertFalse(texts.contains("GameMenuFixation"));
        assertFalse(texts.contains("GameMenuRing"));
        assertFalse(texts.contains("GameMenuMusicVolume"));
        assertNull(view.getFixationRow());
    }

    @Test
    void shouldOfferTheSoundAndQuitToTheReplayOfAFile() {
        final GameMenuView view = new GameMenuView(translator, Locale.FRENCH, config, null, GameMenuView.Kind.REPLAY,
            () -> { });

        final List<String> texts = texts(view.getCard());
        assertTrue(texts.containsAll(List.of("GameMenuQuit", "GameMenuSound")), texts.toString());
        assertFalse(texts.contains("GameMenuRestart"));
        assertFalse(texts.contains("GameMenuFixation"));
    }

    /**
     * A setting that does not act on the game is greyed (17/11/2026), e.g. the speed and the size of the elements in
     * Cream pie.
     */
    @Test
    void shouldGreyTheSettingsThatDoNotActOnTheGame() {
        final GameMenuView view = new GameMenuView(translator, Locale.FRENCH, config, null, GameMenuView.Kind.GAME,
            net.gazeplay.MenuSetting.fixationWithSharedRing(), () -> { });

        assertFalse(view.getFixationRow().isDisabled());
        final javafx.scene.layout.VBox game = (javafx.scene.layout.VBox) view.getFixationRow().getParent();
        assertTrue(game.getChildren().get(2).isDisabled(), "speed");
        assertTrue(game.getChildren().get(3).isDisabled(), "size");
    }

    /**
     * The sliders change the settings, as the former side panel.
     */
    @Test
    void shouldChangeTheSettings() {
        final GameMenuView view = new GameMenuView(translator, Locale.FRENCH, config, null, GameMenuView.Kind.GAME,
            () -> { });
        final javafx.scene.control.Slider fixation = (javafx.scene.control.Slider)
            ((javafx.scene.layout.HBox) view.getFixationRow()).getChildren().get(1);

        fixation.setValue(1500);

        assertEquals(1500, config.getFixationLength());
    }

    private static void click(final Node node) {
        javafx.event.Event.fireEvent(node, new javafx.scene.input.MouseEvent(
            javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, javafx.scene.input.MouseButton.PRIMARY, 1,
            false, false, false, false, true, false, false, false, false, false, null));
    }

    /**
     * Maintainer's request of 18/11/2026: a click on the name of a setting puts back its default value.
     */
    @Test
    void shouldPutBackTheDefaultValueWhenTheNameIsClicked() {
        try {
            final GameMenuView view = new GameMenuView(translator, Locale.FRENCH, config, null, GameMenuView.Kind.GAME,
                () -> { });
            final javafx.scene.layout.HBox fixationRow = (javafx.scene.layout.HBox) view.getFixationRow();
            final javafx.scene.layout.HBox speedRow = (javafx.scene.layout.HBox)
                ((javafx.scene.layout.VBox) fixationRow.getParent()).getChildren().get(2);
            ((javafx.scene.control.Slider) fixationRow.getChildren().get(1)).setValue(1500);
            ((javafx.scene.control.Slider) speedRow.getChildren().get(1)).setValue(3);
            assertEquals(4, config.getAnimationSpeedRatio(), 1e-9);

            click(fixationRow.getChildren().get(0));
            click(speedRow.getChildren().get(0));

            assertEquals(Configuration.DEFAULT_VALUE_FIXATION_LENGTH, config.getFixationLength());
            assertEquals(Configuration.DEFAULT_VALUE_ANIMATION_SPEED_RATIO, config.getAnimationSpeedRatio(), 1e-9);
        } finally {
            config.resetGameSettings();
        }
    }
}

package net.gazeplay.ui.menus;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.ConfigurationSource;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The button of the theme, on every page out of the games (maintainer's request of 28/10/2026), and the quit button of
 * the menu pages, which asks first (user's feedback of 6/11/2026).
 */
@ExtendWith(ApplicationExtension.class)
class MenuButtonsTest {

    private final GazePlay gazePlay = mock(GazePlay.class);
    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
    }

    @AfterEach
    void restoreTheTheme() {
        ActiveConfigurationContext.switchToDefaultUser();
        ActiveConfigurationContext.getInstance().setDarkThemeEnabled(false);
    }

    /**
     * A click switches the theme of the current profile, saves it, applies the stylesheets of the dark theme to the
     * page, and offers to go back to the light theme.
     */
    @Test
    void shouldSwitchTheTheme() throws Exception {
        ActiveConfigurationContext.switchToDefaultUser();
        ActiveConfigurationContext.getInstance().setDarkThemeEnabled(false);
        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        Scene scene = new Scene(new Pane());
        when(gazePlay.getPrimaryScene()).thenReturn(scene);
        when(gazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1280, 800));

        Button button = WaitForAsyncUtils.asyncFx(() -> MenuButtons.themeButton(gazePlay, translator)).get();
        assertEquals("ThemeDark", button.getTooltip().getText());

        WaitForAsyncUtils.asyncFx(button::fire).get();

        assertTrue(ActiveConfigurationContext.getInstance().isDarkThemeEnabled());
        assertTrue(ConfigurationSource.createFromDefaultProfile().isDarkThemeEnabled(), "Not saved");
        assertTrue(scene.getStylesheets().contains("data/stylesheets/base-dark.css"));
        assertEquals("ThemeLight", button.getTooltip().getText());

        WaitForAsyncUtils.asyncFx(button::fire).get();

        assertFalse(ActiveConfigurationContext.getInstance().isDarkThemeEnabled());
        assertTrue(scene.getStylesheets().contains("data/stylesheets/base-light.css"));
    }

    @Test
    void shouldQuitOnlyAfterTheQuestionIsAccepted() throws Exception {
        MenuMessageDialog question = pressTheQuitButton();

        assertEquals("QuitConfirmMessage", question.getMessageLabel().getText());
        assertEquals("QuitConfirmTitle", question.getTitle());
        assertEquals("Exit", question.getOkButton().getText());
        verify(gazePlay, never()).quit();

        WaitForAsyncUtils.asyncFx(() -> question.getOkButton().fire()).get();
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(question.isShowing());
        verify(gazePlay).quit();
    }

    @Test
    void shouldStayWhenTheQuestionIsCancelled() throws Exception {
        MenuMessageDialog question = pressTheQuitButton();

        WaitForAsyncUtils.asyncFx(() -> question.getCancelButton().fire()).get();
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(question.isShowing());
        verify(gazePlay, never()).quit();
    }

    /**
     * A wrong key must not quit: Enter cancels, and the keyboard cannot reach the button that quits.
     */
    @Test
    void shouldCancelWithTheEnterKey(FxRobot robot) throws Exception {
        MenuMessageDialog question = pressTheQuitButton();

        assertFalse(question.getOkButton().isDefaultButton());
        assertFalse(question.getOkButton().isFocusTraversable());
        assertTrue(question.getCancelButton().isDefaultButton());

        WaitForAsyncUtils.asyncFx(question::requestFocus).get();
        robot.type(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(question.isShowing());
        assertFalse(question.isAccepted());
        verify(gazePlay, never()).quit();
    }

    @Test
    void shouldStayWhenTheQuestionIsClosed() throws Exception {
        MenuMessageDialog question = pressTheQuitButton();

        WaitForAsyncUtils.asyncFx(question::close).get();
        WaitForAsyncUtils.waitForFxEvents();

        verify(gazePlay, never()).quit();
    }

    /**
     * @return the question: it is shown and waited for, so the button is pressed later on the JavaFX thread
     */
    private MenuMessageDialog pressTheQuitButton() throws Exception {
        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gazePlay.getPrimaryStage()).thenReturn(stage);
        Button button = WaitForAsyncUtils.asyncFx(() -> MenuButtons.confirmedQuitButton(gazePlay, translator)).get();

        Platform.runLater(button::fire);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> Window.getWindows().stream()
            .anyMatch(MenuMessageDialog.class::isInstance));
        return WaitForAsyncUtils.asyncFx(() -> Window.getWindows().stream().filter(MenuMessageDialog.class::isInstance)
            .map(MenuMessageDialog.class::cast).findFirst().orElseThrow()).get();
    }
}

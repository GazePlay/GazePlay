package net.gazeplay.ui.menus;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.ConfigurationSource;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The button of the theme, on every page out of the games (maintainer's request of 28/10/2026).
 */
@ExtendWith(ApplicationExtension.class)
class MenuButtonsTest {

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
        GazePlay gazePlay = mock(GazePlay.class);
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
}

package net.gazeplay.ui.menus;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.MenuUtils;
import net.gazeplay.components.CssUtil;

/**
 * Buttons shared by the menus of the games and of the evaluations.
 */
public final class MenuButtons {

    private MenuButtons() {
    }

    /**
     * @return the round button switching the full screen (violet), its icon and tooltip following the state of the
     * window
     */
    public static Button fullScreenButton(final GazePlay gazePlay, final Translator translator) {
        final Button button = MenuStyle.roundButton(MenuIcon.FULLSCREEN, "violet", "", gazePlay::toggleFullScreen);
        final Runnable update = () -> {
            final boolean fullScreen = gazePlay.isFullScreen();
            button.setGraphic((fullScreen ? MenuIcon.FULLSCREEN_EXIT : MenuIcon.FULLSCREEN).create(40));
            final String text = translator.translate(fullScreen ? "Exit FullScreen" : "Enter FullScreen");
            button.setText(text);
            button.getTooltip().setText(text);
        };
        update.run();
        gazePlay.getFullScreenProperty().addListener((observable, oldValue, newValue) -> update.run());
        return button;
    }

    /**
     * @return the round button switching between the light and the dark theme (gold), on every page out of the games
     * (maintainer's request of 28/10/2026: no longer in the settings). It shows the active theme (a sun on the light
     * theme, a moon on the dark one, maintainer's choice), its tooltip the theme it switches to; the DARK_THEME_ENABLED
     * setting of the current profile is saved, and the stylesheets of the page are applied again at once
     */
    public static Button themeButton(final GazePlay gazePlay, final Translator translator) {
        final Button button = MenuStyle.roundButton(MenuIcon.LIGHT_THEME, "gold", "", () -> {
        });
        final Runnable update = () -> {
            final boolean dark = ActiveConfigurationContext.getInstance().isDarkThemeEnabled();
            button.setGraphic((dark ? MenuIcon.DARK_THEME : MenuIcon.LIGHT_THEME).create(40));
            final String text = translator.translate(dark ? "ThemeLight" : "ThemeDark");
            button.setText(text);
            button.getTooltip().setText(text);
        };
        button.setOnAction(e -> {
            final Configuration config = ActiveConfigurationContext.getInstance();
            config.setDarkThemeEnabled(!config.isDarkThemeEnabled());
            CssUtil.setPreferredStylesheets(config, gazePlay.getPrimaryScene());
            update.run();
        });
        update.run();
        return button;
    }

    /**
     * @return the version of GazePlay Learning, as a link opening its licence in a window of the menus
     */
    public static Button versionButton(final GazePlay gazePlay, final Translator translator) {
        final Button versionButton = new Button(MenuUtils.versionText(), MenuIcon.INFO.create(20));
        versionButton.getStyleClass().add("gp-link");
        versionButton.setTooltip(MenuStyle.tooltip(translator.translate("License")));
        versionButton.setOnAction(e -> showLicense(gazePlay, translator));
        return versionButton;
    }

    /**
     * @return the name of the current profile, as shown to the user ("Default user" for the default profile)
     */
    public static String currentProfileName(final Translator translator) {
        final String userName = ActiveConfigurationContext.getInstance().getUserName();
        return userName == null || userName.isEmpty() ? translator.translate("DefaultUser") : userName;
    }

    /**
     * Shows the licence of GazePlay Learning in a window of the menus.
     */
    private static void showLicense(final GazePlay gazePlay, final Translator translator) {
        final TextArea text = new TextArea(MenuUtils.licenseText());
        text.setEditable(false);
        text.setWrapText(true);
        text.setPrefSize(720, 480);
        text.getStyleClass().add("gp-license");
        final VBox content = new VBox(text);
        content.setPadding(new Insets(16, 24, 24, 24));

        final Stage dialog = new Stage();
        dialog.initOwner(gazePlay.getPrimaryStage());
        MenuDialog.undecorate(dialog);
        dialog.setTitle(translator.translate("License"));
        dialog.setScene(MenuDialog.createScene(dialog, content, translator.translate("Close"), dialog::close));
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), dialog.getScene());
        dialog.show();
    }
}

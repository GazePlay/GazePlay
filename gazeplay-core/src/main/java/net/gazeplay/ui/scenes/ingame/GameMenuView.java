package net.gazeplay.ui.scenes.ingame;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;
import net.gazeplay.MenuSetting;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.AnimationSpeedRatioControl;
import net.gazeplay.ui.ElementSizeControl;
import net.gazeplay.ui.FixationLengthControl;
import net.gazeplay.ui.GameMenuControls;
import net.gazeplay.ui.ProgressBarControl;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.util.Locale;
import java.util.function.DoubleFunction;

/**
 * The window of the menu of a game screen, opened with Esc (refonte of 17/11/2026, maquette A chosen by the
 * maintainer), in the look of the menus (menus.css, light and dark themes): a card in the middle of the darkened game.
 * <ul>
 *   <li>at the top, the title and "the time does not count during the menu" (format 6 of the results);</li>
 *   <li>the buttons Resume, Restart and Quit the game;</li>
 *   <li>the cards Sound (music and volumes), Game (fixation length, speed and size of the elements) and Progress ring
 *   (size and color). The background color and the full screen are only in the settings of the main menu (decision
 *   of the maintainer of 17/11/2026: the games that draw their own background hide that color).</li>
 * </ul>
 * During an evaluation, only the volume, Resume and Quit (decision of the maintainer of 13/11/2026): the durations of
 * its screens come from the evaluation. The settings keep the controls of the former side panel (same settings, same
 * keyboard shortcuts during the game: Alt, Ctrl or Shift with Up and Down).
 */
final class GameMenuView {

    /**
     * What the menu shows.
     */
    enum Kind {
        /** A game: everything. */
        GAME,
        /** The replay of a replayData.json file: the sound and Quit. */
        REPLAY,
        /** An evaluation: the volume, Resume and Quit. */
        EVALUATION
    }

    private final Translator translator;

    private final Locale locale;

    private final java.util.Set<MenuSetting> acting;

    @Getter(AccessLevel.PACKAGE)
    private final VBox card;

    @Getter(AccessLevel.PACKAGE)
    private final Button resumeButton;

    @Getter(AccessLevel.PACKAGE)
    private final Button restartButton;

    @Getter(AccessLevel.PACKAGE)
    private final Button quitButton;

    /**
     * The row of the fixation length, which a game can take away (Race), or null when the menu has none.
     */
    @Getter(AccessLevel.PACKAGE)
    private Node fixationRow;

    /**
     * @param scene where the keyboard shortcuts of the settings are registered, null for none
     */
    GameMenuView(final Translator translator, final Locale locale, final Configuration config, final Scene scene,
                 final Kind kind, final Runnable resume) {
        this(translator, locale, config, scene, kind, java.util.EnumSet.allOf(MenuSetting.class), resume);
    }

    /**
     * @param acting the settings acting on the game: the others are greyed (maintainer's request of 17/11/2026)
     */
    GameMenuView(final Translator translator, final Locale locale, final Configuration config, final Scene scene,
                 final Kind kind, final java.util.Set<MenuSetting> acting, final Runnable resume) {
        this.translator = translator;
        this.locale = locale;
        this.acting = acting;

        final Label title = new Label(text(kind == Kind.EVALUATION ? "EvalMenuTitle" : "GameMenuTitle"));
        title.getStyleClass().add("gp-game-menu-title");
        final Label note = new Label(text("GameMenuTimeNote"));
        note.getStyleClass().add("gp-game-menu-note");
        note.setWrapText(true);
        final Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        final HBox header = new HBox(14, title, gap, note);
        header.setAlignment(Pos.CENTER_LEFT);

        resumeButton = actionButton(text("GameMenuResume"), MenuIcon.PLAY, "gp-button-primary", resume);
        restartButton = actionButton(text("GameMenuRestart"), MenuIcon.REPLAY, "gp-button-secondary", null);
        quitButton = actionButton(text(kind == Kind.EVALUATION ? "EvalMenuQuit" : "GameMenuQuit"),
            kind == Kind.EVALUATION ? MenuIcon.CLOSE : MenuIcon.HOME,
            kind == Kind.EVALUATION ? "gp-button-danger" : "gp-button-secondary", null);
        final GridPane buttons = new GridPane();
        buttons.setHgap(12);
        if (kind == Kind.GAME) {
            buttons.addRow(0, resumeButton, restartButton, quitButton);
        } else {
            buttons.addRow(0, resumeButton, quitButton);
        }
        buttons.getChildren().forEach(button -> GridPane.setHgrow(button, Priority.ALWAYS));

        final GridPane cards = new GridPane();
        cards.setHgap(12);
        cards.setVgap(12);
        switch (kind) {
            case GAME -> {
                cards.add(soundCard(config, true), 0, 0);
                cards.add(gameCard(config, scene), 1, 0);
                final VBox ring = ringCard(config);
                cards.add(ring, 0, 1, 2, 1);
            }
            case REPLAY -> cards.add(soundCard(config, true), 0, 0);
            case EVALUATION -> cards.add(evaluationSoundCard(config), 0, 0);
        }
        cards.getChildren().forEach(node -> GridPane.setHgrow(node, Priority.ALWAYS));

        final Label hint = new Label(text(kind == Kind.EVALUATION ? "EvalMenuEscHint" : "GameMenuEscHint"));
        hint.getStyleClass().add("gp-game-menu-note");
        final HBox hintLine = new HBox(hint);
        hintLine.setAlignment(Pos.CENTER);

        card = new VBox(18, header, buttons, cards, hintLine);
        card.getStyleClass().add("gp-game-menu");
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        card.setPrefWidth(kind == Kind.EVALUATION ? 560 : 880);
        // The clicks on the card stay in the menu (the backdrop around it lets nothing through either)
        card.setOnMouseClicked(event -> event.consume());
    }

    private String text(final String key) {
        final String translated = translator.translate(key);
        return translated == null ? key : translated;
    }

    private static Button actionButton(final String text, final MenuIcon icon, final String style,
                                       final Runnable action) {
        final Button button = new Button(text, icon.create(22));
        button.getStyleClass().addAll("gp-button", style, "gp-game-menu-action");
        button.setMaxWidth(Double.MAX_VALUE);
        if (action != null) {
            button.setOnAction(event -> action.run());
        }
        return button;
    }

    private static VBox settingsCard(final String title, final Node... rows) {
        final Label label = new Label(title);
        label.getStyleClass().add("gp-game-menu-card-title");
        final VBox card = new VBox(10, label);
        card.getChildren().addAll(rows);
        card.getStyleClass().add("gp-game-menu-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /**
     * Makes the name of a setting put back its default value when clicked (maintainer's request of 18/11/2026), with a
     * tooltip saying so.
     */
    private void resetOnClick(final Label name, final Runnable reset) {
        name.setCursor(javafx.scene.Cursor.HAND);
        name.setTooltip(MenuStyle.tooltip(text("GameMenuResetHint")));
        name.setOnMouseClicked(event -> {
            reset.run();
            event.consume();
        });
    }

    /**
     * @return a row "name, slider, value", the value shown with {@code format}; a click on the name puts the slider
     * back on {@code defaultValue}
     */
    private HBox sliderRow(final String name, final Slider slider, final DoubleFunction<String> format,
                           final double defaultValue) {
        final Label label = new Label(name);
        label.getStyleClass().add("gp-game-menu-label");
        label.setMinWidth(Region.USE_PREF_SIZE);
        resetOnClick(label, () -> slider.setValue(defaultValue));
        // The look shared with the settings of the main menu (GameMenuControls)
        final Label value = GameMenuControls.value(GameMenuControls.styled(slider), format);
        final HBox row = new HBox(12, label, slider, value);
        row.setAlignment(Pos.CENTER_LEFT);
        label.setPrefWidth(170);
        return row;
    }

    /**
     * Greys a row whose setting does not act on this game, with a tooltip saying so.
     */
    private <T extends Node> T greyedUnless(final MenuSetting setting, final T row) {
        if (!acting.contains(setting)) {
            row.setDisable(true);
            row.getStyleClass().add("gp-game-menu-unused");
            if (row instanceof javafx.scene.control.Control control) {
                control.setTooltip(MenuStyle.tooltip(text("GameMenuNotHere")));
            } else {
                javafx.scene.control.Tooltip.install(row, MenuStyle.tooltip(text("GameMenuNotHere")));
            }
        }
        return row;
    }

    /**
     * The card of the sound, shared with the bottom of the menu of the games (GameMenuControls).
     */
    private VBox soundCard(final Configuration config, final boolean withMusic) {
        return GameMenuControls.soundCard(translator, config, withMusic, false);
    }

    /**
     * During an evaluation: the volume of its sounds and videos (the volume of the effects).
     */
    private VBox evaluationSoundCard(final Configuration config) {
        return GameMenuControls.soundCard(translator, config, false, true);
    }

    private VBox gameCard(final Configuration config, final Scene scene) {
        final FixationLengthControl fixation = FixationLengthControl.getInstance();
        final Slider fixationSlider = fixation.createFixationLengthSlider(config, new Label());
        final AnimationSpeedRatioControl speed = AnimationSpeedRatioControl.getInstance();
        final Slider speedSlider = speed.createSpeedEffectSlider(config, new Label());
        final ElementSizeControl size = ElementSizeControl.getInstance();
        final Slider sizeSlider = size.createElementSizeSlider(config, new Label());
        if (scene != null) {
            fixation.registerKeyHandler(scene, fixationSlider);
            speed.registerKeyHandler(scene, speedSlider);
            size.registerKeyHandler(scene, sizeSlider);
        }
        fixationRow = greyedUnless(MenuSetting.FIXATION_LENGTH, sliderRow(text("GameMenuFixation"), fixationSlider,
            value -> String.format(locale, "%.1f s", value / 1000), Configuration.DEFAULT_VALUE_FIXATION_LENGTH));
        return settingsCard(text("GameMenuGame"), fixationRow,
            // 0 on the slider of the speed is the speed by default (× 1)
            greyedUnless(MenuSetting.ANIMATION_SPEED, sliderRow(text("GameMenuSpeed"), speedSlider,
                value -> String.format(locale, "× %.2f", speed.sliderValueToSpeedRatio(value)),
                speed.speedRatioToSliderValue(Configuration.DEFAULT_VALUE_ANIMATION_SPEED_RATIO))),
            greyedUnless(MenuSetting.ELEMENT_SIZE, sliderRow(text("GameMenuSize"), sizeSlider,
                value -> Math.round(value) + " %", Configuration.DEFAULT_VALUE_ELEMENT_SIZE)),
            greyedUnless(MenuSetting.MOVING_FIXATION, movingFixationRow(config)));
    }

    /**
     * @return the row "If the gaze leaves the moving target": starts again from zero, or keeps its progress
     *   (20/11/2026); a click on the name puts back the default (from zero)
     */
    private HBox movingFixationRow(final Configuration config) {
        final HBox choice = GameMenuControls.movingFixationChoice(translator, config);
        final Label label = new Label(text("MovingFixation"));
        label.getStyleClass().add("gp-game-menu-label");
        label.setWrapText(true);
        label.setPrefWidth(170);
        label.setMinWidth(Region.USE_PREF_SIZE);
        resetOnClick(label, () -> GameMenuControls.selectMovingFixation(choice,
            Configuration.DEFAULT_VALUE_MOVING_FIXATION_KEPT));
        final HBox row = new HBox(12, label, choice);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox ringCard(final Configuration config) {
        final ProgressBarControl progress = ProgressBarControl.getInstance();
        final Slider thicknessSlider = progress.createRingThicknessSlider(config, new Label());
        final Label colorName = new Label(text("GameMenuRingColor"));
        colorName.getStyleClass().add("gp-game-menu-label");
        colorName.setPrefWidth(170);
        colorName.setMinWidth(Region.USE_PREF_SIZE);
        final HBox colors = new HBox(8, colorName);
        colors.setAlignment(Pos.CENTER_LEFT);
        final Label[] swatches = progress.createColorLabel(config);
        // The first color, the main color of the menus, is the one by default
        resetOnClick(colorName, () -> {
            config.getProgressBarColorProperty().set(Configuration.RING_COLOR_ACCENT);
            progress.setTextOnLabel(swatches, 0);
        });
        for (final Label swatch : swatches) {
            swatch.getStyleClass().add("gp-game-menu-swatch");
            swatch.setStyle(swatch.getStyle() + " -fx-background-radius: 999; -fx-min-width: 32; -fx-min-height: 32;"
                + " -fx-pref-width: 32; -fx-pref-height: 32; -fx-text-fill: #2B2118;");
            colors.getChildren().add(swatch);
        }
        return settingsCard(text("GameMenuRing"),
            greyedUnless(MenuSetting.RING_THICKNESS,
                sliderRow(text("GameMenuRingThickness"), thicknessSlider, value -> Math.round(value) + " %",
                    Configuration.DEFAULT_VALUE_RING_THICKNESS)),
            greyedUnless(MenuSetting.RING_COLOR, colors));
    }
}

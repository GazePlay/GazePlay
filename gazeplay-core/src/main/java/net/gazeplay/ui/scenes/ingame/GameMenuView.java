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
import javafx.scene.media.MediaPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import net.gazeplay.MenuSetting;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.ui.AnimationSpeedRatioControl;
import net.gazeplay.ui.ElementSizeControl;
import net.gazeplay.ui.FixationLengthControl;
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
     * @return a row "name, slider, value", the value shown with {@code format}
     */
    private HBox sliderRow(final String name, final Slider slider, final DoubleFunction<String> format) {
        final Label label = new Label(name);
        label.getStyleClass().add("gp-game-menu-label");
        label.setMinWidth(Region.USE_PREF_SIZE);
        final Label value = new Label(format.apply(slider.getValue()));
        value.getStyleClass().add("gp-game-menu-value");
        value.setMinWidth(64);
        value.setAlignment(Pos.CENTER_RIGHT);
        slider.valueProperty().addListener((observable, before, now) -> value.setText(format.apply(now.doubleValue())));
        slider.setMinWidth(120);
        slider.setPrefWidth(200);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setShowTickMarks(false);
        slider.getStyleClass().add("gp-game-menu-slider");
        HBox.setHgrow(slider, Priority.ALWAYS);
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

    private String percent(final double ratio) {
        return Math.round(ratio * 100) + " %";
    }

    private VBox soundCard(final Configuration config, final boolean withMusic) {
        final VBox card = settingsCard(text("GameMenuSound"));
        if (withMusic) {
            card.getChildren().add(musicRow());
        }
        card.getChildren().add(sliderRow(text("GameMenuMusicVolume"), volumeSlider(config.getMusicVolume(),
            value -> {
                config.getMusicVolumeProperty().set(value);
                BackgroundMusicManager.getInstance().setVolume(value);
            }), this::percent));
        card.getChildren().add(sliderRow(text("GameMenuEffectsVolume"), volumeSlider(config.getEffectsVolume(),
            value -> config.getEffectsVolumeProperty().set(value)), this::percent));
        return card;
    }

    /**
     * During an evaluation: the volume of its sounds and videos (the volume of the effects).
     */
    private VBox evaluationSoundCard(final Configuration config) {
        return settingsCard(text("GameMenuSound"), sliderRow(text("GameMenuVolume"),
            volumeSlider(config.getEffectsVolume(), value -> config.getEffectsVolumeProperty().set(value)),
            this::percent));
    }

    private static Slider volumeSlider(final double initial, final java.util.function.DoubleConsumer onChange) {
        final Slider slider = new Slider(0, 1, initial);
        slider.setBlockIncrement(0.05);
        slider.valueProperty().addListener((observable, before, now) -> onChange.accept(now.doubleValue()));
        return slider;
    }

    /**
     * @return the title of the music and its buttons previous, play or pause, next
     */
    private HBox musicRow() {
        final BackgroundMusicManager music = BackgroundMusicManager.getInstance();
        final Label name = new Label(text("GameMenuMusic"));
        name.getStyleClass().add("gp-game-menu-label");
        name.setPrefWidth(170);
        name.setMinWidth(Region.USE_PREF_SIZE);
        final Label title = new Label(musicTitle(music));
        title.getStyleClass().add("gp-game-menu-note");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);
        music.getIsMusicChanging().addListener((observable, before, changing) -> {
            if (!changing) {
                title.setText(musicTitle(music));
            }
        });
        final Button previous = MenuStyle.iconButton(MenuIcon.SKIP_PREVIOUS, text("GameMenuPrevious"),
            music::previous);
        final Button playPause = MenuStyle.iconButton(music.isPlaying() ? MenuIcon.PAUSE : MenuIcon.PLAY,
            text(music.isPlaying() ? "GameMenuPause" : "GameMenuPlay"), () -> {
                if (music.isPlaying()) {
                    music.pause();
                } else {
                    music.play();
                }
            });
        music.getIsPlayingProperty().addListener((observable) -> {
            playPause.setGraphic((music.isPlaying() ? MenuIcon.PAUSE : MenuIcon.PLAY).create(32));
            playPause.getTooltip().setText(text(music.isPlaying() ? "GameMenuPause" : "GameMenuPlay"));
            playPause.setAccessibleText(playPause.getTooltip().getText());
        });
        final Button next = MenuStyle.iconButton(MenuIcon.SKIP_NEXT, text("GameMenuNext"), music::next);
        final HBox row = new HBox(8, name, title, previous, playPause, next);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String musicTitle(final BackgroundMusicManager music) {
        final MediaPlayer current = music.getCurrentMusic();
        final String title = BackgroundMusicManager.getMusicTitle(current);
        return title == null ? "" : title;
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
            value -> String.format(locale, "%.1f s", value / 1000)));
        return settingsCard(text("GameMenuGame"), fixationRow,
            greyedUnless(MenuSetting.ANIMATION_SPEED, sliderRow(text("GameMenuSpeed"), speedSlider,
                value -> String.format(locale, "× %.2f", speed.sliderValueToSpeedRatio(value)))),
            // 100 is the size by default
            greyedUnless(MenuSetting.ELEMENT_SIZE,
                sliderRow(text("GameMenuSize"), sizeSlider, value -> Math.round(value) + " %")));
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
        for (final Label swatch : progress.createColorLabel(config)) {
            swatch.getStyleClass().add("gp-game-menu-swatch");
            swatch.setStyle(swatch.getStyle() + " -fx-background-radius: 999; -fx-min-width: 32; -fx-min-height: 32;"
                + " -fx-pref-width: 32; -fx-pref-height: 32; -fx-text-fill: #2B2118;");
            colors.getChildren().add(swatch);
        }
        return settingsCard(text("GameMenuRing"),
            greyedUnless(MenuSetting.RING_THICKNESS,
                sliderRow(text("GameMenuRingThickness"), thicknessSlider, value -> Math.round(value) + " %")),
            greyedUnless(MenuSetting.RING_COLOR, colors));
    }
}

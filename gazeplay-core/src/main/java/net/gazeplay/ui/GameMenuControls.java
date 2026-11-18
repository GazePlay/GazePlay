package net.gazeplay.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * The controls of the menu of a game screen (Esc) shared with the settings of the main menu, so that both look and
 * work the same (maintainer's request of 18/11/2026: the volumes of the settings were different from those of the menu
 * Esc): the sliders in the look of the menus with their value on the right, and the music with its buttons.
 */
public final class GameMenuControls {

    private GameMenuControls() {
    }

    /**
     * Gives a slider the look of the menu Esc: a thin track, a round thumb of the main color, no tick marks.
     */
    public static Slider styled(final Slider slider) {
        slider.setMinWidth(120);
        slider.setPrefWidth(200);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setShowTickMarks(false);
        if (!slider.getStyleClass().contains("gp-game-menu-slider")) {
            slider.getStyleClass().add("gp-game-menu-slider");
        }
        HBox.setHgrow(slider, Priority.ALWAYS);
        return slider;
    }

    /**
     * @return the value of a slider as the menu Esc shows it, on its right, written with {@code format} and followed
     */
    public static Label value(final Slider slider, final DoubleFunction<String> format) {
        final Label value = new Label(format.apply(slider.getValue()));
        value.getStyleClass().add("gp-game-menu-value");
        value.setMinWidth(64);
        value.setAlignment(Pos.CENTER_RIGHT);
        // The value of the slider itself, read at each change: a slider putting itself on a step (the speed) changes its
        // value again during the change
        slider.valueProperty().addListener(observable -> value.setText(format.apply(slider.getValue())));
        return value;
    }

    /**
     * @return a slider in the look of the menu Esc and its value on its right
     */
    public static HBox sliderWithValue(final Slider slider, final DoubleFunction<String> format) {
        final HBox row = new HBox(12, styled(slider), value(slider, format));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(300);
        return row;
    }

    /**
     * @return a volume as the menu Esc shows it, in percent
     */
    public static String percent(final double ratio) {
        return Math.round(ratio * 100) + " %";
    }

    /**
     * @return a slider of a volume, from 0 to 1, calling {@code onChange} with each new value
     */
    public static Slider volumeSlider(final double initial, final DoubleConsumer onChange) {
        final Slider slider = new Slider(0, 1, initial);
        slider.setBlockIncrement(0.05);
        slider.valueProperty().addListener((observable, before, now) -> onChange.accept(now.doubleValue()));
        return slider;
    }

    /**
     * @return the slider of the volume of the music, saved and heard at once
     */
    public static Slider musicVolumeSlider(final Configuration config) {
        return volumeSlider(config.getMusicVolume(), value -> {
            config.getMusicVolumeProperty().set(value);
            BackgroundMusicManager.getInstance().setVolume(value);
        });
    }

    /**
     * @return the slider of the volume of the sounds (and of the sounds and videos of the evaluations), saved at once
     */
    public static Slider effectsVolumeSlider(final Configuration config) {
        return volumeSlider(config.getEffectsVolume(), value -> config.getEffectsVolumeProperty().set(value));
    }

    /**
     * @return the title of the music playing and its buttons previous, play or pause, next
     */
    public static HBox musicPlayer(final Translator translator) {
        final BackgroundMusicManager music = BackgroundMusicManager.getInstance();
        final Label title = new Label(musicTitle(music));
        title.getStyleClass().add("gp-game-menu-note");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);
        // (The properties may be missing on a manager replaced by a mock in the tests)
        if (music.getIsMusicChanging() != null) {
            music.getIsMusicChanging().addListener((observable, before, changing) -> {
                if (!changing) {
                    title.setText(musicTitle(music));
                }
            });
        }
        final Button previous = MenuStyle.iconButton(MenuIcon.SKIP_PREVIOUS, text(translator, "GameMenuPrevious"),
            music::previous);
        final Button playPause = MenuStyle.iconButton(music.isPlaying() ? MenuIcon.PAUSE : MenuIcon.PLAY,
            text(translator, music.isPlaying() ? "GameMenuPause" : "GameMenuPlay"), () -> {
                if (music.isPlaying()) {
                    music.pause();
                } else {
                    music.play();
                }
            });
        if (music.getIsPlayingProperty() != null) {
            music.getIsPlayingProperty().addListener((observable) -> {
                playPause.setGraphic((music.isPlaying() ? MenuIcon.PAUSE : MenuIcon.PLAY).create(32));
                playPause.getTooltip().setText(text(translator, music.isPlaying() ? "GameMenuPause" : "GameMenuPlay"));
                playPause.setAccessibleText(playPause.getTooltip().getText());
            });
        }
        final Button next = MenuStyle.iconButton(MenuIcon.SKIP_NEXT, text(translator, "GameMenuNext"), music::next);
        final HBox row = new HBox(8, title, previous, playPause, next);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * @return the card "Sound" of the menu Esc: the music with its buttons (when {@code withMusic}), then the volume of
     * the music and the volume of the sounds; during an evaluation, the volume of its sounds and videos only. Shown the
     * same in the menu Esc and at the bottom of the menu of the games (maintainer's request of 18/11/2026).
     */
    public static VBox soundCard(final Translator translator, final Configuration config, final boolean withMusic,
                                 final boolean evaluation) {
        return soundCard(translator, config, withMusic, evaluation, true);
    }

    /**
     * @param withTitle whether the card shows its title "Sound": not at the bottom of the menu of the games, where the
     *                  card must stay low (the games above it, on a window of 600 pixels high)
     */
    public static VBox soundCard(final Translator translator, final Configuration config, final boolean withMusic,
                                 final boolean evaluation, final boolean withTitle) {
        final VBox card = new VBox(withTitle ? 10 : 4);
        if (withTitle) {
            final Label title = new Label(text(translator, "GameMenuSound"));
            title.getStyleClass().add("gp-game-menu-card-title");
            card.getChildren().add(title);
        }
        card.getStyleClass().add("gp-game-menu-card");
        card.setMaxWidth(Double.MAX_VALUE);
        if (evaluation) {
            card.getChildren().add(sliderRow(translator, "GameMenuVolume", effectsVolumeSlider(config),
                Configuration.DEFAULT_VALUE_EFFECTS_VOLUME));
            return card;
        }
        if (withMusic) {
            final HBox player = musicPlayer(translator);
            HBox.setHgrow(player, Priority.ALWAYS);
            final HBox music = new HBox(8, name(translator, "GameMenuMusic", null), player);
            music.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(music);
        }
        card.getChildren().add(sliderRow(translator, "GameMenuMusicVolume", musicVolumeSlider(config),
            Configuration.DEFAULT_VALUE_MUSIC_VOLUME));
        card.getChildren().add(sliderRow(translator, "GameMenuEffectsVolume", effectsVolumeSlider(config),
            Configuration.DEFAULT_VALUE_EFFECTS_VOLUME));
        return card;
    }

    /**
     * @return a row of the menu Esc: the name of a volume (a click puts back its default value), its slider, its value
     */
    private static HBox sliderRow(final Translator translator, final String key, final Slider slider,
                                  final double defaultValue) {
        final Label name = name(translator, key, () -> slider.setValue(defaultValue));
        final HBox row = new HBox(12, name, styled(slider), value(slider, GameMenuControls::percent));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * @return the name of a row of the menu Esc; a click on it runs {@code reset} (put back the default value), when
     * there is one
     */
    public static Label name(final Translator translator, final String key, final Runnable reset) {
        final Label name = new Label(text(translator, key));
        name.getStyleClass().add("gp-game-menu-label");
        name.setPrefWidth(170);
        name.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        if (reset != null) {
            name.setCursor(javafx.scene.Cursor.HAND);
            name.setTooltip(MenuStyle.tooltip(text(translator, "GameMenuResetHint")));
            name.setOnMouseClicked(event -> {
                reset.run();
                event.consume();
            });
        }
        return name;
    }

    private static String musicTitle(final BackgroundMusicManager music) {
        final MediaPlayer current = music.getCurrentMusic();
        final String title = BackgroundMusicManager.getMusicTitle(current);
        return title == null ? "" : title;
    }

    private static String text(final Translator translator, final String key) {
        final String translated = translator.translate(key);
        return translated == null ? key : translated;
    }
}

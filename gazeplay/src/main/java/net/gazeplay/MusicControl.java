package net.gazeplay;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.utils.MarqueeText;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class MusicControl {

    private static final String RESOURCES_PATH = "data" + "/" + "common";

    private static final String IMAGES_PATH = RESOURCES_PATH + "/" + "images";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    private static final String PREVIOUS_ICON = IMAGES_PATH + "/" + "previous.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    private static final String NEXT_ICON = IMAGES_PATH + "/" + "skip.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    private static final String PAUSE_ICON = IMAGES_PATH + "/" + "pause.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    private static final String PLAY_ICON = IMAGES_PATH + "/" + "play-button.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    private static final String SPEAKER_ICON = IMAGES_PATH + "/" + "speaker.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    private static final String MUTE_ICON = IMAGES_PATH + "/" + "mute.png";

    private static final int ICON_SIZE = 32;

    private static final double MUSIC_GRID_MAX_WIDTH = 200;

    /**
     * Field used to know if the background music controler has already been built once. This is used to get audio and
     * play it at the beginning.
     */
    private static AtomicBoolean autoplayExecuted = new AtomicBoolean(false);

    @Getter
    private final GazePlay gazePlay;

    /**
     * Fields with listeners from music controler. When need those because when the volume controle is not on stage
     * (i.e. when configuration is shown), it doesn't receive any event from listener (no idea why). Then when it comes
     * back on stage, it needs to be updated.
     */
    private MarqueeText musicName;
    private Button pauseTrack;
    private Button playTrack;
    private double beforeMutedValue;
    
    TitledPane createSpeedEffectsPane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "SpeedEffects");
        pane.setCollapsible(false);

        final BorderPane mainPane = new BorderPane();
        pane.setContent(mainPane);

        final HBox center = new HBox();
        mainPane.setCenter(center);
        center.setSpacing(5);

        Slider speedEffectSlider = createSpeedEffectSlider();

        center.getChildren().add(speedEffectSlider);

        return pane;
    }

    void updateMusicControler() {
        setMusicTitle(musicName);
        if (playTrack != null && pauseTrack != null) {
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            log.info("updating : isPlaying : {}", backgroundMusicManager.isPlaying());
            if (backgroundMusicManager.isPlaying()) {
                playTrack.setVisible(false);
                pauseTrack.setVisible(true);
            } else {
                playTrack.setVisible(true);
                pauseTrack.setVisible(false);
            }
        }
    }

    TitledPane createMusicControlPane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Music");
        pane.setCollapsible(false);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(2);

        Slider volumeSlider = createMediaVolumeSlider();
        grid.add(volumeSlider, 1, 1, 2, 1);

        final Node volumeButtons = createVolumeButton(volumeSlider);
        grid.add(volumeButtons, 0, 1);

        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();

        final MediaPlayer currentMusic = backgroundMusicManager.getCurrentMusic();

        // final Label musicName = new Label(backgroundMusicManager.getMusicTitle(currentMusic));
        musicName = new MarqueeText(BackgroundMusicManager.getMusicTitle(currentMusic));
        grid.add(musicName, 0, 0, 3, 1);
        grid.setMaxWidth(MUSIC_GRID_MAX_WIDTH);
        backgroundMusicManager.getIsMusicChanging().addListener((observable, oldValue, newValue) -> {

            // If we receive a change event and the new value is fales, then it means
            // that the music has been changed (see changingProperty from Slider)
            if (!newValue) {
                setMusicTitle(musicName);
            }
        });

        Image buttonImg = null;
        try {
            buttonImg = new Image(PREVIOUS_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PREVIOUS_ICON);
        }

        Button previousTrack;
        if (buttonImg == null) {
            previousTrack = new Button("<");
        } else {
            previousTrack = new Button("", new ImageView(buttonImg));
        }
        previousTrack.setOnAction((event) -> backgroundMusicManager.previous());

        buttonImg = null;
        try {
            buttonImg = new Image(PAUSE_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PAUSE_ICON);
        }

        if (buttonImg == null) {
            pauseTrack = new Button("||");
        } else {
            pauseTrack = new Button("", new ImageView(buttonImg));
        }
        pauseTrack.setOnAction((event) -> backgroundMusicManager.pause());

        buttonImg = null;
        try {
            buttonImg = new Image(PLAY_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PLAY_ICON);
        }

        if (buttonImg == null) {
            playTrack = new Button("|>");
        } else {
            playTrack = new Button("", new ImageView(buttonImg));
        }
        playTrack.setOnAction((event) -> backgroundMusicManager.play());

        if (backgroundMusicManager.isPlaying()) {
            playTrack.setVisible(false);
            pauseTrack.setVisible(true);
        } else {
            playTrack.setVisible(true);
            pauseTrack.setVisible(false);
        }

        final StackPane stackPane = new StackPane(pauseTrack, playTrack);

        buttonImg = null;
        try {
            buttonImg = new Image(NEXT_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + NEXT_ICON);
        }

        backgroundMusicManager.getIsPlayingPoperty().addListener((observable) -> {

            if (backgroundMusicManager.isPlaying()) {
                playTrack.setVisible(false);
                pauseTrack.setVisible(true);
            } else {
                playTrack.setVisible(true);
                pauseTrack.setVisible(false);
            }
        });

        Button nextTrack;
        if (buttonImg == null) {
            nextTrack = new Button(">");
        } else {
            nextTrack = new Button("", new ImageView(buttonImg));
        }
        nextTrack.setOnAction((event) -> backgroundMusicManager.next());

        grid.add(previousTrack, 0, 2);
        grid.add(stackPane, 1, 2);
        grid.add(nextTrack, 2, 2);

        pane.setContent(grid);

        if (autoplayExecuted.compareAndSet(false, true)) {
            if (backgroundMusicManager.getPlaylist().isEmpty()) {
                final Configuration configuration = ActiveConfigurationContext.getInstance();
                backgroundMusicManager.getAudioFromFolder(configuration.getMusicFolder());
            }
            backgroundMusicManager.changeMusic(0);
            backgroundMusicManager.play();

            // We need to manually set the music title for the first set up
            setMusicTitle(musicName);
        }

        log.info("Music panel created");

        return pane;
    }

    private Node createVolumeButton(final Slider volumeSlider) {
        Image buttonImg = null;
        try {
            buttonImg = new Image(SPEAKER_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + SPEAKER_ICON);
        }

        Button muteButton;
        if (buttonImg == null) {
            muteButton = new I18NButton(getGazePlay().getTranslator(), "Volume");
        } else {
            muteButton = new Button(null, new ImageView(buttonImg));
        }

        Image muteImg = null;
        try {
            muteImg = new Image(MUTE_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + MUTE_ICON);
        }

        Button unmuteButton;
        if (muteImg == null) {
            unmuteButton = new I18NButton(getGazePlay().getTranslator(), "Volume");
        } else {
            unmuteButton = new Button(null, new ImageView(muteImg));
        }

        final boolean muted = volumeSlider.getValue() == 0;
        muteButton.setVisible(!muted);
        unmuteButton.setVisible(muted);

        if (muted) {
            beforeMutedValue = Configuration.DEFAULT_VALUE_MUSIC_VOLUME;
        } else {
            beforeMutedValue = volumeSlider.getValue();
        }

        muteButton.setOnAction((event) -> {
            beforeMutedValue = volumeSlider.getValue();
            volumeSlider.setValue(0);
        });

        unmuteButton.setOnAction((event) -> volumeSlider.setValue(beforeMutedValue));

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            final boolean localMuted = newValue.doubleValue() == 0;
            muteButton.setVisible(!localMuted);
            unmuteButton.setVisible(localMuted);
        });

        final StackPane volumeStackPane = new StackPane(muteButton, unmuteButton);
        return volumeStackPane;
    }

    private Slider createMediaVolumeSlider() {

        final Configuration config = ActiveConfigurationContext.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(false);
        slider.setValue(config.getMusicVolume());
        config.getMusicVolumeProperty().bindBidirectional(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> config.saveConfigIgnoringExceptions());
        return slider;
    }

    TitledPane createEffectsVolumePane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "EffectsVolume");
        pane.setCollapsible(false);

        final BorderPane mainPane = new BorderPane();
        pane.setContent(mainPane);

        final HBox center = new HBox();
        mainPane.setCenter(center);
        center.setSpacing(5);

        final Slider effectsVolumeSlider = createEffectsVolumeSlider();
        final Node volumeButtons = createVolumeButton(effectsVolumeSlider);

        center.getChildren().add(volumeButtons);

        center.getChildren().add(effectsVolumeSlider);

        return pane;
    }

    private Slider createEffectsVolumeSlider() {

        final Configuration config = ActiveConfigurationContext.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(false);
        slider.setValue(config.getEffectsVolume());
        config.getEffectsVolumeProperty().bindBidirectional(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> config.saveConfigIgnoringExceptions());
        return slider;
    }

    private Slider createSpeedEffectSlider() {

        final Configuration config = ActiveConfigurationContext.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(8);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setSnapToTicks(false);
        slider.setValue(config.getSpeedEffectsProperty().getValue());
        log.info("the speed of the effects is = {}", slider.getValue());
        config.getSpeedEffectsProperty().bind(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> {
            log.info("the speed of the slider is = {}", slider.getValue());
            config.saveConfigIgnoringExceptions();
        });
        return slider;
    }

    private void setMusicTitle(final MarqueeText musicLabel) {
        if (musicLabel != null) {
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            String musicTitle = BackgroundMusicManager.getMusicTitle(backgroundMusicManager.getCurrentMusic());
            musicLabel.getTextProperty().setValue(musicTitle);
        }
    }

}

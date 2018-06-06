package net.gazeplay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.I18NTooltip;
import net.gazeplay.commons.utils.MarqueeText;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

@Data
@Slf4j
public abstract class GraphicalContext<T extends Parent> {

    @Getter
    private final GazePlay gazePlay;

    @Getter
    protected final T root;

    public static final String RESOURCES_PATH = "data" + File.separator + "common";
    public static final String IMAGES_PATH = RESOURCES_PATH + File.separator + "images";
    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String PREVIOUS_ICON = IMAGES_PATH + File.separator + "previous.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String NEXT_ICON = IMAGES_PATH + File.separator + "skip.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String PAUSE_ICON = IMAGES_PATH + File.separator + "pause.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String PLAY_ICON = IMAGES_PATH + File.separator + "play-button.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String SPEAKER_ICON = IMAGES_PATH + File.separator + "speaker.png";

    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String MUTE_ICON = IMAGES_PATH + File.separator + "mute.png";

    public static final int ICON_SIZE = 32;

    public static final double MUSIC_GRID_MAX_WIDTH = 200;

    /**
     * Field used to know if the background music controler has already been built once. This is used to get audio and
     * play it at the beginning.
     */
    public static boolean firstMusicSetUp = true;

    /**
     * Fields with listeners from music controler. When need those because when the volume controle is not on stage
     * (i.e. when configuration is shown), it doesn't receive any event from listener (no idea why). Then when it comes
     * back on stage, it needs to be updated.
     */
    private MarqueeText musicName;
    private Button pauseTrack;
    private Button playTrack;
    private double beforeMutedValue;

    /**
     * This list is used to store all references to listeners wrapped in WeakListener and WeakEventHandler. This is done
     * to avoid memory leak.
     */
    @Getter
    private final List<Object> weakReferences = new ArrayList<Object>();

    public void setUpOnStage(final Scene scene) {

        // Make sure we are the root of the scene
        scene.setRoot(root);

        updateMusicControler();

        log.info("Finished setup stage with the game scene");
    }

    public abstract ObservableList<Node> getChildren();

    public void clear() {

        getChildren().clear();

        log.info("Nodes not removed: {}", getChildren().size());
    }

    public void dispose() {
        clear();
        weakReferences.clear();
    }

    public I18NButton createToggleFullScreenButtonInGameScreen(@NonNull GazePlay gazePlay) {

        EventHandler<Event> eventHandler = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {
                gazePlay.toggleFullScreen();
            }
        };

        I18NButton button = new I18NButton(gazePlay.getTranslator(), null);
        configureFullScreenToggleButton(gazePlay.isFullScreen(), button);

        gazePlay.getFullScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean wasFullScreen,
                    Boolean isFullScreen) {
                configureFullScreenToggleButton(isFullScreen, button);
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

        return button;
    }

    private void configureFullScreenToggleButton(Boolean isFullScreen, I18NButton button) {
        final Image buttonGraphics;

        final String label;
        if (isFullScreen) {
            buttonGraphics = new Image("data/common/images/fullscreen-exit.png");
            label = "Exit FullScreen";
        } else {
            buttonGraphics = new Image("data/common/images/fullscreen-enter.png");
            label = "Enter FullScreen";
        }
        ImageView imageView = new ImageView(buttonGraphics);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Screen.getPrimary().getBounds().getWidth() / 40);
        button.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                imageView.setFitHeight(newValue.doubleValue() / 2d);
            }
        });
        button.setGraphic(imageView);
        button.setTooltip(new I18NTooltip(gazePlay.getTranslator(), label));
    }

    public TitledPane createMusicControlPane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Music");
        pane.setCollapsible(false);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(2);

        Slider volumeSlider = createMediaVolumeSlider(gazePlay);
        grid.add(volumeSlider, 1, 1, 2, 1);

        final Node volumeButtons = createVolumeButton(volumeSlider);
        grid.add(volumeButtons, 0, 1);

        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();

        final MediaPlayer currentMusic = backgroundMusicManager.getCurrentMusic();

        // final Label musicName = new Label(backgroundMusicManager.getMusicTitle(currentMusic));
        musicName = new MarqueeText(BackgroundMusicManager.getMusicTitle(currentMusic));
        grid.add(musicName, 0, 0, 3, 1);
        grid.setMaxWidth(MUSIC_GRID_MAX_WIDTH);

        final ChangeListener<Boolean> changeMusicTitleListener = (observable, oldValue, newValue) -> {

            // If we receive a change event and the new value is fales, then it means
            // that the music has been changed (see changingProperty from Slider)
            if (!newValue) {
                setMusicTitle(musicName);
            }
        };
        weakReferences.add(changeMusicTitleListener);
        backgroundMusicManager.getIsMusicChanging()
                .addListener(new WeakChangeListener<Boolean>(changeMusicTitleListener));

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

        final EventHandler<ActionEvent> previousHandler = (ActionEvent event) -> {
            backgroundMusicManager.previous();
        };
        weakReferences.add(previousHandler);

        previousTrack.setOnAction(new WeakEventHandler<>(previousHandler));

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

        final EventHandler<ActionEvent> pauseHandler = (ActionEvent event) -> {
            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            musicManager.pause();
        };
        weakReferences.add(pauseHandler);
        pauseTrack.setOnAction(new WeakEventHandler<>(pauseHandler));

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
        final EventHandler<ActionEvent> playHandler = (ActionEvent event) -> {
            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            musicManager.play();
        };
        weakReferences.add(playHandler);
        playTrack.setOnAction(new WeakEventHandler<>(playHandler));

        final boolean isPlaying = backgroundMusicManager.isPlaying();
        playTrack.setVisible(!isPlaying);
        pauseTrack.setVisible(isPlaying);

        final StackPane stackPane = new StackPane(pauseTrack, playTrack);

        buttonImg = null;
        try {
            buttonImg = new Image(NEXT_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + NEXT_ICON);
        }

        final ChangeListener<Boolean> setVisibleListener = ((observable, oldValue, newValue) -> {

            playTrack.setVisible(!newValue);
            pauseTrack.setVisible(newValue);
        });
        weakReferences.add(setVisibleListener);
        backgroundMusicManager.getIsPlayingPoperty().addListener(new WeakChangeListener<>(setVisibleListener));

        Button nextTrack;
        if (buttonImg == null) {
            nextTrack = new Button(">");
        } else {
            nextTrack = new Button("", new ImageView(buttonImg));
        }

        final EventHandler<ActionEvent> nextHandler = (ActionEvent event) -> {
            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            musicManager.next();
        };
        weakReferences.add(nextHandler);
        nextTrack.setOnAction(new WeakEventHandler<>(nextHandler));

        grid.add(previousTrack, 0, 2);
        grid.add(stackPane, 1, 2);
        grid.add(nextTrack, 2, 2);

        pane.setContent(grid);

        if (GraphicalContext.firstMusicSetUp) {

            if (backgroundMusicManager.getPlaylist().isEmpty()) {
                final Configuration configuration = Configuration.getInstance();
                backgroundMusicManager.getAudioFromFolder(configuration.getMusicFolder());
            }

            backgroundMusicManager.changeMusic(0);
            backgroundMusicManager.play();
            GraphicalContext.setFirstMusicSetip(false);

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

        unmuteButton.setOnAction((event) -> {
            volumeSlider.setValue(beforeMutedValue);
        });

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            final boolean localMuted = newValue.doubleValue() == 0;
            muteButton.setVisible(!localMuted);
            unmuteButton.setVisible(localMuted);
        });

        final StackPane volumeStackPane = new StackPane(muteButton, unmuteButton);
        return volumeStackPane;
    }

    /**
     * This method only exists because of mavent findbug plugin. Since this class is a singleton, this works.
     * 
     * @param value
     *            The new value to set.
     */
    private static void setFirstMusicSetip(boolean value) {
        GraphicalContext.firstMusicSetUp = value;
    }

    private void setMusicTitle(final MarqueeText musicLabel) {
        if (musicLabel != null) {
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            String musicTitle = BackgroundMusicManager.getMusicTitle(backgroundMusicManager.getCurrentMusic());
            musicLabel.getTextProperty().setValue(musicTitle);
        }
    }

    public Slider createMediaVolumeSlider(@NonNull GazePlay gazePlay) {

        final Configuration config = Configuration.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(false);
        slider.setValue(config.getMusicVolume());
        config.getMusicVolumeProperty().bindBidirectional(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> {
            final Configuration localConfig = Configuration.getInstance();
            localConfig.saveConfigIgnoringExceptions();
        });
        return slider;
    }

    private Slider createEffectsVolumeSlider(@NonNull GazePlay gazePlay) {

        final Configuration config = Configuration.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(false);
        slider.setValue(config.getEffectsVolume());
        config.getEffectsVolumeProperty().bindBidirectional(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> {
            final Configuration localConfig = Configuration.getInstance();
            localConfig.saveConfigIgnoringExceptions();
        });
        return slider;
    }

    public void onGameStarted() {
    }

    public TitledPane createEffectsVolumePane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "EffectsVolume");
        pane.setCollapsible(false);

        final BorderPane mainPane = new BorderPane();
        pane.setContent(mainPane);

        final HBox center = new HBox();
        mainPane.setCenter(center);
        center.setSpacing(5);

        final Slider effectsVolumeSlider = createEffectsVolumeSlider(gazePlay);
        final Node volumeButtons = createVolumeButton(effectsVolumeSlider);

        center.getChildren().add(volumeButtons);

        center.getChildren().add(effectsVolumeSlider);

        return pane;
    }

    public void updateMusicControler() {

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
}

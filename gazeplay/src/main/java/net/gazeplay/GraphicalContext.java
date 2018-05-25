package net.gazeplay;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.I18NTooltip;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.MarqueeText;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

@Data
@Slf4j
public abstract class GraphicalContext<T> {

    @Getter
    private final GazePlay gazePlay;

    protected final T root;

    @Getter
    protected final Scene scene;

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

    public static final int ICON_SIZE = 32;

    /**
     * Field used to know if the background music controler has already been built once. This is used to get audio and
     * play it at the beginning.
     */
    public static boolean firstMusicSetUp = true;

    public void setUpOnStage(Stage stage) {
        stage.setTitle("GazePlay");

        // setting the scene again will exit fullscreen
        // so we need to backup the fullscreen status, and restore it after the scene has been set
        boolean fullscreen = stage.isFullScreen();
        stage.setScene(scene);
        stage.setFullScreen(fullscreen);

        stage.setOnCloseRequest((WindowEvent we) -> stage.close());

        final Configuration config = Configuration.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);

        stage.show();
        log.info("Finished setup stage with the game scene");
    }

    public abstract ObservableList<Node> getChildren();

    public void clear() {
        getScene().setFill(Color.BLACK);
        getChildren().clear();

        log.info("Nodes not removed: {}", getChildren().size());
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

        button.button.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

        button.assignIndicator(eventHandler);

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
        button.button.setGraphic(imageView);
        button.button.setTooltip(new I18NTooltip(gazePlay.getTranslator(), label));
    }

    public TitledPane createMusicControlPane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Music");
        pane.setCollapsible(false);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(2);

        Image buttonImg = null;
        try {
            buttonImg = new Image(SPEAKER_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PREVIOUS_ICON);
        }

        Label volumeLabel;
        if (buttonImg == null) {
            volumeLabel = new I18NLabel(getGazePlay().getTranslator(), "Volume");
        } else {
            volumeLabel = new Label(null, new ImageView(buttonImg));
        }

        grid.add(volumeLabel, 0, 1);
        Slider volumeSlider = createMediaVolumeSlider(gazePlay);
        grid.add(volumeSlider, 1, 1, 2, 1);
        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();

        final MediaPlayer currentMusic = backgroundMusicManager.getCurrentMusic();

        // final Label musicName = new Label(backgroundMusicManager.getMusicTitle(currentMusic));
        final MarqueeText musicName = new MarqueeText(backgroundMusicManager.getMusicTitle(currentMusic));
        //musicName.setLabelFor(volumeSlider);
        grid.add(musicName, 0, 0, 3, 1);

        musicName.setMaxWidth(ICON_SIZE * 3 + 3 * grid.getHgap());
        backgroundMusicManager.getMusicIndexProperty().addListener((observable) -> {

            setMusicTitle(musicName);
        });

        buttonImg = null;
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
        previousTrack.setOnAction((event) -> {

            backgroundMusicManager.previous();
        });

        buttonImg = null;
        try {
            buttonImg = new Image(PAUSE_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PAUSE_ICON);
        }

        Button pauseTrack;
        if (buttonImg == null) {
            pauseTrack = new Button("||");
        } else {
            pauseTrack = new Button("", new ImageView(buttonImg));
        }
        pauseTrack.setOnAction((event) -> {
            backgroundMusicManager.pause();
        });

        buttonImg = null;
        try {
            buttonImg = new Image(PLAY_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PLAY_ICON);
        }

        Button playTrack;
        if (buttonImg == null) {
            playTrack = new Button("|>");
        } else {
            playTrack = new Button("", new ImageView(buttonImg));
        }
        playTrack.setOnAction((event) -> {
            backgroundMusicManager.play();
        });

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
        nextTrack.setOnAction((event) -> {

            backgroundMusicManager.next();
        });

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
            backgroundMusicManager.playPlayList();
            GraphicalContext.setFirstMusicSetip(false);

            // We need to manually set the music title for the first set up
            setMusicTitle(musicName);
        }

        return pane;
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
        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
        String musicTitle = backgroundMusicManager.getMusicTitle(backgroundMusicManager.getCurrentMusic());
        musicLabel.getTextProperty().setValue(musicTitle);
    }

    public Slider createMediaVolumeSlider(@NonNull GazePlay gazePlay) {

        final Configuration config = Configuration.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(true);
        slider.setValue(config.getMusicVolume());
        config.getMusicVolumeProperty().bindBidirectional(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> {
            config.saveConfigIgnoringExceptions();
        });
        return slider;
    }

    private Slider createEffectsVolumeSlider(@NonNull GazePlay gazePlay) {

        final Configuration config = Configuration.getInstance();
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(true);
        slider.setValue(config.getEffectsVolume());
        config.getEffectsVolumeProperty().bindBidirectional(slider.valueProperty());
        slider.valueProperty().addListener((observable) -> {
            config.saveConfigIgnoringExceptions();
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
        Image buttonImg = null;
        try {
            buttonImg = new Image(SPEAKER_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + PREVIOUS_ICON);
        }

        Label volumeLabel;
        if (buttonImg == null) {
            volumeLabel = new I18NLabel(getGazePlay().getTranslator(), "Volume");
        } else {
            volumeLabel = new Label(null, new ImageView(buttonImg));
        }
        center.getChildren().add(volumeLabel);

        final Slider effectsVolumeSlider = createEffectsVolumeSlider(gazePlay);
        center.getChildren().add(effectsVolumeSlider);

        return pane;
    }

}

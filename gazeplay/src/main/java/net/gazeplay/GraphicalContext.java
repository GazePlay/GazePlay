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
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.I18NTooltip;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

@Data
@Slf4j
public abstract class GraphicalContext<T> {

    @Getter
    private final GazePlay gazePlay;

    protected final T root;

    @Getter
    protected final Scene scene;

    private Button playTrack;

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

    public static final int ICON_SIZE = 64;

    public void setUpOnStage(Stage stage) {
        stage.setTitle("GazePlay");

        // setting the scene again will exit fullscreen
        // so we need to backup the fullscreen status, and restore it after the scene has been set
        boolean fullscreen = stage.isFullScreen();
        stage.setScene(scene);
        stage.setFullScreen(fullscreen);

        stage.setOnCloseRequest((WindowEvent we) -> stage.close());

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
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

        button.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

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

    public TitledPane createSoundControlPane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Music");
        pane.setCollapsible(false);

        GridPane grid = new GridPane();
        grid.add(new I18NLabel(getGazePlay().getTranslator(), "Volume"), 0, 0);
        grid.add(createMediaVolumeSlider(gazePlay), 0, 1);

        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();

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
        previousTrack.setOnAction((event) -> {
            int currentMusicIndex = backgroundMusicManager.getCurrentMusicIndex();
            currentMusicIndex = (currentMusicIndex + backgroundMusicManager.getPlaylist().size() - 1)
                    % backgroundMusicManager.getPlaylist().size();
            log.info("current index : {}", currentMusicIndex);
            backgroundMusicManager.changeMusic(currentMusicIndex);
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
            playTrack.setVisible(true);
            pauseTrack.setVisible(false);
        });

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
        playTrack.setOnAction((event) -> {
            backgroundMusicManager.play();
            playTrack.setVisible(false);
            pauseTrack.setVisible(true);
        });

        if (backgroundMusicManager.isPaused()) {
            pauseTrack.setVisible(false);
        } else {
            playTrack.setVisible(false);
        }

        final StackPane stackPane = new StackPane(pauseTrack, playTrack);

        buttonImg = null;
        try {
            buttonImg = new Image(NEXT_ICON, ICON_SIZE, ICON_SIZE, false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + NEXT_ICON);
        }

        Button nextTrack;
        if (buttonImg == null) {
            nextTrack = new Button(">");
        } else {
            nextTrack = new Button("", new ImageView(buttonImg));
        }
        nextTrack.setOnAction((event) -> {
            int currentMusicIndex = backgroundMusicManager.getCurrentMusicIndex();
            currentMusicIndex = (currentMusicIndex + 1) % backgroundMusicManager.getPlaylist().size();
            log.info("current index : {}", currentMusicIndex);
            backgroundMusicManager.changeMusic(currentMusicIndex);
        });

        HBox musicControl = new HBox(5, previousTrack, stackPane, nextTrack);

        grid.add(musicControl, 0, 2);

        pane.setContent(grid);

        return pane;
    }

    public Slider createMediaVolumeSlider(@NonNull GazePlay gazePlay) {
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(true);
        slider.setValue(BackgroundMusicManager.getInstance().volumeProperty().getValue());
        BackgroundMusicManager.getInstance().volumeProperty().bindBidirectional(slider.valueProperty());
        return slider;
    }

    public void onGameStarted() {
    }

}

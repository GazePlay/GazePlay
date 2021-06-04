package net.gazeplay.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.MarqueeText;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.components.ProgressButton;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.gazeplay.ui.QuickControl.*;

@Slf4j
@RequiredArgsConstructor
public class MusicControl {

    private static final String RESOURCES_PATH = "data/common";

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

    /**
     * Field used to know if the background music controller has already been built once. This is used to get audio and
     * play it at the beginning.
     */
    private static final AtomicBoolean autoplayExecuted = new AtomicBoolean(false);

    @Getter
    private final GazePlay gazePlay;

    /**
     * Fields with listeners from music controller. When need those because when the volume control is not on stage
     * (i.e. when configuration is shown), it doesn't receive any event from listener (no idea why). Then when it comes
     * back on stage, it needs to be updated.
     */
    private MarqueeText musicName;

    private Button playButton;

    private Button pauseButton;

    private double beforeMutedValue;

    private boolean IsInArrow = false;

    public void updateMusicController() {
        setMusicTitle(musicName);
        if (playButton != null && pauseButton != null) {
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            log.info("updating : isPlaying : {}", backgroundMusicManager.isPlaying());
            if (backgroundMusicManager.isPlaying()) {
                playButton.setVisible(false);
                pauseButton.setVisible(true);
            } else {
                playButton.setVisible(true);
                pauseButton.setVisible(false);
            }
        }
    }

    public TitledPane createMusicControlPane() {

        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();

        final MediaPlayer currentMusic = backgroundMusicManager.getCurrentMusic();

        // final Label musicName = new Label(backgroundMusicManager.getMusicTitle(currentMusic));
        musicName = new MarqueeText(BackgroundMusicManager.getMusicTitle(currentMusic));

        backgroundMusicManager.getIsMusicChanging().addListener((observable, oldValue, newValue) -> {

            // If we receive a change event and the new value is false, then it means
            // that the music has been changed (see changingProperty from Slider)
            if (!newValue) {
                setMusicTitle(musicName);
            }
        });

        final Button previousButton = createButton("<", PREVIOUS_ICON, "previous", ICON_SIZE / 2d);
        previousButton.setOnAction((event) -> backgroundMusicManager.previous());

        final StackPane Pprev = new StackPane();
        final ProgressButton Bprev = new ProgressButton();
        Bprev.assignIndicator((event) -> backgroundMusicManager.previous());
        Bprev.active();
        Bprev.getButton().setVisible(false);
        Pprev.getChildren().addAll(previousButton, Bprev);

        pauseButton = createButton("||", PAUSE_ICON, "pause");
        pauseButton.setOnAction((event) -> backgroundMusicManager.pause());


        playButton = createButton("|>", PLAY_ICON, "play");
        playButton.setOnAction((event) -> backgroundMusicManager.play());

        if (backgroundMusicManager.isPlaying()) {
            playButton.setVisible(false);
            pauseButton.setVisible(true);
        } else {
            playButton.setVisible(true);
            pauseButton.setVisible(false);
        }

        final Button nextButton = createButton(">", NEXT_ICON, "next", ICON_SIZE / 2d);
        nextButton.setOnAction((event) -> backgroundMusicManager.next());

        final StackPane Pnext = new StackPane();
        final ProgressButton Bnext = new ProgressButton();
        Bnext.assignIndicator((event) -> backgroundMusicManager.next());
        Bnext.active();
        Bnext.getButton().setVisible(false);
        Pnext.getChildren().addAll(nextButton, Bnext);

        backgroundMusicManager.getIsPlayingProperty().addListener((observable) -> {
            if (backgroundMusicManager.isPlaying()) {
                playButton.setVisible(false);
                pauseButton.setVisible(true);
            } else {
                playButton.setVisible(true);
                pauseButton.setVisible(false);
            }
        });

        ProgressButton pauseplay = new ProgressButton();
        pauseplay.assignIndicator(event -> {
            if (backgroundMusicManager.isPlaying()) {
                playButton.setVisible(false);
                pauseButton.setVisible(true);
                backgroundMusicManager.pause();
            } else {
                playButton.setVisible(true);
                pauseButton.setVisible(false);
                backgroundMusicManager.play();
            }
        });
        pauseplay.active();
        pauseplay.getButton().setRadius(30);
        pauseplay.getButton().setVisible(false);

        final StackPane stackPane = new StackPane(pauseButton, playButton, pauseplay);


        final HBox line1 = new HBox();
        line1.setSpacing(CONTENT_SPACING);
        line1.setAlignment(Pos.CENTER);
        line1.getChildren().add(musicName);

        final HBox line2 = new HBox();
        line2.setSpacing(CONTENT_SPACING);
        line2.setAlignment(Pos.CENTER);
        line2.getChildren().add(Pprev);
        line2.getChildren().add(stackPane);
        line2.getChildren().add(Pnext);

        final VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().addAll(line1, line2);
        content.setPrefHeight(PREF_HEIGHT);

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

        final I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Music");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    private Button createButton(@NonNull final String altText, @NonNull final String imagePath, @NonNull final String toolTip) {
        return createButton(altText, imagePath, toolTip, ICON_SIZE);
    }

    private Button createButton(@NonNull final String altText, @NonNull final String imagePath, @NonNull final String toolTip, final double imageSize) {
        Image image;
        try {
            image = new Image(imagePath, imageSize, imageSize, true, true);
        } catch (final IllegalArgumentException e) {
            log.warn("Failed to load image with path {}", imagePath);
            image = null;
        }
        final Button button;
        if (image == null) {
            button = new Button(altText);
        } else {
            button = new Button("", new ImageView(image));
        }

        //button.setStyle(
        //    "-fx-background-radius: 5em; " +
        //        "-fx-min-width: 3px; " +
        //        "-fx-min-height: 3px; " +
        //        "-fx-max-width: 3px; " +
        //        "-fx-max-height: 3px;"
        //);

        final double r = imageSize * 1.0d / 2d;
        button.setShape(new Circle(r));
        button.setMinSize(2 * r, 2 * r);
        button.setMaxSize(2 * r, 2 * r);

        button.setPrefWidth(imageSize * 1.1d);
        button.setPrefHeight(imageSize * 1.1d);
        button.setTooltip(new Tooltip(toolTip));
        button.setAccessibleText(toolTip);
        return button;
    }

    private void setUpSwitchButton(final Button button) {
        button.setPrefWidth(ICON_SIZE * 0.5d);
        button.setPrefHeight(ICON_SIZE * 0.5d);

        button.setMinWidth(ICON_SIZE * 0.5d);
        button.setMinHeight(ICON_SIZE * 0.5d);

        button.setMaxWidth(ICON_SIZE * 0.5d);
        button.setMaxHeight(ICON_SIZE * 0.5d);
    }

    Node createMuteSwitchButton(final Slider volumeSlider) {
        final Button muteButton = createButton("mute", SPEAKER_ICON, "mute", ICON_SIZE / 2d);
        final Button unmuteButton = createButton("unmute", MUTE_ICON, "unmute", ICON_SIZE / 2d);

        setUpSwitchButton(muteButton);
        setUpSwitchButton(unmuteButton);

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

        ProgressButton muteunmute = new ProgressButton();
        muteunmute.assignIndicator(event -> {
            boolean mute = volumeSlider.getValue() == 0;
            if (mute){
                volumeSlider.setValue(beforeMutedValue);
            } else {
                beforeMutedValue = volumeSlider.getValue();
                volumeSlider.setValue(0);
            }
        });
        muteunmute.active();
        muteunmute.getButton().setVisible(false);

        return new StackPane(muteButton, unmuteButton, muteunmute);
    }

    private Slider createMediaVolumeSlider(final Configuration config) {
        final Slider slider = QuickControl.getInstance().createVolumeSlider();
        slider.setValue(config.getMusicVolumeProperty().getValue());
        slider.valueProperty().bindBidirectional(config.getMusicVolumeProperty());
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            BackgroundMusicManager.getInstance().setVolume(newValue.doubleValue());
        });
        return slider;
    }

    private Slider createEffectsVolumeSlider(final Configuration config) {
        final Slider slider = QuickControl.getInstance().createVolumeSlider();
        slider.setValue(config.getEffectsVolumeProperty().getValue());
        slider.valueProperty().bindBidirectional(config.getEffectsVolumeProperty());
        return slider;
    }

    public TitledPane createVolumeLevelControlPane(final Configuration config, final Translator translator) {
        final Slider mediaVolumeSlider = createMediaVolumeSlider(config);
        final Node mediaMuteSwitchButton = createMuteSwitchButton(mediaVolumeSlider);

        final Slider effectsVolumeSlider = createEffectsVolumeSlider(config);
        final Node effectsMuteSwitchButton = createMuteSwitchButton(effectsVolumeSlider);

        ImagePattern LEFT = new ImagePattern(new Image("data/labyrinth/images/leftArrow.png"));
        ImagePattern RIGHT = new ImagePattern(new Image("data/labyrinth/images/rightArrow.png"));

        Rectangle LEFT1 = new Rectangle(40, 40);
        Rectangle RIGHT1 = new Rectangle(40, 40);

        LEFT1.setFill(LEFT);
        RIGHT1.setFill(RIGHT);

        //Mouse event
        LEFT1.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            LEFTarrow(mediaVolumeSlider);
        });
        LEFT1.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        RIGHT1.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            RIGHTarrow(mediaVolumeSlider);
        });
        RIGHT1.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        //Gaze event
        LEFT1.addEventHandler(GazeEvent.GAZE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            LEFTarrow(mediaVolumeSlider);
        });
        LEFT1.addEventHandler(GazeEvent.GAZE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        RIGHT1.addEventHandler(GazeEvent.GAZE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            RIGHTarrow(mediaVolumeSlider);
        });
        RIGHT1.addEventHandler(GazeEvent.GAZE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });


        final HBox line1 = new HBox();
        {
            line1.setSpacing(CONTENT_SPACING);
            line1.setAlignment(Pos.CENTER);
            final I18NLabel label = new I18NLabel(translator, "Music");
            label.setMinWidth(ICON_SIZE * 1d);
            line1.getChildren().add(label);
            line1.getChildren().add(mediaMuteSwitchButton);
            line1.getChildren().add(mediaVolumeSlider);
            line1.getChildren().addAll(LEFT1, RIGHT1);
        }

        Rectangle LEFT2 = new Rectangle(40, 40);
        Rectangle RIGHT2 = new Rectangle(40, 40);

        LEFT2.setFill(LEFT);
        RIGHT2.setFill(RIGHT);

        //Mouse event
        LEFT2.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            LEFTarrow(effectsVolumeSlider);
        });
        LEFT2.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        RIGHT2.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            RIGHTarrow(effectsVolumeSlider);
        });
        RIGHT2.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        //Gaze event
        LEFT2.addEventHandler(GazeEvent.GAZE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            LEFTarrow(effectsVolumeSlider);
        });
        LEFT2.addEventHandler(GazeEvent.GAZE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        RIGHT2.addEventHandler(GazeEvent.GAZE_ENTERED_TARGET, mouseEvent -> {
            IsInArrow = true;
            RIGHTarrow(effectsVolumeSlider);
        });
        RIGHT2.addEventHandler(GazeEvent.GAZE_EXITED_TARGET, mouseEvent -> {
            IsInArrow = false;
        });

        final HBox line2 = new HBox();
        {
            line2.setSpacing(CONTENT_SPACING);
            line2.setAlignment(Pos.CENTER);
            final I18NLabel label = new I18NLabel(translator, "Effects");
            label.setMinWidth(ICON_SIZE * 1d);
            line2.getChildren().add(label);
            line2.getChildren().add(effectsMuteSwitchButton);
            line2.getChildren().add(effectsVolumeSlider);
            line2.getChildren().addAll(LEFT2, RIGHT2);
        }

        final VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().addAll(line1, line2);
        content.setPrefHeight(PREF_HEIGHT);

        final I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Sound Volume");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    private void setMusicTitle(final MarqueeText musicLabel) {
        if (musicLabel != null) {
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            final String musicTitle = BackgroundMusicManager.getMusicTitle(backgroundMusicManager.getCurrentMusic());
            musicLabel.getTextProperty().setValue(musicTitle);
        }
    }

    void LEFTarrow(Slider SP){
        PauseTransition left = new PauseTransition(Duration.millis(2));
        left.setOnFinished(leftevent ->{
            if (IsInArrow){
                SP.setValue(SP.getValue()-0.005);
                left.play();
            }
        });
        left.play();
    }

    void RIGHTarrow(Slider SP){
        PauseTransition right = new PauseTransition(Duration.millis(2));
        right.setOnFinished(rightevent ->{
            if (IsInArrow){
                SP.setValue(SP.getValue()+0.005);
                right.play();
            }
        });
        right.play();
    }

}

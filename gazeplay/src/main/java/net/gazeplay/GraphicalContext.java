package net.gazeplay;

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
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

@Data
@Slf4j
public abstract class GraphicalContext<T> {

    @Getter
    private final GazePlay gazePlay;

    protected final T root;

    @Getter
    protected final Scene scene;

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

    public Button createToggleFullScreenButtonInGameScreen(@NonNull GazePlay gazePlay) {

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

    public TitledPane createSoundControlPane() {
        I18NTitledPane pane = new I18NTitledPane(getGazePlay().getTranslator(), "Music");
        pane.setCollapsible(false);

        GridPane grid = new GridPane();
        grid.add(new I18NLabel(getGazePlay().getTranslator(), "Volume"), 0, 0);
        grid.add(createMediaVolumeSlider(gazePlay), 0, 1);

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

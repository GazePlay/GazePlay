package net.gazeplay.ui;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.Translator;

import static net.gazeplay.ui.QuickControl.*;

@NoArgsConstructor
@Slf4j
public class FixationLengthControl {

    @Getter
    private static final FixationLengthControl instance = new FixationLengthControl();

    private static final int FIXATION_LENGTH_SLIDER_MIN_VALUE = 0;

    private static final int FIXATION_LENGTH_SLIDER_MAX_VALUE = 10000;

    public TitledPane createfixationLengthPane(Configuration config, Translator translator, Scene primaryScene) {
        Label fixationLengthValueLabel = new Label("");
        fixationLengthValueLabel.setMinWidth(ICON_SIZE);
        Slider fixationLengthRatioSlider = createFixationLengthSlider(config, fixationLengthValueLabel);
        registerKeyHandler(primaryScene, fixationLengthRatioSlider);

        HBox line1 = new HBox();
        line1.setSpacing(CONTENT_SPACING);
        line1.setAlignment(Pos.CENTER);
        line1.getChildren().addAll(fixationLengthValueLabel, fixationLengthRatioSlider);

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().add(line1);
        content.setPrefHeight(PREF_HEIGHT);

        I18NTitledPane pane = new I18NTitledPane(translator, "FixationLength");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    public Slider createFixationLengthSlider(Configuration config, Label fixationLengthValueLabel) {
        final int initialFixationLengthRatioValue = config.getFixationLength();

        Slider slider = new Slider();
        slider.setMinWidth(QuickControl.SLIDERS_MIN_WIDTH);
        slider.setMaxWidth(QuickControl.SLIDERS_PREF_WIDTH);
        slider.setPrefWidth(QuickControl.SLIDERS_MAX_WIDTH);
        slider.setMin(FIXATION_LENGTH_SLIDER_MIN_VALUE);
        slider.setMax(FIXATION_LENGTH_SLIDER_MAX_VALUE);
        slider.setMajorTickUnit(2500);
        slider.setMinorTickCount(100);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(100);
        slider.setValue(initialFixationLengthRatioValue);

        fixationLengthValueLabel.setText(formatValue(initialFixationLengthRatioValue));

        // user can reset ratio to default just by clicking on the label
        fixationLengthValueLabel.setOnMouseClicked(event -> slider.setValue(500));

        slider.valueProperty().addListener((observable) -> {
            String labelText = formatValue(slider.getValue());
            fixationLengthValueLabel.setText(labelText);
            config.setFixationLength((int) slider.getValue());
        });

        return slider;
    }

    public String formatValue(double fixationLengthValue) {
        return "x" + String.format("%.2f", fixationLengthValue / 1000.0);
    }

    public void registerKeyHandler(@NonNull Scene primaryScene, final Slider fixationLengthRatioSlider) {

        EventHandler increaseFixationLengthEventHandler = (EventHandler<KeyEvent>) event -> {
            final int sliderFixationValue = (int) fixationLengthRatioSlider.getValue();
            if (sliderFixationValue < fixationLengthRatioSlider.getMax()) {
                fixationLengthRatioSlider.setValue(sliderFixationValue + 100.0);
            } else {
                log.info("max fixation length reached !");
            }
        };
        EventHandler decreaseFixationLengthEventHandler = (EventHandler<KeyEvent>) event -> {
            final int sliderFixationValue = (int) fixationLengthRatioSlider.getValue();
            if (sliderFixationValue > fixationLengthRatioSlider.getMin()) {
                fixationLengthRatioSlider.setValue(sliderFixationValue - 100.0);
            } else {
                log.info("min fixation length reached !");
            }
        };

        final KeyCombination keyCombinationUP = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_ANY);
        final KeyCombination keyCombinationDOWN = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_ANY);

        primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            if (keyCombinationUP.match(keyEvent)) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                increaseFixationLengthEventHandler.handle(keyEvent);
            } else if (keyCombinationDOWN.match(keyEvent)) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                decreaseFixationLengthEventHandler.handle(keyEvent);
            }
        });

    }

}

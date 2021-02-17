package net.gazeplay.ui;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.gazeplay.ui.QuickControl.*;

@NoArgsConstructor
@Slf4j
public class FixationLengthControl {

    @Getter
    private static final FixationLengthControl instance = new FixationLengthControl();

    private static final double FIXATION_LENGTH_RANGE_WIDTH = 9d;

    private static final double FIXATION_LENGTH_SLIDER_MIN_VALUE = -1d * FIXATION_LENGTH_RANGE_WIDTH * 0.9d;

    private static final double FIXATION_LENGTH_SLIDER_MAX_VALUE = FIXATION_LENGTH_RANGE_WIDTH;

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
        final double initialFixationLengthRatioValue = config.getFixationlengthProperty().getValue();

        Slider slider = new Slider();
        slider.setMinWidth(QuickControl.SLIDERS_MIN_WIDTH);
        slider.setMaxWidth(QuickControl.SLIDERS_PREF_WIDTH);
        slider.setPrefWidth(QuickControl.SLIDERS_MAX_WIDTH);
        slider.setMin(FIXATION_LENGTH_SLIDER_MIN_VALUE);
        slider.setMax(FIXATION_LENGTH_SLIDER_MAX_VALUE);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(1);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1d);
        slider.setValue(fixationLengthToSliderValue(initialFixationLengthRatioValue));

        fixationLengthValueLabel.setText(formatValue(initialFixationLengthRatioValue));

        // user can reset ratio to default just by clicking on the label
        fixationLengthValueLabel.setOnMouseClicked(event -> slider.setValue(0d));

        slider.valueProperty().addListener((observable) -> {
            int fixationLengthValue = (int) sliderValueToFixationLength((int) slider.getValue());
            String labelText = formatValue(fixationLengthValue);
            fixationLengthValueLabel.setText(labelText);
            config.getFixationlengthProperty().set(fixationLengthValue);
        });

        return slider;
    }

    public String formatValue(double fixationLengthValue) {
        return "x" + String.format("%.2f", fixationLengthValue);
    }

    public double sliderValueToFixationLength(int value) {
        if (value >= 0d) {
            return 1d + value;
        } else {
            BigDecimal result = new BigDecimal(value).setScale(2, RoundingMode.DOWN).divide(new BigDecimal(FIXATION_LENGTH_RANGE_WIDTH).setScale(2, RoundingMode.DOWN), RoundingMode.DOWN).setScale(2, RoundingMode.DOWN);
            return result.add(new BigDecimal(1d)).doubleValue();
        }
    }

    public double fixationLengthToSliderValue(double value) {
        if (value >= 1d) {
            return value - 1d;
        } else {
            BigDecimal result = new BigDecimal(value).add(new BigDecimal(-1d));
            result = result.setScale(2, RoundingMode.DOWN).multiply(new BigDecimal(FIXATION_LENGTH_RANGE_WIDTH).setScale(2, RoundingMode.DOWN)).setScale(2, RoundingMode.DOWN);
            return result.doubleValue();
        }
    }


    public void registerKeyHandler(@NonNull Scene primaryScene, final Slider fixationLengthRatioSlider) {

        EventHandler increaseSpeedEventHandler = (EventHandler<KeyEvent>) event -> {
            final double sliderFixationValue = Math.floor(fixationLengthRatioSlider.getValue());
            if (sliderFixationValue < fixationLengthRatioSlider.getMax()) {
                fixationLengthRatioSlider.setValue(sliderFixationValue + 1);
            } else {
                log.info("max fixation length reached !");
            }
        };
        EventHandler decreaseSpeedEventHandler = (EventHandler<KeyEvent>) event -> {
            final double sliderFixationValue = Math.floor(fixationLengthRatioSlider.getValue());
            if (sliderFixationValue > fixationLengthRatioSlider.getMin()) {
                fixationLengthRatioSlider.setValue(sliderFixationValue - 1);
            } else {
                log.info("min fixation length reached !");
            }
        };

        primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            if (keyEvent.getCode().toString().equals("F")) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                increaseSpeedEventHandler.handle(keyEvent);
            } else if (keyEvent.getCode().toString().equals("S")) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                decreaseSpeedEventHandler.handle(keyEvent);
            }
        });

    }

}

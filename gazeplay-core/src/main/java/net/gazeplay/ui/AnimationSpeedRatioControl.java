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

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.gazeplay.ui.QuickControl.*;

@NoArgsConstructor
@Slf4j
public class AnimationSpeedRatioControl {

    @Getter
    private static final AnimationSpeedRatioControl instance = new AnimationSpeedRatioControl();

    private static final double SPEED_RATIO_RANGE_WIDTH = 9d;

    private static final double SPEED_RATIO_SLIDER_MIN_VALUE = -1d * SPEED_RATIO_RANGE_WIDTH * 0.9d;

    private static final double SPEED_RATIO_SLIDER_MAX_VALUE = SPEED_RATIO_RANGE_WIDTH;

    public TitledPane createSpeedEffectsPane(Configuration config, Translator translator, Scene primaryScene) {
        Label speedEffectValueLabel = new Label("");
        speedEffectValueLabel.setMinWidth(ICON_SIZE);
        Slider speedRatioSlider = createSpeedEffectSlider(config, speedEffectValueLabel);
        registerKeyHandler(primaryScene, speedRatioSlider);

        HBox line1 = new HBox();
        line1.setSpacing(CONTENT_SPACING);
        line1.setAlignment(Pos.CENTER);
        line1.getChildren().addAll(speedEffectValueLabel, speedRatioSlider);

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().add(line1);
        content.setPrefHeight(PREF_HEIGHT);

        I18NTitledPane pane = new I18NTitledPane(translator, "SpeedEffects");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    public Slider createSpeedEffectSlider(Configuration config, Label speedEffectValueLabel) {
        final double initialSpeedRatioValue = config.getAnimationSpeedRatio();

        Slider slider = new Slider();
        slider.setMinWidth(QuickControl.SLIDERS_MIN_WIDTH);
        slider.setMaxWidth(QuickControl.SLIDERS_PREF_WIDTH);
        slider.setPrefWidth(QuickControl.SLIDERS_MAX_WIDTH);
        slider.setMin(SPEED_RATIO_SLIDER_MIN_VALUE);
        slider.setMax(SPEED_RATIO_SLIDER_MAX_VALUE);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(1);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1d);
        slider.setValue(speedRatioToSliderValue(initialSpeedRatioValue));

        speedEffectValueLabel.setText(formatValue(initialSpeedRatioValue));

        // user can reset ratio to default just by clicking on the label
        speedEffectValueLabel.setOnMouseClicked(event -> slider.setValue(0d));

        slider.valueProperty().addListener((observable) -> {
            double speedRatioValue = sliderValueToSpeedRatio(slider.getValue());
            String labelText = formatValue(speedRatioValue);
            speedEffectValueLabel.setText(labelText);
            config.setAnimationSpeedRatio(speedRatioValue);
        });

        return slider;
    }

    public String formatValue(double speedRatioValue) {
        return "x" + String.format("%.2f", speedRatioValue);
    }

    public double sliderValueToSpeedRatio(double value) {
        if (value >= 0d) {
            return 1d + value;
        } else {
            BigDecimal result = new BigDecimal(value).setScale(2, RoundingMode.DOWN).divide(new BigDecimal(SPEED_RATIO_RANGE_WIDTH).setScale(2, RoundingMode.DOWN), RoundingMode.DOWN).setScale(2, RoundingMode.DOWN);
            return result.add(new BigDecimal(1d)).doubleValue();
        }
    }

    public double speedRatioToSliderValue(double value) {
        if (value >= 1d) {
            return value - 1d;
        } else {
            BigDecimal result = new BigDecimal(value).add(new BigDecimal(-1d));
            result = result.setScale(2, RoundingMode.DOWN).multiply(new BigDecimal(SPEED_RATIO_RANGE_WIDTH).setScale(2, RoundingMode.DOWN)).setScale(2, RoundingMode.DOWN);
            return result.doubleValue();
        }
    }


    public void registerKeyHandler(@NonNull Scene primaryScene, final Slider animationSpeedRatioSlider) {

        EventHandler increaseSpeedEventHandler = (EventHandler<KeyEvent>) event -> {
            final double sliderSpeedValue = Math.floor(animationSpeedRatioSlider.getValue());
            if (sliderSpeedValue < animationSpeedRatioSlider.getMax()) {
                animationSpeedRatioSlider.setValue(sliderSpeedValue + 1);
            } else {
                log.info("max speed for effects reached !");
            }
        };
        EventHandler decreaseSpeedEventHandler = (EventHandler<KeyEvent>) event -> {
            final double sliderSpeedValue = Math.floor(animationSpeedRatioSlider.getValue());
            if (sliderSpeedValue > animationSpeedRatioSlider.getMin()) {
                animationSpeedRatioSlider.setValue(sliderSpeedValue - 1);
            } else {
                log.info("min speed for effects reached !");
            }
        };

        final KeyCombination keyCombinationUP = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_ANY);
        final KeyCombination keyCombinationDOWN = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_ANY);

        primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            if (keyCombinationUP.match(keyEvent)) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                increaseSpeedEventHandler.handle(keyEvent);
            } else if (keyCombinationDOWN.match(keyEvent)) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                decreaseSpeedEventHandler.handle(keyEvent);
            }
        });

    }

}

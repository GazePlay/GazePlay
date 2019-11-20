package net.gazeplay.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.Translator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.gazeplay.ui.QuickControl.*;

@NoArgsConstructor
public class AnimationSpeedRatioControl {

    @Getter
    private static final AnimationSpeedRatioControl instance = new AnimationSpeedRatioControl();

    private static final double SPEED_RATIO_RANGE_WIDTH = 9d;

    private static final double SPEED_RATIO_SLIDER_MIN_VALUE = -1d * SPEED_RATIO_RANGE_WIDTH * 0.9d;

    private static final double SPEED_RATIO_SLIDER_MAX_VALUE = SPEED_RATIO_RANGE_WIDTH;

    public TitledPane createSpeedEffectsPane(Configuration config, Translator translator) {
        Label speedEffectValueLabel = new Label("");
        speedEffectValueLabel.setMinWidth(ICON_SIZE);
        Slider speedEffectSlider = createSpeedEffectSlider(config, speedEffectValueLabel);

        HBox line1 = new HBox();
        line1.setSpacing(CONTENT_SPACING);
        line1.setAlignment(Pos.CENTER);
        line1.getChildren().addAll(speedEffectValueLabel, speedEffectSlider);

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
        final double initialSpeedRatioValue = config.getSpeedEffectsProperty().getValue();

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
            config.getSpeedEffectsProperty().set(speedRatioValue);
            config.saveConfigIgnoringExceptions();
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

}

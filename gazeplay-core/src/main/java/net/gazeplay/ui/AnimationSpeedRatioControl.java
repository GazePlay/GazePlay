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

/**
 * Control of the animation speed, in the side panel of the game screen: a slider from x0.10 (slower) to x10 (faster),
 * also moved with the keyboard.
 */
@NoArgsConstructor
@Slf4j
public class AnimationSpeedRatioControl {

    @Getter
    private static final AnimationSpeedRatioControl instance = new AnimationSpeedRatioControl();

    private static final double SPEED_RATIO_RANGE_WIDTH = 9d;

    private static final double SPEED_RATIO_SLIDER_MIN_VALUE = -1d * SPEED_RATIO_RANGE_WIDTH * 0.9d;

    private static final double SPEED_RATIO_SLIDER_MAX_VALUE = SPEED_RATIO_RANGE_WIDTH;

    /**
     * @return the pane of the control, bound to the ANIMATION_SPEED_RATIO setting
     */
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

    /**
     * @return the slider: its value is converted to a speed ratio (see {@link #sliderValueToSpeedRatio(double)}) saved
     *   in the settings and shown in the label
     */
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
        // The steps are applied below (snap), not by the slider: it counts its ticks from its minimum, -8.1, which
        // missed × 10 and × 1 (18/11/2026)
        slider.setSnapToTicks(false);
        slider.setBlockIncrement(1d);
        slider.setValue(speedRatioToSliderValue(initialSpeedRatioValue));
        slider.valueProperty().addListener((observable, before, now) -> {
            final double stepped = snap(slider, now.doubleValue());
            if (stepped != now.doubleValue()) {
                slider.setValue(stepped);
            }
        });

        speedEffectValueLabel.setText(formatValue(initialSpeedRatioValue));

        // user can reset ratio to default just by clicking on the label
        speedEffectValueLabel.setOnMouseClicked(event -> slider.setValue(0d));

        slider.valueProperty().addListener((observable) -> {
            double speedRatioValue = sliderValueToSpeedRatio(slider.getValue());
            String labelText = formatValue(speedRatioValue);
            speedEffectValueLabel.setText(labelText);
            config.getAnimationSpeedRatioProperty().set(speedRatioValue);
        });

        return slider;
    }

    /**
     * @return the speed ratio as shown, e.g. "x1.50"
     */
    public String formatValue(double speedRatioValue) {
        return "x" + String.format("%.2f", speedRatioValue);
    }

    /**
     * @return the speed ratio of a value of the slider: 1 + value above 0, a fraction of 1 below (slower)
     */
    public double sliderValueToSpeedRatio(double value) {
        if (value >= 0d) {
            return 1d + value;
        }
        // Rounded to the hundredth (18/11/2026): truncated, the minimum -8.1, stored as -8.0999..., gave × 0.11
        return BigDecimal.valueOf(1d + value / SPEED_RATIO_RANGE_WIDTH).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * @return the value of the slider of a speed ratio (inverse of {@link #sliderValueToSpeedRatio(double)})
     */
    public double speedRatioToSliderValue(double value) {
        if (value >= 1d) {
            return value - 1d;
        }
        return BigDecimal.valueOf((value - 1d) * SPEED_RATIO_RANGE_WIDTH).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    /**
     * The step of the slider below the speed by default: 0.9, a tenth of the speed (× 0.9, × 0.8 ... × 0.1).
     */
    private static final double SLOWER_STEP = SPEED_RATIO_RANGE_WIDTH / 10;

    /**
     * The step of the slider above the speed by default: half the speed (× 1.5, × 2 ... × 10).
     */
    private static final double FASTER_STEP = 0.5;

    /**
     * @return a value of the slider put on its nearest step: a tenth of the speed below × 1, half the speed above,
     * within its ends. The steps are computed here, not by the slider: it counts its ticks from its minimum (-8.1),
     * which missed × 10 and × 1 (found by the maintainer on 18/11/2026: × 10 gave × 9.90)
     */
    static double snap(final Slider slider, final double value) {
        final double stepped = value >= 0
            ? Math.round(value / FASTER_STEP) * FASTER_STEP
            : Math.round(value / SLOWER_STEP) * SLOWER_STEP;
        final double rounded = Math.round(stepped * 100) / 100.0;
        return Math.max(slider.getMin(), Math.min(slider.getMax(), rounded));
    }

    /**
     * @return the value of the slider one step up ({@code direction} 1: one more speed above × 1, a tenth below) or
     * down (-1), for the keyboard
     */
    static double nextValue(final Slider slider, final int direction) {
        final double value = snap(slider, slider.getValue());
        final double step = direction > 0
            ? (value < 0 ? SLOWER_STEP : 1)
            : (value > 0 ? 1 : SLOWER_STEP);
        final double next = value + direction * step;
        // From above × 1 to below it, or the reverse: stop on × 1 first
        if (value > 0 && next < 0 || value < 0 && next > 0) {
            return 0;
        }
        return snap(slider, next);
    }

    /**
     * Makes Ctrl + Up and Ctrl + Down increase and decrease the speed during the game.
     */
    public void registerKeyHandler(@NonNull Scene primaryScene, final Slider animationSpeedRatioSlider) {

        // One step up or down (nextValue): the former floor + 1 stayed stuck on the minimum, -8.1 (18/11/2026)
        EventHandler<KeyEvent> increaseSpeedEventHandler = event ->
            animationSpeedRatioSlider.setValue(nextValue(animationSpeedRatioSlider, 1));
        EventHandler<KeyEvent> decreaseSpeedEventHandler = event ->
            animationSpeedRatioSlider.setValue(nextValue(animationSpeedRatioSlider, -1));

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

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
public class ElementSizeControl {

    @Getter
    private static final ElementSizeControl instance = new ElementSizeControl();

    private static final int ELEMENT_SIZE_SLIDER_MIN_VALUE = 5;

    private static final int ELEMENT_SIZE_SLIDER_MAX_VALUE = 200;

    public TitledPane createElementSizePane(Configuration config, Translator translator, Scene primaryScene) {
        Label elementSizeValueLabel = new Label("");
        elementSizeValueLabel.setMinWidth(ICON_SIZE);
        Slider elementSizeRatioSlider = createElementSizeSlider(config, elementSizeValueLabel);
        registerKeyHandler(primaryScene, elementSizeRatioSlider);

        HBox line1 = new HBox();
        line1.setSpacing(CONTENT_SPACING);
        line1.setAlignment(Pos.CENTER);
        line1.getChildren().addAll(elementSizeValueLabel, elementSizeRatioSlider);

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().add(line1);
        content.setPrefHeight(PREF_HEIGHT);

        I18NTitledPane pane = new I18NTitledPane(translator, "Choose size");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    public Slider createElementSizeSlider(Configuration config, Label elementSizeValueLabel) {
        final int initialElementSizeValue = config.getElementSize();

        Slider slider = new Slider();
        slider.setMinWidth(QuickControl.SLIDERS_MIN_WIDTH);
        slider.setMaxWidth(QuickControl.SLIDERS_PREF_WIDTH);
        slider.setPrefWidth(QuickControl.SLIDERS_MAX_WIDTH);
        slider.setMin(ELEMENT_SIZE_SLIDER_MIN_VALUE);
        slider.setMax(ELEMENT_SIZE_SLIDER_MAX_VALUE);
        slider.setMajorTickUnit(5);
        slider.setMinorTickCount(4);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1);
        slider.setValue(initialElementSizeValue);

        elementSizeValueLabel.setText(formatValue(initialElementSizeValue));

        // user can reset ratio to default just by clicking on the label
        elementSizeValueLabel.setOnMouseClicked(event -> slider.setValue(50));

        slider.valueProperty().addListener((observable) -> {
            String labelText = formatValue(slider.getValue());
            elementSizeValueLabel.setText(labelText);
            config.getElementSizeProperty().set((int) slider.getValue());
        });

        return slider;
    }

    public String formatValue(double elementSizeValue) {
        return "x" + String.format("%.2f", elementSizeValue);
    }

    public void registerKeyHandler(@NonNull Scene primaryScene, final Slider elementSizeRatioSlider) {

        EventHandler increaseElementSizeEventHandler = (EventHandler<KeyEvent>) event -> {
            final int sliderElementSizeValue = (int) elementSizeRatioSlider.getValue();
            if (sliderElementSizeValue < elementSizeRatioSlider.getMax()) {
                elementSizeRatioSlider.setValue(sliderElementSizeValue + 1);
            } else {
                log.info("max fixation length reached !");
            }
        };
        EventHandler decreaseElementSizeEventHandler = (EventHandler<KeyEvent>) event -> {
            final int sliderElementSizeValue = (int) elementSizeRatioSlider.getValue();
            if (sliderElementSizeValue > elementSizeRatioSlider.getMin()) {
                elementSizeRatioSlider.setValue(sliderElementSizeValue - 1);
            } else {
                log.info("min fixation length reached !");
            }
        };

        final KeyCombination keyCombinationUP = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_ANY);
        final KeyCombination keyCombinationDOWN = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_ANY);

        primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            if (keyCombinationUP.match(keyEvent)) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                increaseElementSizeEventHandler.handle(keyEvent);
            } else if (keyCombinationDOWN.match(keyEvent)) {
                log.info("Key Value :{}", keyEvent.getCode().toString());
                decreaseElementSizeEventHandler.handle(keyEvent);
            }
        });

    }

}

package net.gazeplay.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.Translator;

import static net.gazeplay.ui.QuickControl.*;

/**
 * Control of the size and color of the progress indicators of the fixations, in the side panel of the game screen.
 */
@NoArgsConstructor
public class ProgressBarControl {

    @Getter
    private static final ProgressBarControl instance = new ProgressBarControl();

    /**
     * @return the pane of the control: a slider of the size and one colored square per color
     */
    public TitledPane createProgressControlPane(Configuration config, Translator translator, Scene primaryScene) {
        Label progressBarSizeLabel = new Label("");
        I18NLabel colorStringLabel = new I18NLabel(translator, "Color");
        Label[] colorLabels = this.createColorLabel(config);
        progressBarSizeLabel.setMinWidth(ICON_SIZE);
        Slider progressBarSizeSlider = createProgressBarSizeSlider(config, progressBarSizeLabel);

        HBox line1 = new HBox();
        line1.setSpacing(CONTENT_SPACING);
        line1.setAlignment(Pos.CENTER);
        line1.getChildren().addAll(progressBarSizeLabel, progressBarSizeSlider);

        HBox line2 = new HBox();
        line2.setSpacing(CONTENT_SPACING);
        line2.setAlignment(Pos.CENTER);
        line2.getChildren().add(colorStringLabel);
        for (Label colorLabel : colorLabels) {
            line2.getChildren().add(colorLabel);
        }

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().addAll(line1, line2);
        content.setPrefHeight(PREF_HEIGHT);

        I18NTitledPane pane = new I18NTitledPane(translator, "ProgressBarSizeAndColor");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    /**
     * @return the slider of the size, bound to the PROGRESS_BAR_SIZE setting, its value shown in the label
     */
    public Slider createProgressBarSizeSlider(Configuration config, Label progressBarSizeLabel) {
        final int initialSizeValue = config.getProgressBarSize();
        Slider slider = new Slider();
        slider.setMinWidth(QuickControl.SLIDERS_MIN_WIDTH);
        slider.setMaxWidth(QuickControl.SLIDERS_PREF_WIDTH);
        slider.setPrefWidth(QuickControl.SLIDERS_MAX_WIDTH);
        slider.setMin(15);
        slider.setMax(100);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(5);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setValue(initialSizeValue);

        progressBarSizeLabel.setText(formatValue(initialSizeValue));

        // user can reset ratio to default just by clicking on the label
        progressBarSizeLabel.setOnMouseClicked(event -> slider.setValue(100));

        slider.valueProperty().addListener((observable) -> {
            int sizeRatioValue = sliderValueToSizeRatio(slider.getValue());
            String labelText = formatValue(sizeRatioValue);
            progressBarSizeLabel.setText(labelText);
            config.getProgressBarSizeProperty().set(sizeRatioValue);
        });

        return slider;
    }

    /**
     * Marks the square of the chosen color.
     */
    public void setTextOnLabel(Label[] labels, int position){
        for(int i=0; i< labels.length; i++){
            if(i==position) labels[i].setText("⬤");
            else labels[i].setText("");
        }
    }

    /**
     * Colors a square, and saves its color in the PROGRESS_BAR_COLOR setting when it is clicked.
     */
    public void initializeColorLabel(Label[] labels, int position, String colorName, Configuration config){
        labels[position].setStyle("-fx-background-color: " + colorName + ";");
        labels[position].setOnMouseClicked(event -> {
            config.getProgressBarColorProperty().set(colorName);
            setTextOnLabel(labels, position);
        });
        if(config.getProgressBarColor().equals(colorName))
            setTextOnLabel(labels, position);
    }

    /**
     * @return the squares of the colors, the one of the settings marked
     */
    public Label[] createColorLabel(Configuration config){
        // Sequence : Red Orange Yellow Limegreen Cyan DodgerBlue Violet
        Label[] labels = new Label[7];
        for(int i=0; i<labels.length; i++) {
            labels[i] = new Label();
            labels[i].setMinSize(25, 25);
            labels[i].setAlignment(Pos.CENTER);
        }
        for(int i=0; i<labels.length; i++){
            switch (i) {
                case 0 -> initializeColorLabel(labels, 0, "RED", config);
                case 1 -> initializeColorLabel(labels, 1, "ORANGE", config);
                case 2 -> initializeColorLabel(labels, 2, "YELLOW", config);
                case 3 -> initializeColorLabel(labels, 3, "LIMEGREEN", config);
                case 4 -> initializeColorLabel(labels, 4, "CYAN", config);
                case 5 -> initializeColorLabel(labels, 5, "DODGERBLUE", config);
                default -> initializeColorLabel(labels, 6, "VIOLET", config);
            }
        }
        return labels;
    }

    /**
     * @return the size as shown, in percent
     */
    public String formatValue(int speedRatioValue) {
        return speedRatioValue + "%";
    }

    /**
     * @return the size of a value of the slider
     */
    public int sliderValueToSizeRatio(double value) {
        return (int)value;
    }
}

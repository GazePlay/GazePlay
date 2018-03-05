/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.colors;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;

@Slf4j
public class ColorToolBox extends BorderPane {

    /**
     * Pourcents use to compute height and width.
     */
    public static final double WIDTH_POURCENT = 0.25;
    public static final double HEIGHT_POURCENT = 0.8;

    public static final double SPACING_PX = 10;

    public static final Insets MAIN_INSETS = new Insets(50, 15, 50, 15);

    private final VBox mainPane;

    /**
     * All the color boxes
     */
    private final List<ColorBox> colorBoxes;

    /**
     * The index of the first color displayed (then followed by the NB_COLORS_DISPLAYED next colors).
     */
    private int firstColorDisplayed;
    private static final Integer NB_COLORS_DISPLAYED = 6;

    @Getter
    private ColorBox selectedColorBox;

    public ColorToolBox() {
        super();

        final ReadOnlyDoubleProperty height = GazePlay.getInstance().getPrimaryStage().heightProperty();
        final ReadOnlyDoubleProperty width = GazePlay.getInstance().getPrimaryStage().widthProperty();

        height.addListener((observable) -> {

            this.setPrefHeight(height.doubleValue() * HEIGHT_POURCENT);
        });

        width.addListener((observable) -> {

            this.setPrefHeight(width.doubleValue() * WIDTH_POURCENT);
        });

        this.selectedColorBox = null;

        this.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        mainPane = new VBox();
        this.setCenter(mainPane);
        mainPane.setSpacing(SPACING_PX);
        mainPane.setPadding(MAIN_INSETS);

        ColorBox colorBox;
        EventHandler<MouseEvent> mouseHandler;

        // COLORS

        List<Color> colors = new ArrayList<Color>();
        colors.add(Color.AZURE);
        colors.add(Color.BEIGE);
        colors.add(Color.BLUEVIOLET);
        colors.add(Color.CORNFLOWERBLUE);
        colors.add(Color.DARKGOLDENROD);
        colors.add(Color.DIMGREY);
        // 6
        colors.add(Color.RED);
        colors.add(Color.BLUE);

        colorBoxes = new ArrayList<ColorBox>();
        ToggleGroup group = new ToggleGroup();
        firstColorDisplayed = 0;
        Color color;
        for (int i = 0; i < colors.size(); ++i) {

            color = colors.get(i);

            colorBox = new ColorBox(color);
            mouseHandler = new ColorMouseEventHandler(colorBox);

            if (i < NB_COLORS_DISPLAYED) {
                mainPane.getChildren().add(colorBox);
            }

            colorBox.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseHandler);
            colorBox.setToggleGroup(group);
            colorBoxes.add(colorBox);

            if (this.selectedColorBox == null) {
                colorBox.select();
                selectedColorBox = colorBox;
            }
        }
        Button previousPallet = new Button("<");
        Button nextPallet = new Button(">");

        nextPallet.setOnAction((event) -> {

            firstColorDisplayed += NB_COLORS_DISPLAYED;
            mainPane.getChildren().clear();

            updatePallet(previousPallet, nextPallet);
        });

        if (firstColorDisplayed + NB_COLORS_DISPLAYED > colorBoxes.size()) {
            nextPallet.setDisable(true);
        }

        previousPallet.setOnAction((event) -> {

            firstColorDisplayed -= NB_COLORS_DISPLAYED;
            mainPane.getChildren().clear();

            updatePallet(previousPallet, nextPallet);
        });

        if (firstColorDisplayed - NB_COLORS_DISPLAYED < 0) {
            previousPallet.setDisable(true);
        }

        this.setRight(nextPallet);
        this.setLeft(previousPallet);

        this.getStyleClass().add("bg-colored");
    }

    private class ColorMouseEventHandler implements EventHandler<MouseEvent> {

        private final ColorBox colorBox;

        public ColorMouseEventHandler(final ColorBox colorBox) {
            this.colorBox = colorBox;
        }

        @Override
        public void handle(MouseEvent event) {

            // If already selected, then do nothing
            if (selectedColorBox.equals(colorBox)) {
                return;
            }

            selectedColorBox.unselect();
            colorBox.select();
            selectedColorBox = colorBox;
        }
    }

    private void updatePallet(Button previousPallet, Button nextPallet) {

        for (int i = firstColorDisplayed; i < firstColorDisplayed + NB_COLORS_DISPLAYED && i < colorBoxes.size(); ++i) {
            mainPane.getChildren().add(colorBoxes.get(i));
        }

        if (nextPallet.isDisable() && firstColorDisplayed + NB_COLORS_DISPLAYED < colorBoxes.size()) {
            nextPallet.setDisable(false);
        }

        if (previousPallet.isDisable() && firstColorDisplayed - NB_COLORS_DISPLAYED >= 0) {

            previousPallet.setDisable(false);
        }

        if (firstColorDisplayed + NB_COLORS_DISPLAYED >= colorBoxes.size()) {
            nextPallet.setDisable(true);
        } else if (firstColorDisplayed - NB_COLORS_DISPLAYED < 0) {
            previousPallet.setDisable(true);
        }
    }
}

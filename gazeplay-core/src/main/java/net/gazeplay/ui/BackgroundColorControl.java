package net.gazeplay.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.I18NTitledPane;
import net.gazeplay.commons.ui.Translator;

import java.util.ArrayList;

import static net.gazeplay.ui.QuickControl.*;

@NoArgsConstructor
public class BackgroundColorControl {

    @Getter
    private static final BackgroundColorControl instance = new BackgroundColorControl();
    ArrayList<Label> labels;

    public TitledPane createBackgroundColorControlPane(Configuration config, Translator translator, Pane gamingRoot) {

        I18NLabel colorLabel = new I18NLabel(translator, "Color");

        labels = new ArrayList<>();

        final Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIMEGREEN, Color.CYAN, Color.DODGERBLUE, Color.VIOLET, Color.BLACK, Color.WHITE};

        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            Label label = new Label();
            labels.add(label);
            label.setMinSize(25, 25);
            label.setAlignment(Pos.CENTER);
            label.setBackground(new Background(new BackgroundFill(color, null, null)));
            if(color==Color.WHITE){
                label.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
            int finalI = i;
            label.setOnMouseClicked(mouseEvent -> {
                config.getBackgroundColorProperty().setValue(color.toString());
                gamingRoot.setBackground(new Background(new BackgroundFill(Color.web(config.getBackgroundColorProperty().getValue()), null, null)));
                setSelected(finalI);
            });
            if (config.getBackgroundColorProperty().getValue().equals(color.toString())){
                setSelected(i);
            }
        }

        HBox colorBox = new HBox();
        colorBox.setSpacing(CONTENT_SPACING);
        colorBox.setAlignment(Pos.CENTER);
        colorBox.getChildren().add(colorLabel);
        for (Label label : labels) {
            colorBox.getChildren().add(label);
        }

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(CONTENT_SPACING);
        content.getChildren().add(colorBox);
        content.setPrefHeight(PREF_HEIGHT);

        I18NTitledPane pane = new I18NTitledPane(translator, "BackgroundColor");
        pane.setCollapsible(false);
        pane.setContent(content);
        return pane;
    }

    public void setSelected(int pos){
        for (int i = 0; i < labels.size(); i++) {
            if(i==pos) labels.get(i).setText("⬤");
            else    labels.get(i).setText("");
        }
    }

}

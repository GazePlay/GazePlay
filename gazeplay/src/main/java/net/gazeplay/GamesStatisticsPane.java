package net.gazeplay;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import lombok.Getter;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;

public class GamesStatisticsPane extends GridPane {

    @Getter
    private final Label gamesCountValueLabel;

    public GamesStatisticsPane(Translator translator) {
        super();
        I18NLabel gamesCountLabel = new I18NLabel(translator, "games count");
        gamesCountValueLabel = new Label();
        gamesCountValueLabel.setText("0");

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;";
        gamesCountLabel.setStyle(labelStyle);
        gamesCountValueLabel.setStyle(labelStyle);

        this.add(gamesCountLabel, 0, 0);
        this.add(gamesCountValueLabel, 1, 0);

        setPrefSize(500, 100);
        setGridLinesVisible(false);
        setPadding(new Insets(20, 20, 20, 20));
        setHgap(30);
        setVgap(10);

        final int numCols = 2;
        final int numRows = 1;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / numCols);
            getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / numRows);
            getRowConstraints().add(rowConst);
        }
    }

}

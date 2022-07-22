package net.gazeplay.ui.scenes.gamemenu;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;

import java.util.List;

public class GamesStatisticsPane extends GridPane {

    @Getter
    private final Label gamesCountValueLabel;

    @Getter
    private final Label gamesVariantsCountValueLabel;

    public GamesStatisticsPane(Translator translator, List<GameSpec> games, boolean darkMode) {
        super();
        I18NLabel gamesCountLabel = new I18NLabel(translator, "games count");
        I18NLabel gamesVariantsCountLabel = new I18NLabel(translator, "games variants count");
        gamesCountValueLabel = new Label();
        gamesVariantsCountValueLabel = new Label();

        this.add(gamesCountLabel, 0, 0);
        this.add(gamesCountValueLabel, 1, 0);

        this.add(gamesVariantsCountLabel, 0, 1);
        this.add(gamesVariantsCountValueLabel, 1, 1);

        setGridLinesVisible(false);
        setPadding(new Insets(20, 20, 20, 20));
        setHgap(30);
        setVgap(10);

        //setAllColumnsConstraints();
        if (darkMode) {
            setAllLabelsStyleDark();
        } else {
            setAllLabelsStyleLight();
        }

        final int gamesCount = games.size();
        final int variantsCount = games.stream().mapToInt(gameSpec -> gameSpec.getGameVariantGenerator().getVariants().size()).sum();
        gamesCountValueLabel.setText(Integer.toString(gamesCount));
        gamesVariantsCountValueLabel.setText(Integer.toString(variantsCount));
    }

    public void refreshPreferredSize() {
        double maxPreferredWidth = 0d;
        for (Node node : this.getChildren()) {
            if (node instanceof I18NLabel) {
                I18NLabel label = (I18NLabel) node;
                maxPreferredWidth = Math.max(maxPreferredWidth, label.getPrefWidth());
            }
        }
        setPrefWidth(maxPreferredWidth * 2 + 40);
    }

    private void setAllColumnsConstraints() {
        final int columnsCount = 2;
        for (int i = 0; i < columnsCount; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / columnsCount);
            getColumnConstraints().add(colConst);
        }
    }

    private void setAllLabelsStyleDark() {
        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;";
        this.setStyle("-fx-background-color: black");
        for (Node node : this.getChildren()) {
            node.setStyle(labelStyle);
        }
    }

    public void setAllLabelsStyleLight() {
        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: black;";
        this.setStyle("-fx-background-color: #fffaf0");

        for (Node node : this.getChildren()) {
            node.setStyle(labelStyle);
        }
    }

}

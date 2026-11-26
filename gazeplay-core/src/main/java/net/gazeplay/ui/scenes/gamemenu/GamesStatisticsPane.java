package net.gazeplay.ui.scenes.gamemenu;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.ui.Translator;

import java.util.List;

/**
 * The current profile and the number of games, shown at the bottom right of the menu (look of the menus: style class
 * {@code gp-stats}, menus.css). The number of variants is no longer shown (maintainer, 26/11/2026: not useful).
 */
public class GamesStatisticsPane extends VBox {

    @Getter
    private final Label gamesCountValueLabel;

    /**
     * @param profileName the name of the current profile, as shown to the user
     */
    public GamesStatisticsPane(Translator translator, List<GameSpec> games, String profileName) {
        super(2);
        getStyleClass().add("gp-stats");

        gamesCountValueLabel = new Label(Integer.toString(games.size()));

        getChildren().addAll(
            line(translator.translate("CurrentProfile"), new Label(profileName)),
            line(translator.translate("games count"), gamesCountValueLabel));
    }

    /**
     * @return a line "name: value"
     */
    private static HBox line(final String name, final Label value) {
        value.getStyleClass().add("gp-stats-value");
        final HBox line = new HBox(8, new Label(name), value);
        line.setAlignment(Pos.BASELINE_RIGHT);
        return line;
    }
}

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
 * The current profile, the number of games and of variants, shown at the bottom right of the menu (look of the menus:
 * style class {@code gp-stats}, menus.css).
 */
public class GamesStatisticsPane extends VBox {

    @Getter
    private final Label gamesCountValueLabel;

    @Getter
    private final Label gamesVariantsCountValueLabel;

    /**
     * @param profileName the name of the current profile, as shown to the user
     */
    public GamesStatisticsPane(Translator translator, List<GameSpec> games, String profileName) {
        super(2);
        getStyleClass().add("gp-stats");

        final int gamesCount = games.size();
        final int variantsCount = games.stream().mapToInt(gameSpec -> gameSpec.getGameVariantGenerator().getVariants().size()).sum();
        gamesCountValueLabel = new Label(Integer.toString(gamesCount));
        gamesVariantsCountValueLabel = new Label(Integer.toString(variantsCount));

        getChildren().addAll(
            line(translator.translate("CurrentProfile"), new Label(profileName)),
            line(translator.translate("games count"), gamesCountValueLabel),
            line(translator.translate("games variants count"), gamesVariantsCountValueLabel));
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

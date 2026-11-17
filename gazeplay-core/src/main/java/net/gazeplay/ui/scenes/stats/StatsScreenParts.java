package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * Parts shared by the screens opened from the statistics (areas of interest and scan path, refonte of 17/11/2026,
 * maquettes B chosen by the maintainer): the look of the statistics screen (menus.css, light and dark themes), its
 * header, its buttons, and the view of the game screen scaled down to the room it has.
 */
final class StatsScreenParts {

    private StatsScreenParts() {
    }

    /**
     * Gives a screen the look of the statistics screen, with smaller texts and margins on a narrow screen.
     */
    static void style(final BorderPane root, final Locale locale) {
        MenuStyle.applyTo(root);
        root.getStyleClass().add("gp-stats-screen");
        if ("ar".equals(locale.getLanguage())) {
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        root.widthProperty().addListener((observable, before, width) -> {
            final boolean narrow = width.doubleValue() > 0 && width.doubleValue() < StatsContext.NARROW_WIDTH;
            if (narrow && !root.getStyleClass().contains(StatsContext.NARROW)) {
                root.getStyleClass().add(StatsContext.NARROW);
            } else if (!narrow) {
                root.getStyleClass().remove(StatsContext.NARROW);
            }
        });
    }

    /**
     * @return the translation of a key, or the key itself when it has none
     */
    static String text(final Translator translator, final String key) {
        final String translated = translator.translate(key);
        return translated == null ? key : translated;
    }

    /**
     * @return the header: the title of the screen, then the game, its variant, the profile, the date and the given
     *   chips, and on the right the given switch (or nothing)
     */
    static HBox header(final Translator translator, final Locale locale, final Stats stats,
                       final Configuration config, final String titleKey, final List<String> extraChips,
                       final Node right) {
        final Label title = new Label(text(translator, titleKey));
        title.getStyleClass().add("gp-stats-game-title");

        final FlowPane chips = new FlowPane(8, 8);
        final String game = stats.getCurrentGameNameCode();
        if (game != null && !game.isBlank()) {
            chips.getChildren().add(chip(text(translator, game)));
        }
        final String variant = StatsFigures.variant(stats.getCurrentGameVariant(), game, translator);
        if (variant != null && !variant.isBlank()) {
            chips.getChildren().add(chip(variant));
        }
        final String user = config.getUserName();
        if (user != null && !user.isBlank()) {
            chips.getChildren().add(chip(text(translator, "StatsProfile") + " " + user));
        }
        chips.getChildren().add(chip(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
            .format(LocalDateTime.now())));
        for (final String extra : extraChips) {
            chips.getChildren().add(chip(extra));
        }

        final VBox identity = new VBox(8, title, chips);
        HBox.setHgrow(identity, Priority.ALWAYS);
        final HBox header = right == null ? new HBox(24, identity) : new HBox(24, identity, right);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    static Label chip(final String text) {
        final Label chip = new Label(text);
        chip.getStyleClass().add("gp-stats-chip");
        return chip;
    }

    /**
     * @return the switch "Gaze | Mouse | ..." of the header, in the look of "Simple | Advanced"; one of them is always
     *   chosen, and choosing another one calls {@code onChoice} with its index
     */
    static HBox sourceSwitch(final List<String> labels, final int chosen, final IntConsumer onChoice) {
        final ToggleGroup group = new ToggleGroup();
        final HBox box = new HBox(4);
        for (int i = 0; i < labels.size(); i++) {
            final ToggleButton button = new ToggleButton(labels.get(i));
            button.setToggleGroup(group);
            button.setSelected(i == chosen);
            box.getChildren().add(button);
        }
        group.selectedToggleProperty().addListener((observable, before, now) -> {
            if (now == null) {
                group.selectToggle(before);
            } else {
                onChoice.accept(box.getChildren().indexOf((Node) now));
            }
        });
        box.getStyleClass().add("gp-stats-mode");
        box.setMaxHeight(Region.USE_PREF_SIZE);
        return box;
    }

    /**
     * @return a small switch of the bar (speed...), the chosen one framed in the main color
     */
    static ToggleButton tab(final String text, final ToggleGroup group) {
        final ToggleButton tab = new ToggleButton(text);
        tab.getStyleClass().add("gp-stats-tab");
        tab.setToggleGroup(group);
        return tab;
    }

    static Button button(final String text, final MenuIcon icon, final boolean primary, final Runnable action) {
        final Button button = new Button(text, icon.create(22));
        button.getStyleClass().addAll("gp-button", primary ? "gp-button-primary" : "gp-button-secondary");
        button.setOnAction(e -> action.run());
        return button;
    }

    /**
     * @return the information bubble of an area or a fixation: a title, then rows "label ... value"
     */
    static VBox bubble(final String title, final String[][] rows) {
        final Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("gp-stats-bubble-title");
        final VBox bubble = new VBox(4, titleLabel);
        for (final String[] row : rows) {
            final Label label = new Label(row[0]);
            label.getStyleClass().add("gp-stats-detail-label");
            final Label value = new Label(row[1]);
            value.getStyleClass().add("gp-stats-detail-value");
            final Region gap = new Region();
            HBox.setHgrow(gap, Priority.ALWAYS);
            final HBox line = new HBox(16, label, gap, value);
            line.setAlignment(Pos.BASELINE_LEFT);
            bubble.getChildren().add(line);
        }
        bubble.getStyleClass().add("gp-stats-bubble");
        bubble.setMouseTransparent(true);
        bubble.setManaged(false);
        bubble.autosize();
        return bubble;
    }

    /**
     * Places a bubble of the overlay next to a node of the game view: on its right, or on its left when there is no
     * room, kept inside the overlay.
     */
    static void placeBubble(final Pane overlay, final VBox bubble, final Node target) {
        if (!overlay.getChildren().contains(bubble)) {
            overlay.getChildren().add(bubble);
        }
        bubble.applyCss();
        bubble.autosize();
        final Bounds bounds = overlay.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        if (bounds == null) {
            return;
        }
        final double width = bubble.getWidth();
        final double height = bubble.getHeight();
        double x = bounds.getMaxX() + 16;
        if (x + width > overlay.getWidth() - 8) {
            x = bounds.getMinX() - 16 - width;
        }
        x = Math.max(8, Math.min(x, overlay.getWidth() - width - 8));
        final double y = Math.max(8, Math.min(bounds.getMinY(), overlay.getHeight() - height - 8));
        bubble.relocate(x, y);
    }

    /**
     * The game screen (drawn in its own pixels, the size of the screen) scaled down to the room it has, keeping its
     * proportions, centered. Its background is white in both themes, as the heat maps (16/11/2026).
     */
    static final class GameView extends Region {

        private final Pane screen;

        private final double screenWidth;

        private final double screenHeight;

        GameView(final Pane screen, final double screenWidth, final double screenHeight) {
            this.screen = screen;
            this.screenWidth = Math.max(1, screenWidth);
            this.screenHeight = Math.max(1, screenHeight);
            screen.getStyleClass().add("gp-stats-game-view");
            screen.setMinSize(this.screenWidth, this.screenHeight);
            screen.setPrefSize(this.screenWidth, this.screenHeight);
            screen.setMaxSize(this.screenWidth, this.screenHeight);
            screen.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            getChildren().add(screen);
            setMinSize(0, 0);
            setPrefSize(10, 10);
            setPadding(Insets.EMPTY);
        }

        /**
         * @return how much the game screen is scaled down
         */
        double scale() {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return 1;
            }
            return Math.min(getWidth() / screenWidth, getHeight() / screenHeight);
        }

        @Override
        protected void layoutChildren() {
            final double scale = scale();
            screen.resize(screenWidth, screenHeight);
            // The scale is around the center of the screen: centering it unscaled centers it scaled too
            screen.setScaleX(scale);
            screen.setScaleY(scale);
            screen.relocate((getWidth() - screenWidth) / 2, (getHeight() - screenHeight) / 2);
        }
    }

    /**
     * @return a time of the game, "m:ss,d" (minutes, seconds and tenths, with the decimal separator of the language)
     */
    static String time(final long milliseconds, final Locale locale) {
        final long positive = Math.max(0, milliseconds);
        final long minutes = positive / 60_000;
        final double seconds = (positive % 60_000) / 1000.0;
        return String.format(locale, "%d:%04.1f", minutes, seconds);
    }

    /**
     * @return a duration in seconds, "1,4 s"
     */
    static String seconds(final double seconds, final Locale locale) {
        return String.format(locale, "%.1f s", seconds);
    }
}

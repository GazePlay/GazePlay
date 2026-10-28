package net.gazeplay.ui.scenes.modeselect;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AppMode;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.menus.CardButton;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

/**
 * The first page of the application, before the profile page: the choice of the mode, the games or the evaluations
 * of GazePlay Eval. Chosen with the mouse or the keyboard only (decision of the maintainer: the adult chooses).
 * <p>
 * Look of the menus (menus.css): quit at the top right; in the middle the logo, the question, then a large card per
 * mode, with its icon and its name. The cards go one below the other on a narrow screen.
 */
@Slf4j
public class ModeSelectionContext extends GraphicalContext<BorderPane> {

    /**
     * Width of the name of a mode on its card, the card being this width plus its padding (menus.css).
     */
    private static final double TEXT_WIDTH = 400;

    private static final double ICON_SIZE = 88;

    /**
     * The buttons of the two modes, in the order of {@link AppMode}.
     */
    @Getter
    private final Button gamesButton;

    @Getter
    private final Button evaluationsButton;

    public ModeSelectionContext(final GazePlay gazePlay) {
        super(gazePlay, new BorderPane());
        MenuStyle.applyTo(root);

        final Translator translator = gazePlay.getTranslator();

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final Button exitButton = MenuStyle.quitButton(translator.translate("Exit"), gazePlay::quit);
        final HBox topBar = new HBox(spacer, exitButton);
        topBar.getStyleClass().add("gp-top-bar");

        final Node logo = LogoFactory.getInstance().createLogoStatic(gazePlay.getPrimaryStage());
        final Label prompt = MenuStyle.title(translator.translate("ModeChoose"));

        gamesButton = createModeButton(gazePlay, translator, AppMode.GAMES, MenuIcon.GAMES, "gp-mode-games");
        evaluationsButton = createModeButton(gazePlay, translator, AppMode.EVALUATIONS, MenuIcon.EVALUATIONS,
            "gp-mode-evaluations");

        // Cards of the same size, side by side, or one below the other on a narrow screen
        final TilePane modes = new TilePane(48, 32, gamesButton, evaluationsButton);
        modes.setPrefColumns(2);
        modes.setAlignment(Pos.CENTER);

        final VBox center = new VBox(40, logo, prompt, modes);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(0, 24, 72, 24));

        // Scrolls only when the window is too small for the cards
        final ScrollPane scrollPane = new ScrollPane(center);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setTop(topBar);
        root.setCenter(scrollPane);
    }

    /**
     * @return the large card of a mode: its icon and its name; a click (or Enter, Space) chooses it
     */
    private static Button createModeButton(final GazePlay gazePlay, final Translator translator, final AppMode mode,
                                           final MenuIcon icon, final String styleClass) {
        final String name = translator.translate(mode.getTranslationKey());

        final StackPane badge = new StackPane(icon.create(ICON_SIZE));
        badge.getStyleClass().add("gp-mode-badge");

        final Label nameLabel = wrappedLabel(name, "gp-mode-name");

        final VBox content = new VBox(24, badge, nameLabel);
        content.setAlignment(Pos.TOP_CENTER);

        final Button button = new CardButton(name, content);
        button.getStyleClass().add(styleClass);
        // As high as the other card (TilePane)
        button.setMaxHeight(Double.MAX_VALUE);
        button.setOnAction(e -> gazePlay.chooseMode(mode));
        return button;
    }

    /**
     * @return a centered text of the width of the card, on several lines if needed: with a fixed width, its height is
     * the one of its lines (a wrapping label without width is measured as if one word per line)
     */
    private static Label wrappedLabel(final String text, final String styleClass) {
        final Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(TEXT_WIDTH);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}

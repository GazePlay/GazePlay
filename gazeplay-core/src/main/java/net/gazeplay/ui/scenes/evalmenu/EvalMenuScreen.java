package net.gazeplay.ui.scenes.evalmenu;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AppMode;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.menus.CardButton;
import net.gazeplay.ui.menus.MenuButtons;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;
import net.gazeplay.ui.scenes.configuration.EvalSettings;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.util.List;

/**
 * The menu of the "Evaluations" mode (look of the menus, menus.css, REFONTE_GRAPHIQUE.md 4.4): the same top bar as
 * the menu of the games (settings, full screen, logout, quit); in the middle a large card launching an evaluation (a
 * window then shows the imported evaluations, {@link EvalChooserDialog}, or says that there is none), then three cards:
 * the test of an evaluation (GazePlay Eval Test: no form, nothing recorded, to check the calibration), the import of a
 * .zip of the GazePlay Eval website, and the folder of the results; the current profile and
 * the version at the bottom right. As many evaluations as wanted can be imported.
 */
@Slf4j
public class EvalMenuScreen extends GraphicalContext<BorderPane> {

    private static final double LOGO_HEIGHT = 64;

    /**
     * Name codes of GazePlay Eval and of GazePlay Eval Test in the catalog.
     */
    static final String EVAL = "GazePlayEval";
    static final String EVAL_TEST = "GazePlayEvalTest";

    private final GameMenuController gameMenuController;

    /**
     * GazePlay Eval in the catalog; null if it is missing (the launch button is then disabled).
     */
    private final GameSpec evalSpec;

    /**
     * GazePlay Eval Test (nothing recorded, to check the calibration); null if it is missing (its button is then
     * disabled).
     */
    private final GameSpec testSpec;

    @Getter
    private final Button launchButton;

    @Getter
    private final Button testButton;

    @Getter
    private final Button importButton;

    @Getter
    private final Button resultsButton;

    public EvalMenuScreen(final GazePlay gazePlay, final GameMenuController gameMenuController,
                          final GamesLocator gamesLocator) {
        super(gazePlay, new BorderPane());
        this.gameMenuController = gameMenuController;
        MenuStyle.applyTo(root);

        final Translator translator = gazePlay.getTranslator();

        final List<GameSpec> evaluations = AppMode.EVALUATIONS.filter(gamesLocator.listGames(translator));
        evalSpec = find(evaluations, EVAL);
        testSpec = find(evaluations, EVAL_TEST);

        // Top: logo, then settings, full screen, logout and quit, as in the menu of the games
        final Node logo = LogoFactory.getInstance().createLogoStatic(LOGO_HEIGHT);
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final HBox topBar = new HBox(logo, spacer,
            MenuStyle.roundButton(MenuIcon.SETTINGS, "sky", translator.translate("Options"),
                gazePlay::onDisplayConfigurationManagement),
            MenuButtons.fullScreenButton(gazePlay, translator),
            MenuButtons.themeButton(gazePlay, translator),
            MenuStyle.roundButton(MenuIcon.LOGOUT, "accent", translator.translate("Logout"), gazePlay::goToUserPage),
            MenuStyle.quitButton(translator.translate("Exit"), gazePlay::quit));
        topBar.getStyleClass().add("gp-top-bar");

        // Center: the mode, then launch, import, results
        final Label titleLabel = MenuStyle.title(translator.translate(AppMode.EVALUATIONS.getTranslationKey()));

        // No count of the imported evaluations: the window of the evaluations says when there is none (maintainer's
        // feedback of 28/10/2026)
        final String launchText = translator.translate("EvalLaunch");
        final Label launchLabel = new Label(launchText);
        launchLabel.getStyleClass().add("gp-eval-launch-name");
        final VBox launchContent = new VBox(16, badge(MenuIcon.PLAY, 72, "gp-eval-launch-badge"), launchLabel);
        launchContent.setAlignment(Pos.CENTER);
        launchButton = new CardButton(launchText, launchContent);
        launchButton.getStyleClass().add("gp-eval-launch");
        launchButton.setPrefWidth(520);
        launchButton.setDisable(evalSpec == null);
        launchButton.setOnAction(e -> openChooser(evalSpec));

        testButton = secondaryCard(MenuIcon.TEST, translator.translate("EvalTestLaunch"));
        testButton.setDisable(testSpec == null);
        testButton.setOnAction(e -> openChooser(testSpec));

        importButton = secondaryCard(MenuIcon.UPLOAD_FILE, translator.translate("EvalImportButton"));
        importButton.setOnAction(e -> EvalSettings.importEvaluation(translator, gazePlay.getPrimaryStage()));

        resultsButton = secondaryCard(MenuIcon.FOLDER, translator.translate("EvalResults"));
        resultsButton.setOnAction(e -> EvalSettings.openResultsFolder());

        // Side by side, or below one another on a narrow window
        final FlowPane secondaryButtons = new FlowPane(24, 24, testButton, importButton, resultsButton);
        secondaryButtons.setAlignment(Pos.CENTER);

        final VBox center = new VBox(32, titleLabel, launchButton, secondaryButtons);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(16, 24, 32, 24));

        // Scrolls only when the window is too small
        final ScrollPane scrollPane = new ScrollPane(center);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Bottom right: the current profile and the version (licence)
        final HBox profileLine = new HBox(8, new Label(translator.translate("CurrentProfile")),
            valueLabel(MenuButtons.currentProfileName(translator)));
        profileLine.setAlignment(Pos.BASELINE_RIGHT);
        profileLine.getStyleClass().add("gp-stats");
        final VBox info = new VBox(6, profileLine, MenuButtons.versionButton(gazePlay, translator));
        info.setAlignment(Pos.BOTTOM_RIGHT);
        final Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);
        final HBox bottomBar = new HBox(bottomSpacer, info);
        bottomBar.getStyleClass().add("gp-bottom-bar");

        root.setTop(topBar);
        root.setCenter(scrollPane);
        root.setBottom(bottomBar);
    }

    /**
     * @return a round colored badge with an icon
     */
    private static StackPane badge(final MenuIcon icon, final double size, final String styleClass) {
        final StackPane badge = new StackPane(icon.create(size));
        badge.getStyleClass().addAll("gp-eval-badge", styleClass);
        return badge;
    }

    /**
     * @return a card of the second row: an icon and a text side by side
     */
    private static Button secondaryCard(final MenuIcon icon, final String text) {
        final Label label = new Label(text);
        label.setWrapText(true);
        label.setPrefWidth(220);
        label.setMinHeight(Region.USE_PREF_SIZE);
        final HBox content = new HBox(16, badge(icon, 32, "gp-eval-small-badge"), label);
        content.setAlignment(Pos.CENTER_LEFT);
        final Button card = new CardButton(text, content);
        card.getStyleClass().add("gp-eval-card");
        return card;
    }

    private static Label valueLabel(final String text) {
        final Label label = new Label(text);
        label.getStyleClass().add("gp-stats-value");
        return label;
    }

    /**
     * @return the game of the given name code among the evaluations, null if it is missing
     */
    private static GameSpec find(final List<GameSpec> evaluations, final String nameCode) {
        return evaluations.stream().filter(spec -> spec.getGameSummary().getNameCode().equals(nameCode))
            .findFirst().orElse(null);
    }

    /**
     * Opens the window of the imported evaluations, over the menu (blurred and disabled until it closes); the chosen
     * one is played by GazePlay Eval or GazePlay Eval Test.
     */
    void openChooser(final GameSpec spec) {
        if (spec == null) {
            return;
        }
        root.setEffect(new BoxBlur());
        root.setDisable(true);
        final EvalChooserDialog dialog = new EvalChooserDialog(getGazePlay(), gameMenuController, spec, root);
        dialog.show();
        dialog.toFront();
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}

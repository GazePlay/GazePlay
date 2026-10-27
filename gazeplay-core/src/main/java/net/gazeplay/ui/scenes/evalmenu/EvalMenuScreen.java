package net.gazeplay.ui.scenes.evalmenu;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AppMode;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.eval.EvalLibrary;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NTooltip;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ConfigurationButtonFactory;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.scenes.configuration.EvalSettings;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.util.List;

/**
 * The menu of the "Evaluations" mode: the button launching an evaluation (a window then shows the imported
 * evaluations, {@link EvalChooserDialog}), the import of a .zip of the GazePlay Eval website, the folder of the results,
 * and the buttons of the settings, of the profiles and to quit. As many evaluations as wanted can be imported.
 */
@Slf4j
public class EvalMenuScreen extends GraphicalContext<BorderPane> {

    private final GameMenuController gameMenuController;

    /**
     * GazePlay Eval in the catalog; null if it is missing (the launch button is then disabled).
     */
    private final GameSpec evalSpec;

    @Getter
    private final Button launchButton;

    @Getter
    private final Button importButton;

    @Getter
    private final Button resultsButton;

    @Getter
    private final Label countLabel;

    private final Label titleLabel;

    public EvalMenuScreen(final GazePlay gazePlay, final GameMenuController gameMenuController,
                          final GamesLocator gamesLocator) {
        super(gazePlay, new BorderPane());
        this.gameMenuController = gameMenuController;

        final Translator translator = gazePlay.getTranslator();
        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        final List<GameSpec> evaluations = AppMode.EVALUATIONS.filter(gamesLocator.listGames(translator));
        evalSpec = evaluations.isEmpty() ? null : evaluations.getFirst();

        // Top: logo, profiles and quit
        final Node logo = LogoFactory.getInstance().createLogoStatic(gazePlay.getPrimaryStage());
        final CustomButton logoutButton = new CustomButton("data/common/images/logout.png", screenDimension);
        logoutButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> gazePlay.goToUserPage());
        I18NTooltip.install(logoutButton, new I18NTooltip(translator, "Logout"));
        final CustomButton exitButton = new CustomButton("data/common/images/power-off.png", screenDimension);
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> gazePlay.quit());
        I18NTooltip.install(exitButton, new I18NTooltip(translator, "Exit"));

        final HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        topRightPane.getChildren().addAll(logoutButton, exitButton);

        final BorderPane topPane = new BorderPane();
        topPane.setPadding(new Insets(15));
        topPane.setCenter(logo);
        topPane.setRight(topRightPane);

        // Center: launch, import, results
        titleLabel = new Label(translator.translate(AppMode.EVALUATIONS.getTranslationKey()));
        countLabel = new Label();

        launchButton = new I18NButton(translator, "EvalLaunch");
        launchButton.getStyleClass().addAll("gameChooserButton", "button");
        launchButton.setStyle("-fx-font-size: 32; -fx-font-weight: bold;");
        launchButton.setPrefSize(screenDimension.getWidth() / 3, screenDimension.getHeight() / 4);
        launchButton.setDisable(evalSpec == null);
        launchButton.setOnAction(e -> openChooser());

        importButton = new I18NButton(translator, "EvalImportButton");
        importButton.getStyleClass().addAll("gameChooserButton", "button");
        importButton.setStyle("-fx-font-size: 20;");
        importButton.setOnAction(e -> {
            EvalSettings.importEvaluation(translator, gazePlay.getPrimaryStage());
            updateCount();
        });

        resultsButton = new I18NButton(translator, "EvalResults");
        resultsButton.getStyleClass().addAll("gameChooserButton", "button");
        resultsButton.setStyle("-fx-font-size: 20;");
        resultsButton.setOnAction(e -> EvalSettings.openResultsFolder());

        final HBox secondaryButtons = new HBox(40, importButton, resultsButton);
        secondaryButtons.setAlignment(Pos.CENTER);

        final VBox center = new VBox(40, titleLabel, countLabel, launchButton, secondaryButtons);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));

        // Bottom: settings and full screen
        final ConfigurationButton configurationButton = ConfigurationButtonFactory.createConfigurationButton(gazePlay);
        I18NTooltip.install(configurationButton, new I18NTooltip(translator, "Options"));
        final HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.CENTER_LEFT);
        leftControlPane.getChildren().add(configurationButton);

        final HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER_RIGHT);
        rightControlPane.getChildren().add(createToggleFullScreenButtonInGameScreen(gazePlay));

        final BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setRight(rightControlPane);

        root.setTop(topPane);
        root.setCenter(center);
        root.setBottom(bottomPane);

        updateCount();
    }

    /**
     * Shows the number of imported evaluations.
     */
    void updateCount() {
        final int count = EvalLibrary.names().size();
        final Translator translator = getGazePlay().getTranslator();
        countLabel.setText(count == 0 ? translator.translate("EvalNone")
            : translator.translate("EvalCount").replace("{0}", String.valueOf(count)));
    }

    /**
     * Opens the window of the imported evaluations, over the menu (blurred and disabled until it closes).
     */
    void openChooser() {
        if (evalSpec == null) {
            return;
        }
        root.setEffect(new BoxBlur());
        root.setDisable(true);
        final EvalChooserDialog dialog = new EvalChooserDialog(getGazePlay(), gameMenuController, evalSpec, root);
        dialog.show();
        dialog.toFront();
    }

    @Override
    public void setUpOnStage(final Scene scene) {
        super.setUpOnStage(scene);
        updateCount();
        // Colors of the theme, as the other menus
        final boolean dark = ActiveConfigurationContext.getInstance().isDarkThemeEnabled();
        final String textColor = dark ? "white" : "black";
        root.setStyle(dark ? "-fx-background-color: rgba(0,0,0,1);" : "-fx-background-color: #fffaf0;");
        titleLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        countLabel.setStyle("-fx-font-size: 18; -fx-text-fill: " + textColor + ";");
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}

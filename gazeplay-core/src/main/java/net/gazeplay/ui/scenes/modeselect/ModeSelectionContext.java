package net.gazeplay.ui.scenes.modeselect;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AppMode;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.ui.GraphicalContext;

/**
 * The first page of the application, before the profile page: the choice of the mode, the games or the evaluations
 * of GazePlay Eval. Chosen with the mouse or the keyboard only (decision of the maintainer: the adult chooses).
 */
@Slf4j
public class ModeSelectionContext extends GraphicalContext<BorderPane> {

    /**
     * The buttons of the two modes, in the order of {@link AppMode}.
     */
    @Getter
    private final Button gamesButton;

    @Getter
    private final Button evaluationsButton;

    public ModeSelectionContext(final GazePlay gazePlay) {
        super(gazePlay, new BorderPane());

        final Translator translator = gazePlay.getTranslator();
        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        final Node logo = LogoFactory.getInstance().createLogoAnimated(gazePlay.getPrimaryStage());

        final HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        final CustomButton exitButton = new CustomButton("data/common/images/power-off.png", screenDimension);
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> gazePlay.quit());
        topRightPane.getChildren().add(exitButton);

        final BorderPane topPane = new BorderPane();
        topPane.setCenter(logo);
        topPane.setRight(topRightPane);

        final Label prompt = new Label(translator.translate("ModeChoose"));
        prompt.getStyleClass().add("modeChooserPrompt");
        prompt.setStyle("-fx-font-size: 32; -fx-font-weight: bold;");

        gamesButton = createModeButton(gazePlay, translator, AppMode.GAMES, "ModeGamesDescription", screenDimension);
        evaluationsButton = createModeButton(gazePlay, translator, AppMode.EVALUATIONS, "ModeEvaluationsDescription",
            screenDimension);

        final HBox modes = new HBox(80, gamesButton, evaluationsButton);
        modes.setAlignment(Pos.CENTER);

        final VBox center = new VBox(60, prompt, modes);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));

        root.setTop(topPane);
        root.setCenter(center);
        if (ActiveConfigurationContext.getInstance().isDarkThemeEnabled()) {
            root.setStyle("-fx-background-color: rgba(0,0,0,1);");
            prompt.setStyle(prompt.getStyle() + " -fx-text-fill: white;");
        } else {
            root.setStyle("-fx-background-color: #fffaf0;");
            prompt.setStyle(prompt.getStyle() + " -fx-text-fill: black;");
        }
    }

    /**
     * @return the large button of a mode: its name and what it contains; a click (or Enter, Space) chooses it
     */
    private static Button createModeButton(final GazePlay gazePlay, final Translator translator, final AppMode mode,
                                           final String descriptionKey, final Dimension2D screenDimension) {
        final Button button = new Button(translator.translate(mode.getTranslationKey()) + "\n\n"
            + translator.translate(descriptionKey));
        button.getStyleClass().add("gameChooserButton");
        button.getStyleClass().add("button");
        button.setWrapText(true);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setStyle("-fx-font-size: 24;");
        button.setPrefSize(screenDimension.getWidth() / 3.5, screenDimension.getHeight() / 3);
        button.setOnAction(e -> gazePlay.chooseMode(mode));
        return button;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}

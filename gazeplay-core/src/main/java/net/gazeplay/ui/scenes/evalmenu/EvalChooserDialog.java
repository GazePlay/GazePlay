package net.gazeplay.ui.scenes.evalmenu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.util.ArrayList;
import java.util.List;

/**
 * The window opened by the launch button of the menu of the evaluations: one button per imported evaluation, which
 * starts it (in its own JVM, as a game), and a cancel button.
 */
public class EvalChooserDialog extends Stage {

    /**
     * The buttons of the evaluations, in the order of their names.
     */
    @Getter
    private final List<Button> evaluationButtons = new ArrayList<>();

    @Getter
    private final Button cancelButton;

    public EvalChooserDialog(final GazePlay gazePlay, final GameMenuController gameMenuController,
                             final GameSpec evalSpec, final Parent menuRoot) {
        final Stage primaryStage = gazePlay.getPrimaryStage();
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        initStyle(StageStyle.UTILITY);
        final Translator translator = gazePlay.getTranslator();
        setTitle(translator.translate("EvalChoose"));

        final Label prompt = new Label(translator.translate("EvalChoose"));
        prompt.setStyle("-fx-font-weight: bold; -fx-font-size: 32; -fx-text-fill: black; -fx-padding: 30 0 0 0");

        final FlowPane choices = new FlowPane(20, 20);
        choices.setAlignment(Pos.CENTER);
        choices.setPadding(new Insets(20));
        for (IGameVariant variant : evalSpec.getGameVariantGenerator().getVariants()) {
            final Button button = new Button(variant.getLabel(translator));
            button.getStyleClass().addAll("gameChooserButton", "gameVariation", "button");
            button.setWrapText(true);
            button.setMinSize(primaryStage.getWidth() / 10, primaryStage.getHeight() / 10);
            button.setPrefSize(primaryStage.getWidth() / 7, primaryStage.getHeight() / 7);
            final Runnable start = () -> {
                close();
                gameMenuController.chooseAndStartNewGameProcess(gazePlay, evalSpec, variant);
            };
            button.setOnAction(e -> start.run());
            evaluationButtons.add(button);
            choices.getChildren().add(button);
        }
        if (evaluationButtons.isEmpty()) {
            final Label none = new Label(translator.translate("EvalNone"));
            none.setStyle("-fx-font-size: 20; -fx-text-fill: black;");
            choices.getChildren().add(none);
        }

        final ScrollPane scroller = new ScrollPane(choices);
        scroller.setFitToWidth(true);
        scroller.setFitToHeight(true);

        cancelButton = new I18NButton(translator, "Cancel");
        cancelButton.getStyleClass().addAll("gameChooserButton", "button");
        cancelButton.setOnAction(e -> close());

        final VBox bottom = new VBox(cancelButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(0, 0, 30, 0));

        final VBox top = new VBox(prompt);
        top.setAlignment(Pos.CENTER);

        final BorderPane content = new BorderPane();
        content.setTop(top);
        content.setCenter(scroller);
        content.setBottom(bottom);

        // The menu comes back when the window closes, whatever closes it
        setOnHidden(e -> {
            menuRoot.setEffect(null);
            menuRoot.setDisable(false);
        });

        final Scene scene = new Scene(content, Color.TRANSPARENT);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), scene,
            gazePlay.getCurrentScreenDimensionSupplier());
        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
    }
}

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
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.menus.MenuDialog;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.util.ArrayList;
import java.util.List;

/**
 * The window opened by the launch button of the menu of the evaluations: one button per imported evaluation, which
 * starts it (in its own JVM, as a game), and a cancel button. Look of the menus ({@link MenuDialog}): the question as
 * title, a close button, no frame of the system.
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
        MenuDialog.undecorate(this);
        final Translator translator = gazePlay.getTranslator();
        setTitle(translator.translate("EvalChoose"));

        final FlowPane choices = new FlowPane(16, 16);
        choices.setAlignment(Pos.CENTER);
        choices.setPadding(new Insets(16, 24, 16, 24));
        for (IGameVariant variant : evalSpec.getGameVariantGenerator().getVariants()) {
            final Button button = new Button(variant.getLabel(translator));
            button.getStyleClass().add("gp-variant");
            button.setWrapText(true);
            button.setTextAlignment(TextAlignment.CENTER);
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
            none.getStyleClass().add("gp-subtitle");
            choices.getChildren().add(none);
        }

        final ScrollPane scroller = new ScrollPane(choices);
        scroller.setFitToWidth(true);
        scroller.setFitToHeight(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        cancelButton = new Button(translator.translate("Cancel"));
        cancelButton.getStyleClass().addAll("gp-button", "gp-button-secondary");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> close());

        final HBox bottom = new HBox(cancelButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(8, 0, 24, 0));

        final BorderPane content = new BorderPane();
        content.setCenter(scroller);
        content.setBottom(bottom);

        // The menu comes back when the window closes, whatever closes it
        setOnHidden(e -> {
            menuRoot.setEffect(null);
            menuRoot.setDisable(false);
        });

        final Scene scene = MenuDialog.createScene(this, content, translator.translate("Close"), this::close);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), scene,
            gazePlay.getCurrentScreenDimensionSupplier());
        setScene(scene);
        setWidth(primaryStage.getWidth() * 0.6);
        setHeight(primaryStage.getHeight() * 0.6);
    }
}

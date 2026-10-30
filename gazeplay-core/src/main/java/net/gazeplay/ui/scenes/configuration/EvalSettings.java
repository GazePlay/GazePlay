package net.gazeplay.ui.scenes.configuration;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.EvalOptions;
import net.gazeplay.commons.eval.EvalImporter;
import net.gazeplay.commons.eval.EvalLibrary;
import net.gazeplay.commons.eval.EvalProblem;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.ui.SystemFileManager;
import net.gazeplay.ui.menus.MenuMessageDialog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controls of the settings page for GazePlay Eval: the imported evaluations (list, deletion) and the way the
 * evaluations are taken (EVAL_OPTIONS). Also the import of a .zip of the website and the opening of the folder of the
 * results, used by the menu of the evaluations.
 */
@Slf4j
public final class EvalSettings {

    private static final double PREF_WIDTH = 200;

    private EvalSettings() {
    }

    /**
     * @return the list of the imported evaluations, each with a delete button (they are imported from the menu of the
     *   evaluations, which also opens the folder of their results)
     */
    static Node buildLibrary(Translator translator, Stage owner) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        refreshLibrary(box, translator, owner);
        return box;
    }

    private static void refreshLibrary(VBox box, Translator translator, Stage owner) {
        box.getChildren().clear();
        List<String> names = EvalLibrary.names();
        if (names.isEmpty()) {
            Label none = new Label(translator.translate("EvalNone"));
            none.getStyleClass().add("item");
            box.getChildren().add(none);
        }
        for (String name : names) {
            // A bullet before each name, so that it reads as a list (maintainer's request of 30/10/2026)
            Label label = new Label("•  " + name);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
            Button delete = new I18NButton(translator, "EvalDelete");
            delete.setOnAction(e -> {
                if (confirm(translator, owner, translator.translate("EvalDelete"),
                    format(translator.translate("EvalDeleteConfirm"), name), "EvalDelete")) {
                    try {
                        EvalLibrary.delete(name);
                    } catch (IOException exception) {
                        log.error("Cannot delete the evaluation {}", name, exception);
                        inform(translator, owner, true, translator.translate("EvalDelete"), exception.getMessage());
                    }
                    refreshLibrary(box, translator, owner);
                }
            });
            HBox row = new HBox(10, label, delete);
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
    }

    /**
     * Asks for a .zip of the website, checks it, asks before replacing an evaluation of the same name, then imports it.
     */
    public static void importEvaluation(Translator translator, Stage owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(translator.translate("EvalImportButton"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GazePlay Eval (.zip)", "*.zip", "*.ZIP"));
        File downloads = new File(System.getProperty("user.home"), "Downloads");
        if (downloads.isDirectory()) {
            chooser.setInitialDirectory(downloads);
        }
        File zip = chooser.showOpenDialog(owner);
        if (zip == null) {
            return;
        }
        String title = translator.translate("EvalImportButton");
        EvalImporter.Prepared prepared;
        try {
            prepared = EvalImporter.prepare(zip.toPath(), EvalLibrary.folder());
        } catch (IOException | RuntimeException e) {
            log.warn("Cannot import {}", zip, e);
            inform(translator, owner, true, title, translator.translate("EvalImportFailed") + "\n" + e.getMessage());
            return;
        }
        if (prepared.hasErrors()) {
            EvalImporter.discard(prepared);
            inform(translator, owner, true, title, translator.translate("EvalImportFailed") + "\n\n"
                + problems(prepared.problems(), translator));
            return;
        }
        if (prepared.replacesExisting() && !confirm(translator, owner, title,
            format(translator.translate("EvalReplaceConfirm"), prepared.name()), "EvalReplace")) {
            EvalImporter.discard(prepared);
            return;
        }
        try {
            EvalImporter.commit(prepared);
        } catch (IOException e) {
            log.error("Cannot put the evaluation {} in place", prepared.name(), e);
            EvalImporter.discard(prepared);
            inform(translator, owner, true, title, translator.translate("EvalImportFailed") + "\n" + e.getMessage());
            return;
        }
        log.info("Evaluation {} imported from {} ({} warnings)", prepared.name(), zip, prepared.problems().size());
        String message = format(translator.translate("EvalImportSuccess"), prepared.name());
        if (!prepared.problems().isEmpty()) {
            message += "\n\n" + translator.translate("EvalWarnings") + "\n" + problems(prepared.problems(), translator);
        }
        inform(translator, owner, false, title,
            message);
    }

    /**
     * @return the choice of the way the evaluations are taken (EVAL_OPTIONS)
     */
    static ChoiceBox<EvalOptions> buildOptionsChooser(Configuration configuration, Translator translator) {
        ChoiceBox<EvalOptions> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(EvalOptions.values());
        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(EvalOptions option) {
                return option == null ? "" : translator.translate(option.translationKey());
            }

            @Override
            public EvalOptions fromString(String string) {
                return null;
            }
        });
        choiceBox.getSelectionModel().select(configuration.getEvalOptions());
        choiceBox.setPrefWidth(PREF_WIDTH * 1.5);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                configuration.getEvalOptionsProperty().setValue(newValue.name());
            }
        });
        return choiceBox;
    }

    /**
     * Opens the folder of the results of the evaluations (GazePlayLearning_Eval in Documents), created if needed.
     */
    public static void openResultsFolder() {
        File folder = GazePlayDirectories.getEvaluationResultsFolder();
        if (!folder.isDirectory() && !folder.mkdirs()) {
            log.warn("Cannot create the folder {}", folder);
        }
        SystemFileManager.openFolder(folder);
    }

    private static String problems(List<EvalProblem> problems, Translator translator) {
        return problems.stream().limit(30).map(problem -> "• " + problem.format(translator))
            .collect(Collectors.joining("\n"));
    }

    private static String format(String pattern, String argument) {
        return pattern.replace("{0}", argument);
    }

    /**
     * Asks a question in the look of the menus.
     *
     * @param okKey key of the translation of the button accepting it
     */
    private static boolean confirm(Translator translator, Stage owner, String title, String message, String okKey) {
        return MenuMessageDialog.confirm(owner, title, message, translator.translate(okKey),
            translator.translate("Cancel"), translator.translate("Close"));
    }

    /**
     * Shows a message in the look of the menus (an error in the color "danger" of the theme).
     */
    private static void inform(Translator translator, Stage owner, boolean error, String title, String message) {
        MenuMessageDialog.inform(owner, title, message, error, translator.translate("OkButton"),
            translator.translate("Close"));
    }
}

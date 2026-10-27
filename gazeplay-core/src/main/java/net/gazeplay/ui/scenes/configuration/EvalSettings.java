package net.gazeplay.ui.scenes.configuration;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The controls of the settings page for GazePlay Eval: the imported evaluations (import of a .zip of the website,
 * deletion), the way the evaluations are taken (EVAL_OPTIONS) and the folder of their results.
 */
@Slf4j
final class EvalSettings {

    private static final double PREF_WIDTH = 200;

    private EvalSettings() {
    }

    /**
     * @return the list of the imported evaluations, each with a delete button, and the import button
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
            Label label = new Label(name);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
            Button delete = new I18NButton(translator, "EvalDelete");
            delete.setOnAction(e -> {
                if (confirm(owner, translator.translate("EvalDelete"),
                    format(translator.translate("EvalDeleteConfirm"), name))) {
                    try {
                        EvalLibrary.delete(name);
                    } catch (IOException exception) {
                        log.error("Cannot delete the evaluation {}", name, exception);
                        inform(owner, Alert.AlertType.ERROR, translator.translate("EvalDelete"), exception.getMessage());
                    }
                    refreshLibrary(box, translator, owner);
                }
            });
            HBox row = new HBox(10, label, delete);
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
        Button importButton = new I18NButton(translator, "EvalImportButton");
        importButton.setOnAction(e -> {
            importEvaluation(translator, owner);
            refreshLibrary(box, translator, owner);
        });
        box.getChildren().add(importButton);
    }

    /**
     * Asks for a .zip of the website, checks it, asks before replacing an evaluation of the same name, then imports it.
     */
    static void importEvaluation(Translator translator, Stage owner) {
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
            inform(owner, Alert.AlertType.ERROR, title, translator.translate("EvalImportFailed") + "\n" + e.getMessage());
            return;
        }
        if (prepared.hasErrors()) {
            EvalImporter.discard(prepared);
            inform(owner, Alert.AlertType.ERROR, title, translator.translate("EvalImportFailed") + "\n\n"
                + problems(prepared.problems(), translator));
            return;
        }
        if (prepared.replacesExisting() && !confirm(owner, title,
            format(translator.translate("EvalReplaceConfirm"), prepared.name()))) {
            EvalImporter.discard(prepared);
            return;
        }
        try {
            EvalImporter.commit(prepared);
        } catch (IOException e) {
            log.error("Cannot put the evaluation {} in place", prepared.name(), e);
            EvalImporter.discard(prepared);
            inform(owner, Alert.AlertType.ERROR, title, translator.translate("EvalImportFailed") + "\n" + e.getMessage());
            return;
        }
        log.info("Evaluation {} imported from {} ({} warnings)", prepared.name(), zip, prepared.problems().size());
        String message = format(translator.translate("EvalImportSuccess"), prepared.name());
        if (!prepared.problems().isEmpty()) {
            message += "\n\n" + translator.translate("EvalWarnings") + "\n" + problems(prepared.problems(), translator);
        }
        inform(owner, prepared.problems().isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING, title,
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
     * @return the button opening the folder of the results of the evaluations (GazePlayLearning_Eval in Documents)
     */
    static Node buildResultsFolderButton(Translator translator) {
        Button button = new I18NButton(translator, "SeeFolder");
        button.setOnAction(e -> {
            File folder = GazePlayDirectories.getEvaluationResultsFolder();
            if (!folder.isDirectory() && !folder.mkdirs()) {
                log.warn("Cannot create the folder {}", folder);
            }
            SystemFileManager.openFolder(folder);
        });
        return button;
    }

    private static String problems(List<EvalProblem> problems, Translator translator) {
        return problems.stream().limit(30).map(problem -> "• " + problem.format(translator))
            .collect(Collectors.joining("\n"));
    }

    private static String format(String pattern, String argument) {
        return pattern.replace("{0}", argument);
    }

    private static boolean confirm(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.OK;
    }

    private static void inform(Stage owner, Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        // In a text area: the list of the problems can be long
        TextArea text = new TextArea(message);
        text.setEditable(false);
        text.setWrapText(true);
        text.setPrefRowCount(Math.min(20, message.split("\n").length + 1));
        text.setPrefColumnCount(70);
        alert.getDialogPane().setContent(text);
        alert.showAndWait();
    }
}

package net.gazeplay.ui.scenes.configuration;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Slf4j
public class ImportGame extends Stage {

    private Configuration configuration;
    private ConfigurationContext configurationContext;
    private Translator translator;
    private GazePlay gazePlay;

    private FlowPane flowPanes = new FlowPane();

    private String folder = "evals";

    ImportGame(Configuration configuration,
               ConfigurationContext configurationContext,
               Translator translator,
               GazePlay gazePlay) {
        super();
        this.configuration = configuration;
        this.configurationContext = configurationContext;
        this.translator = translator;
        this.gazePlay = gazePlay;

        this.setMinWidth(500);
        this.setMinHeight(500);

        final Stage primaryStage = gazePlay.getPrimaryStage();
        this.initOwner(primaryStage);
        this.initModality(Modality.WINDOW_MODAL);
        this.initStyle(StageStyle.UTILITY);
        this.setOnCloseRequest(
            windowEvent -> primaryStage.getScene().getRoot().setEffect(null));
        this.setTitle(translator.translate("importEvalChooser"));
        this.toFront();

        buildCustomColorDialog();
    }

    private void buildCustomColorDialog() {
        final BorderPane imageSelector = new BorderPane();
        final Scene scene = new Scene(imageSelector, 500, 500, Color.TRANSPARENT);
        initImportGameSelectorPane(imageSelector, scene);
        this.setScene(scene);
    }

    private BorderPane initImportGameSelectorPane(BorderPane imageSelectorGridPane, Scene scene) {

        Group group = new Group();
        StackPane section;
        Color colors = Color.BLUEVIOLET;
        String imagePath = "data/common/images/eval.png";

        HBox input = buildImportGameChooser();
        input.setPadding(new Insets(20, 0, 20, 0));
        input.setAlignment(Pos.CENTER);
        imageSelectorGridPane.setTop(input);

        DropShadow dropShadow = createNewDropShadow();

        BorderPane background = new BorderPane();
        background.layoutYProperty().bind(input.heightProperty().add(50));
        background.setLayoutX(0);
        background.minWidthProperty().bind(scene.widthProperty());
        background.minHeightProperty().bind(scene.heightProperty().subtract(50).subtract(input.heightProperty()));
        background.maxWidthProperty().bind(scene.widthProperty());
        background.maxHeightProperty().bind(scene.heightProperty().subtract(50).subtract(input.heightProperty()));
        background.setBackground(new Background(new BackgroundFill(colors, CornerRadii.EMPTY, null)));

        this.flowPanes.setAlignment(Pos.CENTER);
        this.flowPanes.setHgap(10);
        this.flowPanes.setVgap(10);
        this.flowPanes.setPadding(new Insets(20, 20, 20, 20));
        this.flowPanes.setBackground(new Background(new BackgroundFill(colors, CornerRadii.EMPTY, null)));

        ScrollPane scrollPane = new ScrollPane(this.flowPanes);
        scrollPane.setBackground(new Background(new BackgroundFill(colors, CornerRadii.EMPTY, null)));
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        StackPane addButtonStackPane = new StackPane();
        Rectangle backgroundAddButton = new Rectangle();
        backgroundAddButton.widthProperty().bind(background.widthProperty());
        backgroundAddButton.setHeight(50);
        backgroundAddButton.setFill(colors);
        VBox choice = new VBox();
        choice.setAlignment(Pos.CENTER);
        choice.setPadding(new Insets(20, 20, 20, 20));
        choice.setSpacing(5);
        I18NButton add = createAddButton();
        add.setMinWidth(300);
        addButtonStackPane.getChildren().addAll(backgroundAddButton, add);

        BorderPane.setAlignment(addButtonStackPane, Pos.CENTER);
        background.setTop(addButtonStackPane);
        background.setCenter(scrollPane);

        section = new StackPane();
        section.layoutYProperty().bind(input.heightProperty());
        Rectangle ongletBackground = new Rectangle();
        ongletBackground.setHeight(50);
        ongletBackground.widthProperty().bind(background.maxWidthProperty());
        ongletBackground.setFill(colors);
        section.getChildren().add(ongletBackground);
        section.setOnMouseClicked(e -> {
            group.toFront();
        });

        group.getChildren().addAll(background, section);
        group.setEffect(dropShadow);

        ImageView sectionLogo = new ImageView(new Image(imagePath));
        sectionLogo.setPreserveRatio(true);
        sectionLogo.setFitHeight(30);
        Label sectionLabel = new Label(folder);
        HBox title = new HBox(sectionLogo, sectionLabel);
        title.setSpacing(10);
        title.setAlignment(Pos.CENTER);
        StackPane.setAlignment(title, Pos.CENTER);
        section.getChildren().add(title);

        imageSelectorGridPane.getChildren().addAll(group);
        updateFlow();
        return imageSelectorGridPane;
    }

    private DropShadow createNewDropShadow() {
        DropShadow dropShadowRight = new DropShadow();
        dropShadowRight.setRadius(10);
        dropShadowRight.setOffsetX(0);
        dropShadowRight.setOffsetY(0);
        dropShadowRight.setColor(Color.color(0, 0, 0, 0.5));
        return dropShadowRight;
    }

    private I18NButton createAddButton() {
        I18NButton add = new I18NButton(translator, "addNewEval");
        add.setPrefHeight(10);
        add.setOnAction(e -> {
            String folderPath = configuration.getFileDir() + "\\evals\\";
            File dir = new File(folderPath);
            if (dir.mkdirs() || dir.exists()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(
                    new File(folderPath)
                );
                List<File> files =
                    fileChooser.showOpenMultipleDialog(this);
                if (files != null) {
                    for (File f : files) {
                        try {
                            unzipFile(f.getPath(), folderPath);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            } else {
                log.debug("File {} doesn't exist and can't be created", dir.getAbsolutePath());
            }
            updateFlow();
        });
        return add;
    }

    private void unzipFile(String zipFile, String destDir) throws IOException {

        final File destDirectory = new File(destDir);
        final byte[] buffer = new byte[1024];
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            final File newUnzipFile = newUnzipFile(destDirectory, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newUnzipFile.isDirectory() && !newUnzipFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newUnzipFile);
                }
            } else {
                File parent = newUnzipFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                try (FileOutputStream fos = new FileOutputStream(newUnzipFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    public static File newUnzipFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private HBox buildImportGameChooser() {
        final HBox pane = new HBox(5);
        String fileDir = configuration.getFileDir() + "\\evals\\";
        Button buttonLoad = new Button(fileDir);

        buttonLoad.setOnAction(arg0 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            final File currentFolder;
            currentFolder = new File(fileDir);
            if (currentFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentFolder);
            }
            final GazePlay gazePlay = configurationContext.getGazePlay();
            final Scene scene = gazePlay.getPrimaryScene();
            File file = directoryChooser.showDialog(scene.getWindow());
            if (file == null) {
                return;
            }
            String newPropertyValue = file.getAbsolutePath();
            if (Utils.isWindows()) {
                newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
            }
            buttonLoad.textProperty().setValue(newPropertyValue);
            configuration.setFileDir(newPropertyValue);
        });

        pane.getChildren().addAll(buttonLoad);
        return pane;
    }

    private void updateFlow() {
        flowPanes.getChildren().clear();

        File directoryPath = new File(configuration.getFileDir() + "\\evals\\");
        String[] content = directoryPath.list();

        if (content != null){
            List<String> allGamesList = new LinkedList<>(Arrays.asList(content));

            for (String nameGame : allGamesList) {
                StackPane preview = new StackPane();

                Rectangle backgroundPreview = new Rectangle(110, 110);
                backgroundPreview.setFill(Color.WHITE);

                Label text = new Label(nameGame);

                Button delete = new Button("x");
                delete.setPrefWidth(10);
                delete.setPrefHeight(10);
                StackPane.setAlignment(delete, Pos.TOP_RIGHT);
                delete.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
                    final Stage dialog = removeDialogue(nameGame, preview);

                    final String dialogTitle = translator.translate("Remove");
                    dialog.setTitle(dialogTitle);

                    dialog.toFront();

                    dialog.show();
                    this.getScene().getRoot().setEffect(new GaussianBlur());
                });

                preview.getChildren().addAll(backgroundPreview, text, delete);
                flowPanes.getChildren().add(preview);
            }
        }
    }

    private Stage removeDialogue(String nameGame, StackPane preview) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(this);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> this.getScene().getRoot().setEffect(null));


        EventHandler<Event> yesEventHandler = event -> {
            File directoryPath = new File(configuration.getFileDir() + "\\evals\\");
            File gameToDelete = new File(directoryPath + "/" + nameGame);
            deleteGame(gameToDelete);
            closeDialog(dialog);
            updateFlow();
        };

        final Button yes = createAnswerButton("YesRemove", yesEventHandler);

        EventHandler<Event> noEventHandler = event -> {
            closeDialog(dialog);
        };

        final Button no = createAnswerButton("NoCancel", noEventHandler);

        final HBox choicePane = new HBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        choicePane.getChildren().addAll(yes, no);

        final ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setPrefHeight(gazePlay.getPrimaryStage().getHeight() / 3);
        choicePanelScroller.setPrefWidth(gazePlay.getPrimaryStage().getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        final Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);
        dialog.setScene(scene);
        return dialog;
    }

    public static void deleteGame(File directory) {

        if(directory.isDirectory()) {
            File[] files = directory.listFiles();

            if(files != null) {
                for(File file : files) {
                    deleteGame(file);
                }
            }
        }

        if(directory.delete()) {
            System.out.println(directory + " is deleted");
        }
        else {
            System.out.println("Directory not deleted");
        }
    }

    private void closeDialog(Stage dialog) {
        this.getScene().getRoot().setEffect(null);
        dialog.close();
    }

    private I18NButton createAnswerButton(String text, EventHandler<Event> eventHandler) {
        I18NButton button = new I18NButton(translator, text);
        button.getStyleClass().add("gameChooserButton");
        button.getStyleClass().add("gameVariation");
        button.getStyleClass().add("button");
        button.setMinHeight(gazePlay.getPrimaryStage().getHeight() / 10);
        button.setMinWidth(gazePlay.getPrimaryStage().getWidth() / 10);
        button.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
        return button;
    }
}

package net.gazeplay.ui.scenes.whereisit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomGames;
import net.gazeplay.commons.whereisit.CustomScreen;
import net.gazeplay.components.CssUtil;
import net.gazeplay.components.GameStyle;
import net.gazeplay.ui.menus.MenuDialog;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuMessageDialog;
import net.gazeplay.ui.menus.MenuStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The window making or changing a game of "Where is it?" (mock-up A, chosen by the maintainer on 25/11/2026): its name,
 * the list of its screens at the left, and for the screen chosen the size of its grid, a sentence and a sound if the user
 * wants, its pictures (chosen by a click on a place, or dropped on it) and the right answer. GazePlay makes the folder of
 * the game ({@link CustomGames}).
 */
@Slf4j
public class WhereIsItEditorDialog extends Stage {

    /**
     * Extensions of the sounds that can be given.
     */
    private static final Set<String> SOUND_EXTENSIONS = Set.of("mp3", "wav");

    private static final double SCREENS_WIDTH = 240;

    private final Translator translator;

    private final CustomGame game;

    /**
     * Whether something changed since the game was opened or saved.
     */
    @Getter
    private boolean changed;

    @Getter
    private int chosenScreen;

    @Getter
    private final TextField nameField = new TextField();

    @Getter
    private final TextField sentenceField = new TextField();

    @Getter
    private final VBox screenButtons = new VBox(8);

    @Getter
    private final GridPane grid = new GridPane();

    @Getter
    private final Button saveButton;

    private final Button soundButton = new Button();
    private final Button removeSoundButton;
    private final List<RadioButton> sizeButtons = new ArrayList<>();

    @Getter
    private final Label problemLabel = new Label();

    /**
     * The fields are being filled with the screen chosen: not a change of the user.
     */
    private boolean filling;

    /**
     * @param game a new game, or the one to change (it is changed at once, its folder only when it is saved)
     */
    public WhereIsItEditorDialog(final Stage owner, final Translator translator, final CustomGame game) {
        this.translator = translator;
        this.game = game;
        if (game.getScreens().isEmpty()) {
            game.getScreens().add(new CustomScreen());
        }
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        MenuDialog.undecorate(this);
        setTitle(translator.translate("OwnGameMake"));

        nameField.setText(game.getFolder() == null && game.getName().isEmpty()
            ? translator.translate("OwnGameNew") : game.getName());
        nameField.setPrefColumnCount(20);
        nameField.textProperty().addListener((observable, before, now) -> changed = true);
        final HBox nameRow = new HBox(12, new Label(translator.translate("OwnGameName")), nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        saveButton = new Button(translator.translate("OwnGameSave"));
        removeSoundButton = MenuStyle.iconButton(MenuIcon.DELETE, translator.translate("OwnGameRemoveSound"), () -> {
            screen().setSound(null);
            changed();
        });

        final HBox editor = new HBox(20, screensColumn(), screenPane());
        VBox.setVgrow(editor, Priority.ALWAYS);

        problemLabel.setWrapText(true);
        problemLabel.setStyle("-fx-text-fill: -gp-danger;");
        HBox.setHgrow(problemLabel, Priority.ALWAYS);
        problemLabel.setMaxWidth(Double.MAX_VALUE);
        saveButton.getStyleClass().addAll("gp-button", "gp-button-primary");
        saveButton.setOnAction(event -> {
            if (save()) {
                close();
            }
        });
        saveButton.setMinWidth(Region.USE_PREF_SIZE);
        final HBox bottom = new HBox(12, problemLabel, saveButton);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        final VBox content = new VBox(10, nameRow, editor, bottom);
        content.setPadding(new Insets(0, 24, 18, 24));
        setScene(MenuDialog.createScene(this, content, translator.translate("Close"), this::closeIfAccepted));
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), getScene());
        setWidth(Math.max(900, owner.getWidth()));
        setHeight(Math.max(620, owner.getHeight()));
        showScreen(0);
    }

    private CustomScreen screen() {
        return game.getScreens().get(chosenScreen);
    }

    /**
     * The list of the screens, and the buttons adding, copying and removing one.
     */
    private VBox screensColumn() {
        final Label title = new Label(translator.translate("OwnGameScreensTitle"));
        title.setStyle(GameStyle.titleFontStyle(20));
        final ScrollPane scroller = new ScrollPane(screenButtons);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroller, Priority.ALWAYS);
        final Button add = new Button(translator.translate("OwnGameAddScreen"), MenuIcon.ADD.create(20));
        add.getStyleClass().addAll("gp-button", "gp-button-secondary");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(event -> addScreen(new CustomScreen()));
        final Button copy = new Button(translator.translate("OwnGameCopyScreen"));
        copy.setOnAction(event -> addScreen(screen().copy()));
        final Button remove = new Button(translator.translate("OwnGameRemoveScreen"));
        remove.setOnAction(event -> removeScreen());
        final HBox copyRow = new HBox(8, copy, remove);
        for (final Button button : List.of(copy, remove)) {
            button.getStyleClass().addAll("gp-button", "gp-button-secondary");
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
        }
        for (final Button button : List.of(add, copy, remove)) {
            button.setStyle("-fx-padding: 8 10 8 10; -fx-font-size: 12pt;");
        }
        final VBox column = new VBox(10, title, scroller, add, copyRow);
        column.setMinWidth(SCREENS_WIDTH);
        column.setPrefWidth(SCREENS_WIDTH);
        column.setMaxWidth(SCREENS_WIDTH);
        return column;
    }

    /**
     * The screen chosen: its size, its sentence and sound, its pictures.
     */
    private VBox screenPane() {
        final ToggleGroup sizes = new ToggleGroup();
        final FlowPane sizeRow = new FlowPane(10, 10);
        sizeRow.getStyleClass().add("gp-variant-groups");
        sizeRow.setAlignment(Pos.CENTER_LEFT);
        sizeRow.getChildren().add(new Label(translator.translate("OwnGameSize")));
        for (final int[] size : CustomScreen.SIZES) {
            final RadioButton radio = new RadioButton(size[0] + " × " + size[1]);
            radio.setToggleGroup(sizes);
            radio.setOnAction(event -> chooseSize(size[0], size[1]));
            sizeButtons.add(radio);
            sizeRow.getChildren().add(radio);
        }

        sentenceField.textProperty().addListener((observable, before, now) -> {
            if (!filling) {
                screen().setSentence(now);
                changed = true;
                showScreenButtons();
            }
        });
        HBox.setHgrow(sentenceField, Priority.ALWAYS);
        soundButton.getStyleClass().addAll("gp-button", "gp-button-secondary");
        soundButton.setOnAction(event -> askSound());
        final HBox sentenceRow = new HBox(10, new Label(translator.translate("OwnGameSentence")), sentenceField,
            soundButton, removeSoundButton);
        sentenceRow.setAlignment(Pos.CENTER_LEFT);
        for (final javafx.scene.Node node : List.of(sentenceRow.getChildren().getFirst(), soundButton)) {
            ((Region) node).setMinWidth(Region.USE_PREF_SIZE);
        }

        final Label hint = new Label(translator.translate("OwnGameHint"));
        hint.getStyleClass().add("gp-subtitle");
        hint.setWrapText(true);
        hint.setMinHeight(Region.USE_PREF_SIZE);

        grid.setHgap(12);
        grid.setVgap(12);
        VBox.setVgrow(grid, Priority.ALWAYS);
        final VBox pane = new VBox(10, sizeRow, sentenceRow, hint, grid);
        pane.setPadding(new Insets(12, 18, 12, 18));
        pane.setStyle("-fx-background-color: -gp-surface; -fx-background-radius: 24; -fx-border-color: -gp-border;"
            + " -fx-border-width: 2; -fx-border-radius: 24;");
        HBox.setHgrow(pane, Priority.ALWAYS);
        return pane;
    }

    /**
     * Shows a screen of the game.
     */
    void showScreen(final int index) {
        chosenScreen = index;
        filling = true;
        sentenceField.setText(screen().getSentence());
        filling = false;
        showAll();
    }

    private void changed() {
        changed = true;
        showAll();
    }

    private void showAll() {
        for (int i = 0; i < CustomScreen.SIZES.size(); i++) {
            final int[] size = CustomScreen.SIZES.get(i);
            sizeButtons.get(i).setSelected(size[0] == screen().getLines() && size[1] == screen().getColumns());
        }
        final Path sound = screen().getSound();
        soundButton.setText(sound == null ? translator.translate("OwnGameAddSound") : sound.toFile().getName());
        removeSoundButton.setVisible(sound != null);
        removeSoundButton.setManaged(sound != null);
        showGrid();
        showScreenButtons();
    }

    private void showScreenButtons() {
        screenButtons.getChildren().clear();
        boolean incomplete = false;
        for (int i = 0; i < game.getScreens().size(); i++) {
            final CustomScreen one = game.getScreens().get(i);
            final CustomScreen.Problem problem = one.problem();
            incomplete |= problem != CustomScreen.Problem.NONE;
            final Label title = new Label(translator.translate("WhereIsItScreen") + " " + (i + 1));
            final Label detail = new Label(switch (problem) {
                case PICTURES -> translator.translate("OwnGameMissingPictures");
                case ANSWER -> translator.translate("OwnGameMissingAnswer");
                case NONE -> one.getSentence().isBlank() ? translator.translate("OwnGameNoSentence")
                    : one.getSentence();
            });
            detail.getStyleClass().add("gp-subtitle");
            detail.setStyle("-fx-font-size: 11pt;"
                + (problem == CustomScreen.Problem.NONE ? "" : " -fx-text-fill: -gp-danger;"));
            final Button button = new Button(null, new VBox(title, detail));
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("gp-variant");
            if (i == chosenScreen) {
                button.getStyleClass().add("gp-variant-selected");
            }
            final int index = i;
            button.setOnAction(event -> showScreen(index));
            screenButtons.getChildren().add(button);
        }
        problemLabel.setText(incomplete ? translator.translate("OwnGameNotPlayed") : "");
    }

    /**
     * The places of the grid, each with its picture or a "+".
     */
    private void showGrid() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        for (int column = 0; column < screen().getColumns(); column++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100.0 / screen().getColumns());
            grid.getColumnConstraints().add(constraints);
        }
        for (int line = 0; line < screen().getLines(); line++) {
            final RowConstraints constraints = new RowConstraints();
            constraints.setPercentHeight(100.0 / screen().getLines());
            grid.getRowConstraints().add(constraints);
        }
        for (int place = 0; place < screen().getPlaces(); place++) {
            grid.add(place(place), place % screen().getColumns(), place / screen().getColumns());
        }
    }

    private VBox place(final int place) {
        final Path picture = screen().getPictures().get(place);
        final boolean here = picture != null && Files.isRegularFile(picture);
        final boolean right = here && screen().getRightAnswer() == place;

        final Button pictureButton = new Button();
        pictureButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pictureButton.setMinSize(60, 40);
        pictureButton.getStyleClass().add("gp-variant");
        pictureButton.setStyle("-fx-border-color: " + (right ? "#2E8B45" : "-gp-border") + "; -fx-border-width: "
            + (right ? 5 : 2) + ";" + (here ? " -fx-background-color: " + GameStyle.PICTURE_BACKGROUND + ";"
            : " -fx-border-style: dashed;"));
        if (here) {
            final ImageView view = new ImageView(new Image(picture.toUri().toString(), 400, 400, true, true, true));
            view.setPreserveRatio(true);
            // The picture takes the room of the place, without giving it a size of its own
            final StackPane frame = new StackPane(view);
            frame.setMinSize(0, 0);
            frame.setPrefSize(10, 10);
            frame.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            frame.prefWidthProperty().bind(pictureButton.widthProperty().subtract(28));
            frame.prefHeightProperty().bind(pictureButton.heightProperty().subtract(22));
            view.fitWidthProperty().bind(frame.widthProperty());
            view.fitHeightProperty().bind(frame.heightProperty());
            pictureButton.setGraphic(frame);
            pictureButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            pictureButton.setText(translator.translate("OwnGamePicture"));
            pictureButton.setGraphic(MenuIcon.ADD.create(26));
        }
        pictureButton.setOnAction(event -> askPictures(place));
        pictureButton.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        pictureButton.setOnDragDropped(event -> {
            event.setDropCompleted(event.getDragboard().hasFiles()
                && putPictures(place, event.getDragboard().getFiles()));
            event.consume();
        });
        // In a pane without a size of its own: a large picture does not push the window
        final StackPane room = new StackPane(pictureButton);
        room.setMinSize(0, 0);
        room.setPrefSize(10, 10);
        VBox.setVgrow(room, Priority.ALWAYS);

        final ToggleButton answer = new ToggleButton(translator.translate("OwnGameRightAnswer"));
        answer.getStyleClass().addAll("gp-button", right ? "gp-button-primary" : "gp-button-secondary");
        answer.setStyle("-fx-padding: 4 14 4 14; -fx-font-size: 12pt;"
            + (right ? " -fx-background-color: #23733A; -fx-border-color: #23733A;" : ""));
        answer.setSelected(right);
        answer.setDisable(!here);
        answer.setMinWidth(Region.USE_PREF_SIZE);
        answer.setOnAction(event -> chooseAnswer(place));
        final VBox cell = new VBox(4, room, answer);
        cell.setAlignment(Pos.CENTER);
        return cell;
    }

    /**
     * Chooses the size of the grid of the screen; its pictures keep their order.
     */
    void chooseSize(final int lines, final int columns) {
        screen().setSize(lines, columns);
        changed();
    }

    /**
     * Chooses the right answer of the screen.
     */
    void chooseAnswer(final int place) {
        screen().setRightAnswer(place);
        changed();
    }

    private void askPictures(final int place) {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle(translator.translate("AddImages"));
        chooser.getExtensionFilters().add(filter("Images", UserImagesVariant.IMAGE_EXTENSIONS));
        final List<File> files = chooser.showOpenMultipleDialog(this);
        if (files != null) {
            putPictures(place, files);
        }
    }

    /**
     * Puts pictures in the screen: the first one at a place, the others at the places still empty after it.
     *
     * @return whether at least one was an image
     */
    boolean putPictures(final int place, final List<File> files) {
        final List<File> images = files.stream().filter(UserImagesVariant::isImage).toList();
        if (images.isEmpty()) {
            return false;
        }
        screen().setPicture(place, images.getFirst().toPath());
        int next = 1;
        for (int i = place + 1; i < screen().getPlaces() && next < images.size(); i++) {
            if (screen().getPictures().get(i) == null) {
                screen().setPicture(i, images.get(next).toPath());
                next++;
            }
        }
        changed();
        return true;
    }

    private void askSound() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle(translator.translate("OwnGameAddSound"));
        chooser.getExtensionFilters().add(filter("OwnGameSounds", SOUND_EXTENSIONS));
        final File file = chooser.showOpenDialog(this);
        if (file != null) {
            screen().setSound(file.toPath());
            changed();
        }
    }

    private FileChooser.ExtensionFilter filter(final String name, final Set<String> extensions) {
        return new FileChooser.ExtensionFilter(translator.translate(name), extensions.stream().sorted()
            .flatMap(e -> List.of("*." + e, "*." + e.toUpperCase(Locale.ROOT)).stream()).toList());
    }

    /**
     * Adds a screen just after the one chosen, and shows it.
     */
    void addScreen(final CustomScreen screen) {
        game.getScreens().add(chosenScreen + 1, screen);
        changed = true;
        showScreen(chosenScreen + 1);
    }

    /**
     * Removes the screen chosen; the last one is replaced by an empty one.
     */
    void removeScreen() {
        game.getScreens().remove(chosenScreen);
        if (game.getScreens().isEmpty()) {
            game.getScreens().add(new CustomScreen());
        }
        changed = true;
        showScreen(Math.min(chosenScreen, game.getScreens().size() - 1));
    }

    /**
     * Saves the game; a message says when it cannot be.
     *
     * @return whether it is saved
     */
    boolean save() {
        game.setName(nameField.getText());
        if (game.getName().isEmpty()) {
            game.setName(translator.translate("OwnGameNew"));
        }
        try {
            CustomGames.save(game);
            changed = false;
            return true;
        } catch (final IOException e) {
            log.warn("Cannot save the game {}", game.getName(), e);
            MenuMessageDialog.inform(this, getTitle(), translator.translate("OwnGameNotSaved") + "\n" + e.getMessage(),
                true, translator.translate("Close"), translator.translate("Close"));
            return false;
        }
    }

    /**
     * Closes the window, after a question when something is not saved.
     */
    private void closeIfAccepted() {
        if (!changed || MenuMessageDialog.confirm(this, getTitle(), translator.translate("OwnGameCloseQuestion"),
            translator.translate("OwnGameCloseAnyway"), translator.translate("Cancel"), translator.translate("Close"))) {
            close();
        }
    }
}

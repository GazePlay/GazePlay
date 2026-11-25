package net.gazeplay.ui.scenes.whereisit;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.CustomWhereIsItVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
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
import net.gazeplay.ui.scenes.gamemenu.GameHelp;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * The window of "Where is it?" in the menu (maintainer's request of 25/11/2026, point 86): the games made by the user,
 * each to be chosen, changed or deleted, in a slider showing at least {@value #MIN_VISIBLE} of them, with a card making a
 * new one ({@link WhereIsItEditorDialog}) always at its right, and at the bottom the hearts and "Play" (each screen is
 * played once). As large as the window of the variants of the other games. It replaces the variants of the game and the
 * folder of pictures to choose in the settings.
 */
@Slf4j
public class WhereIsItGamesDialog extends Stage {

    /**
     * The name code of the game.
     */
    public static final String NAME_CODE = "WhereIsIt";

    /**
     * The games seen at once at least.
     */
    static final int MIN_VISIBLE = 2;

    private static final double CARD_WIDTH = 190;
    private static final double THUMBNAILS_HEIGHT = 110;
    private static final double CARD_HEIGHT = THUMBNAILS_HEIGHT + 105;
    private static final double ACTIONS_HEIGHT = 52;
    private static final double GAP = 14;
    /**
     * Around the cards in the slider, for their shadow.
     */
    private static final double MARGIN = 8;
    private static final double CREATE_WIDTH = 160;
    private static final double MIN_WIDTH = 820;
    private static final double MIN_HEIGHT = 620;
    /**
     * The margins of the window and of its shadow.
     */
    private static final double MARGINS = 2 * (24 + 24);
    private static final double ARROWS_WIDTH = 2 * 60;
    private static final int THUMBNAILS = 4;
    private static final Duration SLIDE = Duration.millis(250);

    private final GazePlay gazePlay;
    private final Translator translator;

    /**
     * The cards of the games, in a row sliding behind {@link #viewport}.
     */
    @Getter
    private final HBox cards = new HBox(GAP);

    private final Pane viewport = new Pane(cards);

    @Getter
    private final Button previousButton;

    @Getter
    private final Button nextButton;

    /**
     * The games that can be seen at once in this window.
     */
    private final int visibleGames;

    /**
     * The first game seen.
     */
    @Getter
    private int firstVisible;

    @Getter
    private final Button createButton;

    @Getter
    private final Button playButton;

    private final TranslateTransition sliding = new TranslateTransition(SLIDE, cards);

    private List<CustomGame> games = List.of();

    /**
     * The folder of the game chosen, null when there is none.
     */
    @Getter
    private String chosenFolder;

    private String lives = LivesVariant.options(true).getFirst();

    /**
     * @param root the menu, blurred and disabled while this window is open, given back when it closes
     */
    public WhereIsItGamesDialog(final GazePlay gazePlay, final GameMenuController gameMenuController,
                                final GameSpec gameSpec, final Parent root) {
        this.gazePlay = gazePlay;
        translator = gazePlay.getTranslator();
        final Stage primaryStage = gazePlay.getPrimaryStage();
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        MenuDialog.undecorate(this);
        setTitle(translator.translate(NAME_CODE));
        setOnHidden(event -> {
            root.setEffect(null);
            root.setDisable(false);
        });

        final Label prompt = new Label(translator.translate("OwnGameChoose"));
        prompt.getStyleClass().add("gp-subtitle");
        prompt.setWrapText(true);
        prompt.setTextAlignment(TextAlignment.CENTER);
        prompt.setMinHeight(Region.USE_PREF_SIZE);

        createButton = new Button(translator.translate("OwnGameCreate"), MenuIcon.ADD.create(40));
        createButton.setContentDisplay(ContentDisplay.TOP);
        createButton.getStyleClass().add("gp-variant");
        createButton.setWrapText(true);
        createButton.setTextAlignment(TextAlignment.CENTER);
        createButton.setMinSize(CREATE_WIDTH, CARD_HEIGHT);
        createButton.setPrefSize(CREATE_WIDTH, CARD_HEIGHT);
        createButton.setOnAction(event -> edit(new CustomGame()));

        final double windowWidth = Math.max(MIN_WIDTH, primaryStage.getWidth() * 0.6);
        visibleGames = Math.max(MIN_VISIBLE,
            (int) ((windowWidth - MARGINS - CREATE_WIDTH - ARROWS_WIDTH - 2 * MARGIN) / (CARD_WIDTH + GAP)));
        previousButton = arrow(MenuIcon.BACK, "Previous", -1);
        nextButton = arrow(MenuIcon.FORWARD, "Next", 1);
        cards.setLayoutX(MARGIN);
        cards.setLayoutY(MARGIN);
        final Rectangle clip = new Rectangle();
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());
        viewport.setClip(clip);
        viewport.setOnScroll(event -> slide(event.getDeltaY() + event.getDeltaX() > 0 ? -1 : 1));
        final HBox slider = new HBox(6, previousButton, viewport, nextButton, createButton);
        // The arrows and the card making a game at the height of the cards, above their buttons
        for (final Button button : List.of(previousButton, nextButton, createButton)) {
            HBox.setMargin(button, new Insets(0, 0, ACTIONS_HEIGHT, 0));
        }
        slider.setAlignment(Pos.CENTER);
        VBox.setVgrow(slider, Priority.ALWAYS);

        final FlowPane hearts = switches(LivesVariant.options(true), value -> lives = value);

        playButton = new Button(translator.translate("OwnGamePlay"));
        playButton.getStyleClass().addAll("gp-button", "gp-button-primary");
        playButton.setDefaultButton(true);
        playButton.setOnAction(event -> {
            final CustomWhereIsItVariant variant = variant();
            if (variant != null) {
                close();
                gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
            }
        });
        final Button helpButton = MenuStyle.roundButton(MenuIcon.INFO, "teal", translator.translate("GameHelp"),
            () -> GameHelp.dialog(this, translator, NAME_CODE, getTitle(), null).show());
        final BorderPane bottom = new BorderPane();
        bottom.setCenter(playButton);
        bottom.setRight(helpButton);
        // Room on the left as wide as the help button, so that "Play" is in the middle
        final Region balance = new Region();
        balance.prefWidthProperty().bind(helpButton.widthProperty());
        bottom.setLeft(balance);

        final VBox content = new VBox(10, prompt, slider, hearts, bottom);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(4, 24, 24, 24));

        setScene(MenuDialog.createScene(this, content, translator.translate("Close"), this::close));
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), getScene());
        // The size of the window of the variants, with its help button (GameVariantDialog)
        setWidth(windowWidth);
        setHeight(Math.max(MIN_HEIGHT, primaryStage.getHeight() * 0.65 + 90));
        refresh();
    }

    private Button arrow(final MenuIcon icon, final String text, final int move) {
        final Button button = MenuStyle.iconButton(icon, translator.translate(text), () -> slide(move));
        button.getStyleClass().add("gp-profile-action");
        button.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return button;
    }

    /**
     * Moves the slider by a number of games, as far as the first or the last one.
     */
    void slide(final int move) {
        showFrom(firstVisible + move, true);
    }

    private void showFrom(final int first, final boolean animated) {
        firstVisible = Math.max(0, Math.min(first, games.size() - visibleGames));
        previousButton.setDisable(firstVisible == 0);
        nextButton.setDisable(firstVisible + visibleGames >= games.size());
        final double target = -firstVisible * (CARD_WIDTH + GAP);
        sliding.stop();
        if (animated) {
            sliding.setToX(target);
            sliding.playFromStart();
        } else {
            cards.setTranslateX(target);
        }
    }

    /**
     * Moves the slider so that the game chosen is seen.
     */
    private void showTheChosenGame() {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getFolder().equals(chosenFolder)) {
                if (i < firstVisible) {
                    showFrom(i, false);
                } else if (i >= firstVisible + visibleGames) {
                    showFrom(i - visibleGames + 1, false);
                }
            }
        }
    }

    /**
     * @return a row of switches, the first one selected
     */
    private FlowPane switches(final List<String> values, final Consumer<String> onChosen) {
        final FlowPane row = new FlowPane(10, 10);
        row.getStyleClass().add("gp-variant-groups");
        row.setAlignment(Pos.CENTER);
        row.setMinHeight(Region.USE_PREF_SIZE);
        final ToggleGroup toggles = new ToggleGroup();
        for (final String value : values) {
            final RadioButton radio = new RadioButton(translator.translate(value));
            radio.setToggleGroup(toggles);
            radio.setOnAction(event -> onChosen.accept(value));
            row.getChildren().add(radio);
        }
        ((RadioButton) row.getChildren().getFirst()).setSelected(true);
        return row;
    }

    /**
     * @return the variant of the game chosen, null when none can be played
     */
    CustomWhereIsItVariant variant() {
        final CustomGame game = chosenGame();
        return game == null || game.playableScreens().isEmpty() ? null
            : new CustomWhereIsItVariant(game.getFolder(), lives);
    }

    private CustomGame chosenGame() {
        return games.stream().filter(game -> game.getFolder().equals(chosenFolder)).findFirst().orElse(null);
    }

    /**
     * Reads the games again and shows them; the game chosen stays chosen, else the first one is.
     */
    void refresh() {
        games = CustomGames.list();
        if (chosenGame() == null) {
            chosenFolder = games.isEmpty() ? null : games.getFirst().getFolder();
        }
        cards.getChildren().clear();
        for (final CustomGame game : games) {
            cards.getChildren().add(card(game));
        }
        // As wide as the games seen; no arrows when all of them are seen
        final int seen = Math.min(games.size(), visibleGames);
        final double width = seen * (CARD_WIDTH + GAP) - GAP + 2 * MARGIN;
        final double height = CARD_HEIGHT + ACTIONS_HEIGHT + 2 * MARGIN;
        viewport.setMinSize(width, height);
        viewport.setPrefSize(width, height);
        viewport.setMaxSize(width, height);
        final boolean hasGames = !games.isEmpty();
        viewport.setVisible(hasGames);
        viewport.setManaged(hasGames);
        final boolean arrows = games.size() > visibleGames;
        for (final Button arrow : List.of(previousButton, nextButton)) {
            arrow.setVisible(arrows);
            arrow.setManaged(arrows);
        }
        showFrom(firstVisible, false);
        showTheChosenGame();
        showChosen();
    }

    private void showChosen() {
        for (int i = 0; i < games.size(); i++) {
            final Button choose = (Button) ((VBox) cards.getChildren().get(i)).getChildren().getFirst();
            choose.getStyleClass().remove("gp-variant-selected");
            if (games.get(i).getFolder().equals(chosenFolder)) {
                choose.getStyleClass().add("gp-variant-selected");
            }
        }
        playButton.setDisable(variant() == null);
    }

    /**
     * Chooses a game.
     */
    void choose(final CustomGame game) {
        chosenFolder = game.getFolder();
        showChosen();
    }

    /**
     * @return the card of a game: the pictures of its first screen, its name and its screens, chosen by a click, and its
     * buttons changing and deleting it
     */
    private VBox card(final CustomGame game) {
        final Label name = new Label(game.getName());
        // On one line, cut when long: a label on several lines makes the card twice as high
        name.setMaxWidth(CARD_WIDTH - 40);
        name.setAlignment(Pos.CENTER);
        final int playable = game.playableScreens().size();
        final int incomplete = game.getScreens().size() - playable;
        final Label screens = new Label(translator.translate("OwnGameScreens") + " " + playable
            + (incomplete == 0 ? "" : "\n" + translator.translate("OwnGameIncomplete") + " " + incomplete));
        screens.setTextAlignment(TextAlignment.CENTER);
        screens.getStyleClass().add("gp-subtitle");
        screens.setStyle("-fx-font-size: 11pt;");
        final VBox shown = new VBox(4, thumbnails(game), name, screens);
        shown.setAlignment(Pos.CENTER);
        final Button choose = new Button(game.getName(), shown);
        choose.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        choose.getStyleClass().add("gp-variant");
        choose.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        choose.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        choose.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        choose.setOnAction(event -> choose(game));

        final Button edit = new Button(translator.translate("OwnGameEdit"));
        edit.getStyleClass().addAll("gp-button", "gp-button-secondary");
        edit.setStyle("-fx-padding: 6 14 6 14; -fx-font-size: 12pt;");
        edit.setOnAction(event -> {
            choose(game);
            final CustomGame fresh = CustomGames.load(game.getFolder());
            if (fresh != null) {
                edit(fresh);
            }
        });
        // A red disc: it deletes a game (maintainer's request of 25/11/2026)
        final Button delete = MenuStyle.iconButton(MenuIcon.DELETE, translator.translate("Delete"), () -> delete(game));
        delete.getStyleClass().addAll("gp-round", "gp-round-danger");
        delete.setStyle("-fx-padding: 5;");
        final HBox actions = new HBox(10, edit, delete);
        actions.setAlignment(Pos.CENTER);
        final VBox card = new VBox(8, choose, actions);
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    private Region thumbnails(final CustomGame game) {
        final GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        grid.setMinHeight(THUMBNAILS_HEIGHT);
        if (!game.getScreens().isEmpty()) {
            final CustomScreen screen = game.getScreens().getFirst();
            final List<Path> pictures = screen.getPictures().stream()
                .filter(picture -> picture != null && Files.isRegularFile(picture)).limit(THUMBNAILS).toList();
            final int lines = pictures.size() > 2 ? 2 : 1;
            final double size = (THUMBNAILS_HEIGHT - (lines - 1) * 6) / lines;
            for (int i = 0; i < pictures.size(); i++) {
                grid.add(thumbnail(pictures.get(i), Math.min(size, (CARD_WIDTH - 50) / 2)), i % 2, i / 2);
            }
        }
        return grid;
    }

    /**
     * @return a picture on a white square (a transparent picture is seen in both themes)
     */
    static StackPane thumbnail(final Path picture, final double size) {
        final ImageView view = new ImageView(new Image(picture.toUri().toString(), size - 8, size - 8, true, true, true));
        final StackPane square = new StackPane(view);
        square.setMinSize(size, size);
        square.setMaxSize(size, size);
        square.setStyle("-fx-background-color: " + GameStyle.PICTURE_BACKGROUND
            + "; -fx-background-radius: 12; -fx-border-color: -gp-border; -fx-border-radius: 12;");
        return square;
    }

    /**
     * Opens the window making or changing a game; the games are shown again when it closes.
     */
    private void edit(final CustomGame game) {
        final Parent ownRoot = getScene().getRoot();
        ownRoot.setEffect(new BoxBlur());
        ownRoot.setDisable(true);
        final WhereIsItEditorDialog editor = new WhereIsItEditorDialog(this, translator, game);
        editor.setOnHidden(event -> {
            ownRoot.setEffect(null);
            ownRoot.setDisable(false);
            if (game.getFolder() != null) {
                chosenFolder = game.getFolder();
            }
            refresh();
        });
        editor.show();
        editor.toFront();
    }

    private void delete(final CustomGame game) {
        if (!MenuMessageDialog.confirmDeletion(this, game.getName(), translator.translate("OwnGameDeleteQuestion"),
            translator.translate("Delete"), translator.translate("Cancel"), translator.translate("Close"))) {
            return;
        }
        try {
            CustomGames.delete(game);
        } catch (final IOException e) {
            log.warn("Cannot delete the game {}", game.getFolder(), e);
        }
        refresh();
    }
}

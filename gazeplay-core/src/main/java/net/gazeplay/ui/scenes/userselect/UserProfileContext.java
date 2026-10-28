package net.gazeplay.ui.scenes.userselect;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AppMode;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationSource;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FileUtils;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.ui.menus.MenuButtons;
import net.gazeplay.ui.menus.MenuDialog;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The user selection page, after the choice of the mode: one card per profile (the default one, and those of the
 * "profiles" folder), to choose, create, edit (name and picture) or delete a profile. Each profile has its own
 * settings, results and files.
 * <p>
 * Look of the menus (menus.css): change of mode and quit at the top right; in the middle the logo, the chosen mode, the
 * question, then the cards, each with a round picture (or a colored disc with an icon) and the name. Creating, editing
 * and deleting are done in separate windows, as before (decision of the maintainer).
 */
@Slf4j
public class UserProfileContext extends GraphicalContext<BorderPane> {

    /**
     * Colors of the profiles without picture, chosen from their name: the same profile keeps its color. Not the accent
     * color, that of the default profile.
     */
    private static final String[] AVATAR_COLORS = {"gp-avatar-teal", "gp-avatar-violet", "gp-avatar-sky"};

    private final double avatarSize;

    public UserProfileContext(final GazePlay gazePlay) {
        super(gazePlay, new BorderPane());

        final GamePanelDimensionProvider gamePanelDimensionProvider = new GamePanelDimensionProvider(() -> root, gazePlay::getPrimaryScene);

        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        // As the former cards (an eighth of the width, a quarter of the height), as a circle
        avatarSize = Math.min(gamePanelDimensionProvider.getDimension2D().getWidth() / 8,
            gamePanelDimensionProvider.getDimension2D().getHeight() / 4) * 0.8;

        gazeplayUserProfileContext(gazePlay, screenDimension);
    }

    /**
     * Builds the page.
     */
    public void gazeplayUserProfileContext(GazePlay gazePlay, Dimension2D screenDimension) {
        MenuStyle.applyTo(root);
        final Translator translator = gazePlay.getTranslator();

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final Button exitButton = MenuStyle.quitButton(translator.translate("Exit"), gazePlay::quit);
        final HBox topBar = new HBox(spacer, createChangeModeButton(gazePlay),
            MenuButtons.themeButton(gazePlay, translator), exitButton);
        topBar.getStyleClass().add("gp-top-bar");

        final Node logo = LogoFactory.getInstance().createLogoStatic(gazePlay.getPrimaryStage());
        // The mode chosen before (games or evaluations), under the logo
        final AppMode mode = gazePlay.getMode() == null ? AppMode.GAMES : gazePlay.getMode();
        final Label prompt = MenuStyle.title(translator.translate("ProfileChoose"));
        final VBox header = new VBox(16, logo, createModeChip(translator, mode), prompt);
        header.setAlignment(Pos.CENTER);

        final ScrollPane userPickerChoicePane = createUserPickerChoicePane(gazePlay);
        VBox.setVgrow(userPickerChoicePane, Priority.ALWAYS);

        final VBox center = new VBox(16, header, userPickerChoicePane);
        center.setAlignment(Pos.TOP_CENTER);

        root.setTop(topBar);
        root.setCenter(center);
    }

    /**
     * @return the reminder of the chosen mode: its icon and name, in its color
     */
    private static HBox createModeChip(final Translator translator, final AppMode mode) {
        final MenuIcon icon = mode == AppMode.EVALUATIONS ? MenuIcon.EVALUATIONS : MenuIcon.GAMES;
        final HBox chip = new HBox(icon.create(24), new Label(translator.translate(mode.getTranslationKey())));
        chip.getStyleClass().addAll("gp-mode-chip",
            mode == AppMode.EVALUATIONS ? "gp-mode-evaluations" : "gp-mode-games");
        chip.setMaxWidth(Region.USE_PREF_SIZE);
        return chip;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    /**
     * @return the names of the user profiles: the folders of the "profiles" folder of the GazePlay folder, sorted
     */
    public static List<String> findAllUsersProfiles() {
        final File profilesDirectory = GazePlayDirectories.getProfilesDirectory();
        log.info("profilesDirectory = {}", profilesDirectory);

        final File[] directoryContent = profilesDirectory.listFiles();
        if (directoryContent == null) {
            return Collections.emptyList();
        }

        final List<String> listOfUsers = Arrays.stream(directoryContent)
            .filter(f -> !f.isHidden())
            .filter(File::isDirectory)
            .map(File::getName)
            .sorted()
            .collect(Collectors.toList());
        log.info("FULL LIST OF GAZEPLAY USERS: " + listOfUsers);
        return listOfUsers;
    }

    /**
     * @return the scrolling pane of the cards of the profiles, with the card creating a new one
     */
    ScrollPane createUserPickerChoicePane(final GazePlay gazePlay) {
        final int flowPaneGap = 40;
        final FlowPane choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.TOP_CENTER);
        choicePanel.setHgap(flowPaneGap);
        choicePanel.setVgap(flowPaneGap);
        choicePanel.setRowValignment(VPos.TOP);
        choicePanel.setPadding(new Insets(24, 60, 48, 60));

        final ScrollPane choicePanelScrollPane = new ScrollPane(choicePanel);
        choicePanelScrollPane.setFitToWidth(true);
        choicePanelScrollPane.setFitToHeight(true);
        choicePanelScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        final List<String> allUsersProfiles = findAllUsersProfiles();

        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        // No picture: a disc with the icon of the default profile
        final HBox configUserCard = createUser(
            gazePlay,
            choicePanel,
            getGazePlay().getTranslator().translate("DefaultUser"),
            null,
            false,
            false,
            screenDimension
        );

        choicePanel.getChildren().add(configUserCard);

        for (final String currentUserProfile : allUsersProfiles) {
            log.info("Profile found : {}", currentUserProfile);
            final Configuration currentUserProfileConfiguration = ConfigurationSource.createFromProfile(currentUserProfile);
            final ImagePattern imagePattern = lookupProfilePicture(currentUserProfileConfiguration);
            final HBox userCard = createUser(
                gazePlay,
                choicePanel,
                currentUserProfile,
                imagePattern,
                true,
                false,
                screenDimension
            );

            choicePanel.getChildren().add(userCard);
        }

        final HBox newUserCard = createUser(
            gazePlay,
            choicePanel,
            getGazePlay().getTranslator().translate("AddUser"),
            null,
            false,
            true,
            screenDimension
        );
        choicePanel.getChildren().add(newUserCard);

        return choicePanelScrollPane;
    }

    /**
     * @return the picture of a profile, null when it has none or it cannot be read (the card then shows a colored disc)
     */
    static ImagePattern lookupProfilePicture(final Configuration currentUserProfileConfiguration) {
        ImagePattern imagePattern = null;
        final String userPicture = currentUserProfileConfiguration.getUserPicture();

        // A profile without picture has an empty value: since Java 25, new File("") is the current folder and exists,
        // and opening it as an image failed (AccessDeniedException on Windows)
        if (userPicture != null && !userPicture.isBlank()) {
            final File userPictureFile = new File(userPicture);
            if (userPictureFile.isFile()) {
                try (InputStream is = Files.newInputStream(userPictureFile.toPath())) {
                    imagePattern = new ImagePattern(new Image(is));
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return imagePattern;
    }

    /**
     * @param imagePattern the picture of the profile, null for none: the card then shows a colored disc with an icon
     *                     (the default profile, a new profile, or a profile without picture)
     * @return the card of a profile; choosing it (click, Enter or Space) makes it the active profile and shows the menu
     */
    User createUser(
        @NonNull final GazePlay gazePlay,
        @NonNull final FlowPane choicePanel,
        @NonNull final String userProfileName,
        final ImagePattern imagePattern,
        final boolean editable,
        final boolean newUser,
        final Dimension2D screenDimension
    ) {
        if (userProfileName.trim().isEmpty()) {
            throw new IllegalArgumentException("userProfileName should not be empty");
        }

        final User user = new User(userProfileName);

        final StackPane avatar = createAvatar(userProfileName, imagePattern, editable, newUser);

        // Centered, on two lines at most (menus.css), of the same width on every card
        final double nameWidth = Math.max(avatarSize + 24, 180);
        final Label userNameLabel = new Label(user.getName());
        userNameLabel.getStyleClass().add("gp-profile-name");
        userNameLabel.setWrapText(true);
        userNameLabel.setTextAlignment(TextAlignment.CENTER);
        userNameLabel.setAlignment(Pos.TOP_CENTER);
        userNameLabel.setMinWidth(nameWidth);
        userNameLabel.setPrefWidth(nameWidth);
        userNameLabel.setMaxWidth(nameWidth);
        BorderPane.setAlignment(userNameLabel, Pos.CENTER);

        final BorderPane content = new BorderPane();
        content.getStyleClass().addAll("gp-card", "gp-profile-card");
        content.setCenter(avatar);
        content.setBottom(userNameLabel);
        content.setFocusTraversable(true);

        // No tooltip on the card: the name is written on it (maintainer's feedback of 28/10/2026)

        user.setAlignment(Pos.TOP_LEFT);
        user.setSpacing(8);
        user.getChildren().add(content);

        if (editable) {
            final Button editUserButton = createEditUserButton(getGazePlay(), choicePanel, user, screenDimension);
            final Button deleteUserButton = createDeleteUserButton(getGazePlay(), choicePanel, user);
            final VBox buttonBox = new VBox(8);

            buttonBox.getChildren().addAll(editUserButton, deleteUserButton);
            user.getChildren().add(buttonBox);
        }

        final EventHandler<Event> mouseClickedEventHandler;
        if (newUser) {
            mouseClickedEventHandler = event -> {
                log.info("Adding user");
                root.setEffect(new BoxBlur());
                final Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user, true, screenDimension);

                final String dialogTitle = getGazePlay().getTranslator().translate("NewUser");
                dialog.setTitle(dialogTitle);
                dialog.show();

                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            };
        } else {
            mouseClickedEventHandler = event -> {
                if (!user.getName().equals(getGazePlay().getTranslator().translate("DefaultUser"))) {
                    ActiveConfigurationContext.switchToUser(user.getName());
                }
                gazePlay.getTranslator().notifyLanguageChanged();

                final Configuration config = ActiveConfigurationContext.getInstance();
                CssUtil.setPreferredStylesheets(config, gazePlay.getPrimaryScene(), gazePlay.getCurrentScreenDimensionSupplier());

                BackgroundMusicManager.onConfigurationChanged();

                choicePanel.getChildren().clear();
                gazePlay.onReturnToMenu();
            };
        }

        content.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
        content.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                mouseClickedEventHandler.handle(event);
            }
        });
        return user;
    }

    /**
     * @return the round picture of a profile, or, without picture, a colored disc with an icon: a gear for the default
     * profile, a plus for a new one, a person (color from the name) for the others. The circle is a Rectangle with
     * rounded corners, so that {@link #modifyUser} can give it a picture later.
     */
    private StackPane createAvatar(final String name, final ImagePattern imagePattern, final boolean editable,
                                   final boolean newUser) {
        final Rectangle picture = new Rectangle(avatarSize, avatarSize);
        picture.setArcWidth(avatarSize);
        picture.setArcHeight(avatarSize);
        picture.getStyleClass().add("gp-avatar-shape");

        final StackPane avatar = new StackPane(picture);
        avatar.getStyleClass().add("gp-avatar");
        if (imagePattern != null) {
            picture.setFill(imagePattern);
        } else {
            final MenuIcon icon;
            final String color;
            if (newUser) {
                icon = MenuIcon.ADD;
                color = "gp-avatar-new";
            } else if (!editable) {
                icon = MenuIcon.MANAGE_ACCOUNTS;
                color = "gp-avatar-accent";
            } else {
                icon = MenuIcon.PERSON;
                color = AVATAR_COLORS[Math.floorMod(name.hashCode(), AVATAR_COLORS.length)];
            }
            picture.getStyleClass().add(color);
            avatar.getStyleClass().add(color);
            avatar.getChildren().add(icon.create(avatarSize * 0.55));
        }
        return avatar;
    }

    /**
     * @return the button deleting a profile, after confirmation
     */
    private Button createDeleteUserButton(
        final GazePlay gazePlay,
        final FlowPane choicePanel,
        final User user
    ) {
        final Button button = MenuStyle.iconButton(MenuIcon.DELETE, getGazePlay().getTranslator().translate("Remove"),
            () -> {
                final Stage dialog = createRemoveDialog(gazePlay.getPrimaryStage(), choicePanel, user, gazePlay.getCurrentScreenDimensionSupplier());

                final String dialogTitle = getGazePlay().getTranslator().translate("Remove");
                dialog.setTitle(dialogTitle);

                dialog.toFront();
                dialog.setAlwaysOnTop(true);

                dialog.show();
            });
        button.getStyleClass().addAll("gp-profile-action", "gp-danger");
        return button;
    }

    /**
     * @return the button editing the name and picture of a profile
     */
    private Button createEditUserButton(final GazePlay gazePlay, final FlowPane choicePanel, final User user, final Dimension2D screenDimension) {
        final Button button = MenuStyle.iconButton(MenuIcon.EDIT, getGazePlay().getTranslator().translate("UserModif"),
            () -> {
                final Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user, false, screenDimension);

                final String dialogTitle = getGazePlay().getTranslator().translate("UserModif");
                dialog.setTitle(dialogTitle);

                dialog.toFront();
                dialog.setAlwaysOnTop(true);

                dialog.show();
            });
        button.getStyleClass().add("gp-profile-action");
        return button;
    }

    /**
     * @return the button going back to the choice of the mode (games or evaluations)
     */
    private static Button createChangeModeButton(final GazePlay gazePlay) {
        final Button button = new Button(gazePlay.getTranslator().translate("ModeChange"), MenuIcon.SWAP.create(24));
        button.getStyleClass().addAll("gp-button", "gp-button-secondary");
        button.setOnAction(e -> gazePlay.goToModePage());
        return button;
    }

    /**
     * @return an empty modal window over the page, without the frame of the system (see {@link MenuDialog})
     */
    private Stage initializeDialog(Stage primaryStage) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        MenuDialog.undecorate(dialog);
        dialog.setOnCloseRequest(windowEvent -> primaryStage.getScene().getRoot().setEffect(null));
        return dialog;
    }

    /**
     * @return the scene of a window of the page: its content in a card with the title of the window and a close
     * button; closing it removes the blur of the page
     */
    private Scene createDialogScene(final Stage dialog, final Stage primaryStage, final ScrollPane content,
                                    final Supplier<Dimension2D> currentScreenDimensionSupplier) {
        final Scene scene = MenuDialog.createScene(dialog, content, getGazePlay().getTranslator().translate("Close"),
            () -> {
                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            });
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), scene, currentScreenDimensionSupplier);
        return scene;
    }

    /**
     * @return a scrolling pane of a window of the page, a third of the width of the window wide and as high as its
     * content
     */
    private ScrollPane initializeScroller(Pane choicePane, Stage primaryStage) {
        choicePane.setPadding(new Insets(24, 32, 32, 32));
        final ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        return choicePanelScroller;
    }

    /**
     * @return the window confirming the deletion of a profile; confirming deletes its folder with all its content
     */
    Stage createRemoveDialog(
        final Stage primaryStage,
        final FlowPane choicePanel,
        final User user,
        final Supplier<Dimension2D> currentScreenDimensionSupplier
    ) {
        final Stage dialog = initializeDialog(primaryStage);

        final HBox choicePane = new HBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        final ScrollPane choicePanelScroller = initializeScroller(choicePane, primaryStage);

        final Button yes = createDialogButton(getGazePlay().getTranslator().translate("YesRemove"), "gp-button-danger");
        yes.setOnMouseClicked(event -> {
            dialog.close();
            choicePanel.getChildren().remove(user);
            final File userDirectory = GazePlayDirectories.getUserProfileDirectory(user.getName());
            FileUtils.deleteDirectoryRecursively(userDirectory);
            log.info("Profile: " + user.getName() + " deleted");
        });

        final Button no = createDialogButton(getGazePlay().getTranslator().translate("NoCancel"),
            "gp-button-secondary");
        no.setOnMouseClicked(event -> dialog.close());

        choicePane.getChildren().add(yes);
        choicePane.getChildren().add(no);

        dialog.setScene(createDialogScene(dialog, primaryStage, choicePanelScroller, currentScreenDimensionSupplier));
        return dialog;
    }

    /**
     * @return the window creating a profile, or editing one, with its name and picture
     */
    Stage createDialog(
        final GazePlay gazePlay,
        final Stage primaryStage,
        final FlowPane choicePanel,
        final User user,
        final boolean newUser,
        final Dimension2D screenDimension
    ) {
        final Stage dialog = initializeDialog(primaryStage);

        final VBox choicePane = new VBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        final ScrollPane choicePanelScroller = initializeScroller(choicePane, primaryStage);

        final HBox nameField = new HBox(12);

        final TextField tf = new TextField();
        Utils.addTextLimiter(tf, 32);

        if (newUser) {
            nameField.setAlignment(Pos.CENTER);

            final Label t = new Label(getGazePlay().getTranslator().translate("Name"));
            tf.setPromptText(getGazePlay().getTranslator().translate("enterName"));
            tf.setPrefColumnCount(16);

            nameField.getChildren().addAll(t, tf);
        }

        final HBox imageField = new HBox(12);
        imageField.setAlignment(Pos.CENTER);

        final Label ti = new Label(getGazePlay().getTranslator().translate("Image"));

        final Button chooseImageButton = createDialogButton(getGazePlay().getTranslator().translate("ChooseImage"),
            "gp-button-secondary");
        chooseImageButton.setOnMouseClicked(event -> {
            String s = null;
            try {
                s = getImage(dialog, chooseImageButton);
            } catch (IOException e) {
                log.warn("Cannot load the image of the profile", e);
            }
            if (s != null) {
                chooseImageButton.setText(s);
            }
        });

        final Button reset = createDialogButton(getGazePlay().getTranslator().translate("Reset"),
            "gp-button-secondary");
        reset.setOnMouseClicked(event -> {
            chooseImageButton.setGraphic(null);
            chooseImageButton.setText(getGazePlay().getTranslator().translate("ChooseImage"));
        });

        imageField.getChildren().addAll(ti, chooseImageButton, reset);

        final ImageView iv = new ImageView();
        choicePane.getChildren().addAll(imageField, nameField, iv);

        final Button okButton = createDialogButton("Ok", "gp-button-primary");
        okButton.setDefaultButton(true);
        okButton.setMinWidth(160);
        choicePane.getChildren().add(okButton);

        final EventHandler<Event> event;

        if (newUser) {
            event = mouseEvent -> {
                // Without picture, the card shows a colored disc
                ImagePattern imagePattern = null;
                if (chooseImageButton.getGraphic() != null) {
                    imagePattern = new ImagePattern(((ImageView) chooseImageButton.getGraphic()).getImage());
                }

                final User newUser1 = createUser(gazePlay, choicePanel, tf.getText(), imagePattern, true, false, screenDimension);

                if (checkNewName(newUser1.getName())) {
                    choicePanel.getChildren().remove(user);
                    choicePanel.getChildren().add(newUser1);
                    choicePanel.getChildren().add(user);

                    ActiveConfigurationContext.switchToUser(newUser1.getName());
                    final Configuration conf = ActiveConfigurationContext.getInstance();
                    final File userDirectory = GazePlayDirectories.getUserProfileDirectory(newUser1.getName());
                    final boolean userDirectoryCreated = userDirectory.mkdirs();
                    log.debug("userDirectoryCreated = {}", userDirectoryCreated);

                    conf.setUserName(newUser1.getName());

                    log.info("THE NAME OF THE NEW USER IS = {}", conf.getUserName());

                    if (!chooseImageButton.getText().equals(getGazePlay().getTranslator().translate("ChooseImage"))) {
                        final File src = new File(chooseImageButton.getText());
                        final File dst = new File(GazePlayDirectories.getUserProfileDirectory(newUser1.getName()), src.getName());
                        copyFile(src, dst);

                        conf.setUserPicture(dst.getAbsolutePath());
                    }

                    conf.setFileDir(GazePlayDirectories.getFileDirectoryUserValue(newUser1.getName()).getAbsolutePath());

                    dialog.close();
                    primaryStage.getScene().getRoot().setEffect(null);
                } else {
                    final Text error = new Text(getGazePlay().getTranslator().translate("AlreadyUsed"));
                    error.getStyleClass().add("gp-error");
                    choicePane.getChildren().add(error);
                }
            };
        } else {
            event = mouseEvent -> {
                ImagePattern ip = null;
                if (chooseImageButton.getGraphic() != null) {
                    ip = new ImagePattern(((ImageView) chooseImageButton.getGraphic()).getImage());
                }
                modifyUser(user, user.getName(), ip);

                final Configuration conf = ConfigurationSource.createFromProfile(user.getName());

                if (!chooseImageButton.getText().equals(getGazePlay().getTranslator().translate("ChooseImage"))) {
                    final File src = new File(chooseImageButton.getText());
                    final File dst = new File(GazePlayDirectories.getUserProfileDirectory(user.getName()), src.getName());
                    copyFile(src, dst);

                    conf.setUserPicture(dst.getAbsolutePath());
                }

                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            };
        }
        okButton.setOnMouseClicked(event);

        dialog.setScene(createDialogScene(dialog, primaryStage, choicePanelScroller,
            gazePlay.getCurrentScreenDimensionSupplier()));

        return dialog;
    }

    /**
     * @param variant gp-button-primary (the action), gp-button-secondary or gp-button-danger (menus.css)
     * @return a button of a window of the page
     */
    private static Button createDialogButton(String text, String variant) {
        final Button button = new Button(text);
        button.getStyleClass().addAll("gp-button", variant);
        return button;
    }

    /**
     * @return whether a name is free for a new profile
     */
    private boolean checkNewName(final String s) {
        final boolean isNew = !findAllUsersProfiles().contains(s);
        return (isNew && !s.equals(getGazePlay().getTranslator().translate("DefaultUser")));
    }

    /**
     * Updates the card of a profile after its edition.
     */
    private void modifyUser(final HBox user, final String name, final ImagePattern ip) {
        final BorderPane c = (BorderPane) user.getChildren().get(0);
        final StackPane avatar = (StackPane) c.getCenter();
        final Rectangle r = (Rectangle) avatar.getChildren().get(0);
        if (ip != null) {
            // The picture replaces the colored disc and its icon; the color class would override the fill
            r.getStyleClass().retainAll("gp-avatar-shape");
            avatar.getStyleClass().retainAll("gp-avatar");
            avatar.getChildren().retainAll(r);
            r.setFill(ip);
        }
        if ((name != null) && (!name.isBlank())) {
            ((Label) c.getBottom()).setText(name);
        }
    }

    /**
     * @return the content type of a file as recognised by the system, e.g. "image/png"
     */
    public String getContentType(File file) throws IOException {
        return Files.probeContentType(Path.of(file.getAbsolutePath()));
    }

    /**
     * @return the image file chosen by the user, null when cancelled
     */
    private File chooseImageFile(final Stage primaryStage) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff"),
            new ExtensionFilter("PNG Files", "*.png"),
            new ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
            new ExtensionFilter("GIF Files", "*.gif"),
            new ExtensionFilter("BMP Files", "*.bmp"),
            new ExtensionFilter("TIFF Files", "*.tiff"));
        return fileChooser.showOpenDialog(primaryStage);
    }

    /**
     * Asks for a picture and shows it on the button.
     *
     * @return the path of the picture, null when the chosen file is not an image
     */
    private String getImage(final Stage primaryStage, final Button targetButton) throws IOException {
        final File selectedImageFile = chooseImageFile(primaryStage);
        if (selectedImageFile == null) {
            // Choice cancelled
            return null;
        }
        String typeImage = getContentType(selectedImageFile);
        if (typeImage == null || !typeImage.contains("image/")) {
            return null;
        }

        final String result = selectedImageFile.getAbsolutePath();
        try {
            final ImageView imageView = new ImageView(new Image(Files.newInputStream(selectedImageFile.toPath())));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(primaryStage.getHeight() / 10);
            targetButton.setGraphic(imageView);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Copies a file; a failure is logged.
     */
    private static void copyFile(final File source, final File dest) {
        try {
            org.apache.commons.io.FileUtils.copyFile(source, dest);
        } catch (final Exception e) {
            log.info("Unable to copy the profile picture");
        }
    }
}

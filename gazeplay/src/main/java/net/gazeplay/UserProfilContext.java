package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationSource;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.FilesUtility;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class UserProfilContext extends GraphicalContext<BorderPane> {

    public static UserProfilContext newInstance(final GazePlay gazePlay, final Configuration config) {
        BorderPane root = new BorderPane();

        GamePanelDimensionProvider gamePanelDimensionProvider = new GamePanelDimensionProvider(root,
            gazePlay.getPrimaryScene());

        return new UserProfilContext(gazePlay, root, gamePanelDimensionProvider, config);
    }

    private static List<String> findAllUsersProfiles() {
        File profilesDirectory = GazePlayDirectories.getProfilesDirectory();
        log.info("profilesDirectory = {}", profilesDirectory);
        //
        File[] directoryContent = profilesDirectory.listFiles();
        if (directoryContent == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(directoryContent)
            .filter(f -> !f.getName().startsWith("."))
            .filter(File::isDirectory)
            .map(File::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    @Setter
    @Getter
    private GazeDeviceManager gazeDeviceManager;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    private final double cardHeight;

    private final double cardWidth;

    public UserProfilContext(
        GazePlay gazePlay,
        BorderPane root,
        GamePanelDimensionProvider gamePanelDimensionProvider,
        Configuration config
    ) {
        super(gazePlay, root);

        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
        cardHeight = gamePanelDimensionProvider.getDimension2D().getHeight() / 4;
        cardWidth = gamePanelDimensionProvider.getDimension2D().getWidth() / 8;

        Node logo = LogoFactory.getInstance().createLogoAnimated(gazePlay.getPrimaryStage());

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        CustomButton exitButton = createExitButton();
        topRightPane.getChildren().addAll(exitButton);

        Node userPickerChoicePane = createuUserPickerChoicePane(gazePlay);

        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        centerCenterPane.getChildren().add(userPickerChoicePane);

        BorderPane topPane = new BorderPane();
        topPane.setCenter(logo);
        topPane.setRight(topRightPane);

        root.setTop(topPane);
        root.setCenter(centerCenterPane);

        root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
            + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
            + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    private ScrollPane createuUserPickerChoicePane(GazePlay gazePlay) {
        final int flowpaneGap = 40;
        FlowPane choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(flowpaneGap);
        choicePanel.setVgap(flowpaneGap);
        choicePanel.setPadding(new Insets(20, 60, 20, 60));

        ScrollPane choicePanelScroller = new ScrollPane(choicePanel);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        final List<String> allUsersProfiles = findAllUsersProfiles();

        HBox configUserCard = createUser(gazePlay, choicePanel, getGazePlay().getTranslator().translate("DefaultUser"), new ImagePattern(new Image("data/common/images/ConfigUser.png")), false, false);
        choicePanel.getChildren().add(configUserCard);

        for (final String currentUserProfile : allUsersProfiles) {
            log.info("Profile founded : {}", currentUserProfile);
            Configuration currentUserProfileConfiguration = ConfigurationSource.createFromProfile(currentUserProfile);
            ImagePattern imagePattern = lookupForProfilePicture(currentUserProfileConfiguration);
            HBox userCard = createUser(gazePlay, choicePanel, currentUserProfile, imagePattern, true, false);
            choicePanel.getChildren().add(userCard);
        }

        ActiveConfigurationContext.setInstance(ConfigurationSource.createFromDefaultProfile());

        HBox newUserCard = createUser(gazePlay, choicePanel, getGazePlay().getTranslator().translate("AddUser"), new ImagePattern(new Image("data/common/images/AddUser.png")), false, true);
        choicePanel.getChildren().add(newUserCard);

        return choicePanelScroller;
    }

    private static ImagePattern lookupForProfilePicture(Configuration currentUserProfileConfiguration) {
        ImagePattern imagePattern = null;
        final String userPicture = currentUserProfileConfiguration.getUserPicture();
        if (userPicture != null) {
            final File userPictureFile = new File(userPicture);
            if (userPictureFile.exists()) {
                try (InputStream is = new FileInputStream(userPictureFile)) {
                    imagePattern = new ImagePattern(new Image(is));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (imagePattern == null) {
            imagePattern = new ImagePattern(new Image("data/common/images/DefaultUser.png"));
        }
        return imagePattern;
    }

    private User createUser(
        @NonNull final GazePlay gazePlay,
        @NonNull final FlowPane choicePanel,
        @NonNull final String userProfileName,
        @NonNull final ImagePattern imagePattern,
        final boolean editable,
        final boolean newUser
    ) {
        if (userProfileName.trim().isEmpty()) {
            throw new IllegalArgumentException("userProfileName should not be empty");
        }

        final User user = new User(userProfileName);

        Rectangle pictureRectangle = new Rectangle(0, 0, cardWidth, cardHeight);
        pictureRectangle.setFill(imagePattern);

        Text userNameText = new Text(user.getName());
        userNameText.setFill(Color.WHITE);
        userNameText.getStyleClass().add("gameChooserButtonTitle");
        BorderPane.setAlignment(userNameText, Pos.BOTTOM_CENTER);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("gameChooserButton");
        content.getStyleClass().add("button");
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setCenter(pictureRectangle);
        content.setBottom(userNameText);

        user.setAlignment(Pos.TOP_RIGHT);
        user.getChildren().add(content);

        if (editable) {
            BorderPane editUserButton = createEditUserButton(getGazePlay(), choicePanel, user);
            BorderPane deleteUserButton = createDeleteUserButton(getGazePlay(), choicePanel, user);
            VBox buttonBox = new VBox();
            // buttonBox.setSpacing(10);
            buttonBox.getChildren().addAll(editUserButton, deleteUserButton);
            user.getChildren().add(buttonBox);
        }

        final EventHandler<Event> mouseClickedEventHandler;
        if (newUser) {
            mouseClickedEventHandler = event -> {
                log.info("Adding user");
                root.setEffect(new BoxBlur());
                Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user, true);

                String dialogTitle = getGazePlay().getTranslator().translate("NewUser");
                dialog.setTitle(dialogTitle);
                dialog.show();

                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            };
        } else {
            mouseClickedEventHandler = event -> {
                if (!user.getName().equals(getGazePlay().getTranslator().translate("DefaultUser"))) {
                    ActiveConfigurationContext.setInstance(ConfigurationSource.createFromProfile(user.getName()));
                }
                gazePlay.getTranslator().notifyLanguageChanged();

                Configuration config = ActiveConfigurationContext.getInstance();
                CssUtil.setPreferredStylesheets(config, gazePlay.getPrimaryScene());

                BackgroundMusicManager.getInstance().stop();

                BackgroundMusicManager.setInstance(new BackgroundMusicManager());

                gazePlay.getHomeMenuScreen().clear();

                gazePlay.setHomeMenuScreen(HomeMenuScreen.newInstance(getGazePlay(), config));

                choicePanel.getChildren().clear();
                gazePlay.getHomeMenuScreen().setUpOnStage(gazePlay.getPrimaryScene());
            };
        }

        content.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
        return user;
    }

    private BorderPane createDeleteUserButton(GazePlay gazePlay, FlowPane choicePanel, User user) {
        final double size = Screen.getPrimary().getBounds().getWidth() / 50;
        CustomButton button = new CustomButton("data/common/images/error.png", size);

        button.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
            Stage dialog = createRemoveDialog(gazePlay.getPrimaryStage(), choicePanel, user);

            String dialogTitle = getGazePlay().getTranslator().translate("Remove");
            dialog.setTitle(dialogTitle);

            dialog.toFront();
            dialog.setAlwaysOnTop(true);

            dialog.show();
        });

        BorderPane rbp = new BorderPane();
        rbp.getStyleClass().add("gameChooserButton");
        rbp.getStyleClass().add("button");
        rbp.setCenter(button);
        rbp.maxWidthProperty().bind(button.widthProperty());
        rbp.maxHeightProperty().bind(button.heightProperty());
        rbp.minWidthProperty().bind(button.widthProperty());
        rbp.minHeightProperty().bind(button.heightProperty());

        return rbp;
    }

    private BorderPane createEditUserButton(GazePlay gazePlay, FlowPane choicePanel, User user) {
        double size = Screen.getPrimary().getBounds().getWidth() / 50;
        CustomButton button = new CustomButton("data/common/images/configuration-button-alt3.png", size);

        button.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
            Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user, false);

            String dialogTitle = getGazePlay().getTranslator().translate("UserModif");
            dialog.setTitle(dialogTitle);

            dialog.toFront();
            dialog.setAlwaysOnTop(true);

            dialog.show();
        });

        BorderPane rbp = new BorderPane();
        rbp.getStyleClass().add("gameChooserButton");
        rbp.getStyleClass().add("button");
        rbp.setCenter(button);
        rbp.maxWidthProperty().bind(button.widthProperty());
        rbp.maxHeightProperty().bind(button.heightProperty());
        rbp.minWidthProperty().bind(button.widthProperty());
        rbp.minHeightProperty().bind(button.heightProperty());

        return rbp;
    }

    private CustomButton createExitButton() {
        CustomButton exitButton = new CustomButton("data/common/images/power-off.png");
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> System.exit(0));
        return exitButton;
    }

    private Stage createRemoveDialog(Stage primaryStage, FlowPane choicePanel, User user) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> primaryStage.getScene().getRoot().setEffect(null));


        Button yes = new Button(getGazePlay().getTranslator().translate("YesRemove"));
        yes.getStyleClass().add("gameChooserButton");
        yes.getStyleClass().add("gameVariation");
        yes.getStyleClass().add("button");
        yes.setMinHeight(primaryStage.getHeight() / 10);
        yes.setMinWidth(primaryStage.getWidth() / 10);
        yes.addEventFilter(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
            dialog.close();
            choicePanel.getChildren().remove(user);
            File userDirectory = GazePlayDirectories.getUserProfileDirectory(user.getName());
            FilesUtility.deleteDirectoryRecursivly(userDirectory);
            log.info("Profile: " + user.getName() + " deleted");
        });

        Button no = new Button(getGazePlay().getTranslator().translate("NoCancel"));
        no.getStyleClass().add("gameChooserButton");
        no.getStyleClass().add("gameVariation");
        no.getStyleClass().add("button");
        no.setMinHeight(primaryStage.getHeight() / 10);
        no.setMinWidth(primaryStage.getWidth() / 10);
        no.addEventFilter(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> dialog.close());

        HBox choicePane = new HBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        choicePane.getChildren().add(yes);
        choicePane.getChildren().add(no);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);
        Configuration config = ActiveConfigurationContext.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);
        dialog.setScene(scene);
        return dialog;
    }

    private Stage createDialog(GazePlay gazePlay, Stage primaryStage, FlowPane choicePanel, User user,
                               boolean newUser) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> primaryStage.getScene().getRoot().setEffect(null));

        VBox choicePane = new VBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        HBox nameField = new HBox();

        TextField tf = new TextField();

        if (newUser) {
            nameField.setAlignment(Pos.CENTER);

            Text t = new Text(getGazePlay().getTranslator().translate("Name"));
            t.setFill(Color.WHITE);
            tf.setPromptText(getGazePlay().getTranslator().translate("enterName"));
            tf.setMaxWidth(primaryStage.getWidth() / 10);

            nameField.getChildren().addAll(t, tf);
        }

        HBox imageField = new HBox();
        imageField.setAlignment(Pos.CENTER);

        Text ti = new Text(getGazePlay().getTranslator().translate("Image"));
        ti.setFill(Color.WHITE);

        final Button chooseImageButton = new Button(getGazePlay().getTranslator().translate("ChooseImage"));
        chooseImageButton.getStyleClass().add("gameChooserButton");
        chooseImageButton.getStyleClass().add("gameVariation");
        chooseImageButton.getStyleClass().add("button");
        chooseImageButton.setMinHeight(primaryStage.getHeight() / 20);
        chooseImageButton.setMinWidth(primaryStage.getWidth() / 10);
        chooseImageButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
            String s = getImage(dialog, chooseImageButton);
            if (s != null) {
                chooseImageButton.setText(s);
            }
        });

        final Button reset = new Button(getGazePlay().getTranslator().translate("reset"));
        reset.getStyleClass().add("gameChooserButton");
        reset.getStyleClass().add("gameVariation");
        reset.getStyleClass().add("button");
        reset.setMinHeight(primaryStage.getHeight() / 20);
        reset.setMinWidth(primaryStage.getWidth() / 20);
        reset.addEventFilter(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event1 -> {
            chooseImageButton.setGraphic(null);
            chooseImageButton.setText(getGazePlay().getTranslator().translate("ChooseImage"));
        });

        imageField.getChildren().addAll(ti, chooseImageButton, reset);

        ImageView iv = new ImageView();
        choicePane.getChildren().addAll(imageField, nameField, iv);

        Button button = new Button("Ok");
        button.getStyleClass().add("gameChooserButton");
        button.getStyleClass().add("gameVariation");
        button.getStyleClass().add("button");
        button.setMinHeight(primaryStage.getHeight() / 10);
        button.setMinWidth(primaryStage.getWidth() / 10);
        choicePane.getChildren().add(button);

        EventHandler<Event> event;

        if (newUser) {
            event = mouseEvent -> {
                ImagePattern imagePattern = null;
                if (chooseImageButton.getGraphic() != null) {
                    imagePattern = new ImagePattern(((ImageView) chooseImageButton.getGraphic()).getImage());
                }
                if (imagePattern == null) {
                    imagePattern = new ImagePattern(new Image("data/common/images/DefaultUser.png"));
                }

                final User newUser1 = createUser(gazePlay, choicePanel, tf.getText(), imagePattern, false, false);

                if (checkNewName(newUser1.getName())) {

                    choicePanel.getChildren().remove(user);
                    choicePanel.getChildren().add(newUser1);
                    choicePanel.getChildren().add(user);

                    ActiveConfigurationContext.setInstance(ConfigurationSource.createFromProfile(newUser1.getName()));
                    Configuration conf2 = ActiveConfigurationContext.getInstance();
                    File userDirectory = GazePlayDirectories.getUserProfileDirectory(newUser1.getName());
                    final boolean userDirectoryCreated = userDirectory.mkdirs();
                    log.debug("userDirectoryCreated = {}", userDirectoryCreated);

                    conf2.setUserName(newUser1.getName());

                    log.info("THE NAME OF THE NEW USER IS = {}", conf2.getUserName());

                    if (!chooseImageButton.getText().equals(getGazePlay().getTranslator().translate("ChooseImage"))) {

                        File src = new File(chooseImageButton.getText());
                        File dst = new File(GazePlayDirectories.getUserProfileDirectory(newUser1.getName()), src.getName());
                        copyFile(src, dst);

                        conf2.setUserPicture(dst.getAbsolutePath());
                    }

                    conf2.setFileDir(GazePlayDirectories.getFileDirectoryUserValue(newUser1.getName()).getAbsolutePath());

                    conf2.saveConfigIgnoringExceptions();

                    dialog.close();
                    primaryStage.getScene().getRoot().setEffect(null);

                } else {
                    Text error = new Text(getGazePlay().getTranslator().translate("AlreadyUsed"));
                    error.setFill(Color.RED);
                    choicePane.getChildren().add(error);
                }

            };
        } else {
            event = mouseEvent -> {
                ImagePattern ip = null;
                if (chooseImageButton.getGraphic() != null) {
                    ip = new ImagePattern(((ImageView) chooseImageButton.getGraphic()).getImage());
                }
                modifUser(user, choicePanel, gazePlay, user.getName(), ip);

                Configuration conf2 = ConfigurationSource.createFromProfile(user.getName());

                if (!chooseImageButton.getText().equals(getGazePlay().getTranslator().translate("ChooseImage"))) {

                    File src = new File(chooseImageButton.getText());
                    File dst = new File(GazePlayDirectories.getUserProfileDirectory(user.getName()), src.getName());
                    copyFile(src, dst);

                    conf2.setUserPicture(dst.getAbsolutePath());
                }
                conf2.saveConfigIgnoringExceptions();

                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            };
        }
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        Configuration config = ActiveConfigurationContext.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);

        return dialog;
    }

    private boolean checkNewName(String s) {
        boolean isNew = !findAllUsersProfiles().contains(s);
        return (isNew && !s.equals(getGazePlay().getTranslator().translate("DefaultUser")));
    }

    private void modifUser(HBox user, FlowPane choicePanel, GazePlay gazePlay, String name, ImagePattern ip) {
        BorderPane c = (BorderPane) user.getChildren().get(0);
        Rectangle r = (Rectangle) c.getCenter();
        if (ip != null) {
            r.setFill(ip);
        }
        if ((name != null) && (!name.equals(""))) {
            String userName = name;
            ((Text) c.getBottom()).setText(userName);
        }
    }

    private File chooseImageFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff"),
            new ExtensionFilter("PNG Files", "*.png"), new ExtensionFilter("JPeg Files", "*.jpg", "*.jpeg"),
            new ExtensionFilter("GIF Files", "*.gif"), new ExtensionFilter("BMP Files", "*.bmp"),
            new ExtensionFilter("TIFF Files", "*.tiff"));
        return fileChooser.showOpenDialog(primaryStage);
    }

    private String getImage(Stage primaryStage, Button targetButton) {
        File selectedImageFile = chooseImageFile(primaryStage);
        if (selectedImageFile == null) {
            return null;
        }
        String result = selectedImageFile.getAbsolutePath();
        try {
            ImageView imageView = new ImageView(new Image(new FileInputStream(selectedImageFile)));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(primaryStage.getHeight() / 10);
            targetButton.setGraphic(imageView);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static boolean copyFile(File source, File dest) {
        try {
            FileUtils.copyFile(source, dest);
            return true;
        } catch (Exception e) {
            log.info("Unable to copy the profile picture");
            return false;
        }
    }

}

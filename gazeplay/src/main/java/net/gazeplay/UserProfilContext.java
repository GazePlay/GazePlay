package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
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
import javafx.util.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.GamePanelDimensionProvider;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

@Data
@Slf4j
public class UserProfilContext extends GraphicalContext<BorderPane> {

    private final static String LOGO_PATH = "data/common/images/gazeplay.png";
    private int nbUser = 1;

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public static UserProfilContext newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        GamePanelDimensionProvider gamePanelDimensionProvider = new GamePanelDimensionProvider(root,
                gazePlay.getPrimaryScene());

        return new UserProfilContext(gazePlay, root, gamePanelDimensionProvider, config);
    }

    @Setter
    @Getter
    private GazeDeviceManager gazeDeviceManager;

    @Setter
    private Configuration config;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    private final double cardHeight;
    private final double cardWidth;

    private List<String> allUsers;

    public UserProfilContext(GazePlay gazePlay, BorderPane root, GamePanelDimensionProvider gamePanelDimensionProvider,
            Configuration config) {
        super(gazePlay, root);

        this.config = config;

        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
        cardHeight = gamePanelDimensionProvider.getDimension2D().getHeight() / 4;

        cardWidth = gamePanelDimensionProvider.getDimension2D().getWidth() / 8;

        Node logo = createLogo();
        StackPane topLogoPane = new StackPane();
        topLogoPane.getChildren().add(logo);

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        CustomButton exitButton = createExitButton();
        topRightPane.getChildren().addAll(exitButton);

        ProgressIndicator indicator = new ProgressIndicator(0);
        Node userPickerChoicePane = createuUserPickerChoicePane(gazePlay, indicator);

        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        centerCenterPane.getChildren().add(userPickerChoicePane);

        BorderPane topPane = new BorderPane();
        topPane.setCenter(topLogoPane);
        topPane.setRight(topRightPane);

        root.setTop(topPane);
        root.setCenter(centerCenterPane);

        root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

    }

    private ScrollPane createuUserPickerChoicePane(GazePlay gazePlay, ProgressIndicator indicator) {

        final int flowpaneGap = 40;
        FlowPane choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(flowpaneGap);
        choicePanel.setVgap(flowpaneGap);
        choicePanel.setPadding(new Insets(20, 60, 20, 60));

        ScrollPane choicePanelScroller = new ScrollPane(choicePanel);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        HBox userCard = createUser(choicePanel, gazePlay, null, null, 0);
        choicePanel.getChildren().add(userCard);

        File directory = new File(Utils.getGazePlayFolder() + "profiles");
        log.info(Utils.getGazePlayFolder() + "profiles");
        String[] nameList = directory.list();
        if (nameList != null) {
            nbUser = nbUser + nameList.length;
            allUsers = new LinkedList<String>();
            for (String names : nameList) {
                allUsers.add(names);
            }
        }

        for (int i = 1; i < nbUser; i++) {
            log.info("Profile founded : ={}", nameList[i - 1]);
            Configuration.setCONFIGPATH(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + nameList[i - 1]
                    + Utils.FILESEPARATOR + "GazePlay.properties");
            Configuration conf2 = Configuration.createFromPropertiesResource();
            ImagePattern ip = null;
            String s = conf2.getUserPicture();
            if (s != null) {
                File f = new File(s);
                if (f.exists()) {
                    try {
                        ip = new ImagePattern(new Image(new FileInputStream(f)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            userCard = createUser(choicePanel, gazePlay, nameList[i - 1], ip, i);
            choicePanel.getChildren().add(userCard);

        }
        Configuration.setCONFIGPATH(Utils.getGazePlayFolder() + "GazePlay.properties");

        userCard = createUser(choicePanel, gazePlay, null, null, nbUser);
        choicePanel.getChildren().add(userCard);

        return choicePanelScroller;
    }

    private User createUser(FlowPane choicePanel, GazePlay gazePlay, String name, ImagePattern ip, int i) {
        User user = new User();
        user.setAlignment(Pos.TOP_RIGHT);

        BorderPane c = new BorderPane();
        c.getStyleClass().add("gameChooserButton");
        c.getStyleClass().add("button");
        c.setPadding(new Insets(10, 10, 10, 10));

        user.getChildren().add(c);

        Rectangle r = new Rectangle(0, 0, cardWidth, cardHeight);
        if (ip != null) {
            r.setFill(ip);
        } else {
            if (i == nbUser) {
                r.setFill(new ImagePattern(new Image("data/common/images/AddUser.png")));
            } else if (i == 0) {
                r.setFill(new ImagePattern(new Image("data/common/images/ConfigUser.png")));
            } else {
                r.setFill(new ImagePattern(new Image("data/common/images/DefaultUser.png")));
            }

        }
        c.setCenter(r);
        if (i == 0) {
            user.name = getGazePlay().getTranslator().translate("DefaultUser");
        } else if (i == nbUser) {
            user.name = getGazePlay().getTranslator().translate("AddUser");
        } else {
            if ((name != null) && (!name.equals(""))) {
                user.name = name;
            } else {
                user.name = "User" + i;
            }

            BorderPane rm = createRemoveButton(getGazePlay(), choicePanel, user);

            BorderPane modif = createModifButton(getGazePlay(), choicePanel, user);

            VBox buttonBox = new VBox();
            // buttonBox.setSpacing(10);

            buttonBox.getChildren().addAll(modif, rm);

            user.getChildren().add(buttonBox);
        }
        Text t = new Text(user.name);
        t.setFill(Color.WHITE);
        t.getStyleClass().add("gameChooserButtonTitle");
        BorderPane.setAlignment(t, Pos.BOTTOM_CENTER);
        c.setBottom(t);
        EventHandler<Event> enterh;

        enterh = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {

                if (!user.name.equals(getGazePlay().getTranslator().translate("DefaultUser"))) {

                    Configuration.setCONFIGPATH(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + user.name
                            + Utils.FILESEPARATOR + "GazePlay.properties");
                    Configuration.setInstance(Configuration.createFromPropertiesResource());

                    config = Configuration.getInstance();

                }

                if (getGazePlay().getTranslator() instanceof DefaultTranslator) {
                    ((DefaultTranslator) gazePlay.getTranslator()).setConfig(config);
                }

                CssUtil.setPreferredStylesheets(config, gazePlay.getPrimaryScene());

                BackgroundMusicManager.getInstance().stop();

                BackgroundMusicManager.setInstance(new BackgroundMusicManager());

                gazePlay.getHomeMenuScreen().clear();

                gazePlay.setHomeMenuScreen(HomeMenuScreen.newInstance(getGazePlay(), config));

                choicePanel.getChildren().clear();
                gazePlay.getHomeMenuScreen().setUpOnStage(gazePlay.getPrimaryScene());

            }
        };

        if (i == nbUser) {
            enterh = new EventHandler<Event>() {
                @Override
                public void handle(Event event) {

                    log.info("Adding user");
                    root.setEffect(new BoxBlur());
                    Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user, true);

                    String dialogTitle = getGazePlay().getTranslator().translate("NewUser");
                    dialog.setTitle(dialogTitle);
                    dialog.show();

                    dialog.toFront();
                    dialog.setAlwaysOnTop(true);
                }
            };
        }

        c.addEventFilter(MouseEvent.MOUSE_CLICKED, enterh);
        return user;
    }

    private BorderPane createRemoveButton(GazePlay gazePlay, FlowPane choicePanel, User user) {
        double size = Screen.getPrimary().getBounds().getWidth() / 50;
        CustomButton removeButton = new CustomButton("data/common/images/error.png", size);
        EventHandler<Event> removeHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                Stage dialog = createRemoveDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user);

                String dialogTitle = getGazePlay().getTranslator().translate("Remove");
                dialog.setTitle(dialogTitle);
                dialog.show();

                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            }
        };
        removeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, removeHandler);

        BorderPane rbp = new BorderPane();
        rbp.getStyleClass().add("gameChooserButton");
        rbp.getStyleClass().add("button");
        rbp.setCenter(removeButton);
        rbp.maxWidthProperty().bind(removeButton.widthProperty());
        rbp.maxHeightProperty().bind(removeButton.heightProperty());
        rbp.minWidthProperty().bind(removeButton.widthProperty());
        rbp.minHeightProperty().bind(removeButton.heightProperty());

        return rbp;
    }

    private BorderPane createModifButton(GazePlay gazePlay, FlowPane choicePanel, User user) {
        double size = Screen.getPrimary().getBounds().getWidth() / 50;
        CustomButton removeButton = new CustomButton("data/common/images/configuration-button-alt3.png", size);
        EventHandler<Event> removeHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, user, false);

                String dialogTitle = getGazePlay().getTranslator().translate("UserModif");
                dialog.setTitle(dialogTitle);
                dialog.show();

                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            }
        };
        removeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, removeHandler);

        BorderPane rbp = new BorderPane();
        rbp.getStyleClass().add("gameChooserButton");
        rbp.getStyleClass().add("button");
        rbp.setCenter(removeButton);
        rbp.maxWidthProperty().bind(removeButton.widthProperty());
        rbp.maxHeightProperty().bind(removeButton.heightProperty());
        rbp.minWidthProperty().bind(removeButton.widthProperty());
        rbp.minHeightProperty().bind(removeButton.heightProperty());

        return rbp;
    }

    private CustomButton createExitButton() {
        CustomButton exitButton = new CustomButton("data/common/images/power-off.png");
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> System.exit(0));
        return exitButton;
    }

    private Node createLogo() {
        double width = root.getWidth() * 0.5;
        double height = root.getHeight() * 0.2;

        log.info(LOGO_PATH);
        final Image logoImage = new Image(LOGO_PATH, width, height, true, true);
        final ImageView logoView = new ImageView(logoImage);

        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            final double newHeight = newValue.doubleValue() * 0.2;
            final Image newLogoImage = new Image(LOGO_PATH, width, newHeight, true, true);
            logoView.setImage(newLogoImage);
        });

        return logoView;
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    private Stage createRemoveDialog(GazePlay gazePlay, Stage primaryStage, FlowPane choicePanel, User user) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
        });

        VBox choicePane = new VBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        Button yes = new Button(getGazePlay().getTranslator().translate("YesRemove"));
        yes.getStyleClass().add("gameChooserButton");
        yes.getStyleClass().add("gameVariation");
        yes.getStyleClass().add("button");
        yes.setMinHeight(primaryStage.getHeight() / 10);
        yes.setMinWidth(primaryStage.getWidth() / 10);
        choicePane.getChildren().add(yes);

        EventHandler<Event> yesHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                dialog.close();
                choicePanel.getChildren().remove(user);
                File userDirectory = new File(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + user.name);
                deleteDir(userDirectory);
                if (allUsers != null) {
                    for (String names : allUsers) {
                        if (names.equals(user.name)) {
                            allUsers.remove(names);
                        }
                    }
                }
                log.info("Profile: " + user.name + " deleted");
            }
        };

        yes.addEventFilter(MouseEvent.MOUSE_CLICKED, yesHandler);

        Button no = new Button(getGazePlay().getTranslator().translate("NoCancel"));
        no.getStyleClass().add("gameChooserButton");
        no.getStyleClass().add("gameVariation");
        no.getStyleClass().add("button");
        no.setMinHeight(primaryStage.getHeight() / 10);
        no.setMinWidth(primaryStage.getWidth() / 10);
        choicePane.getChildren().add(no);

        EventHandler<Event> noHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                dialog.close();
            }
        };

        no.addEventFilter(MouseEvent.MOUSE_CLICKED, noHandler);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

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
        dialog.setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
        });

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

        Button tfi = new Button(getGazePlay().getTranslator().translate("ChooseImage"));
        tfi.getStyleClass().add("gameChooserButton");
        tfi.getStyleClass().add("gameVariation");
        tfi.getStyleClass().add("button");
        tfi.setMinHeight(primaryStage.getHeight() / 20);
        tfi.setMinWidth(primaryStage.getWidth() / 10);

        ImageView iv = new ImageView();

        EventHandler<Event> chooseImageHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                String s = getImage(tfi, dialog);
                if (s != null) {
                    tfi.setText(s);
                }
            }
        };

        tfi.addEventFilter(MouseEvent.MOUSE_CLICKED, chooseImageHandler);

        Button reset = new Button(getGazePlay().getTranslator().translate("reset"));
        reset.getStyleClass().add("gameChooserButton");
        reset.getStyleClass().add("gameVariation");
        reset.getStyleClass().add("button");
        reset.setMinHeight(primaryStage.getHeight() / 20);
        reset.setMinWidth(primaryStage.getWidth() / 20);

        EventHandler<Event> resetHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tfi.setGraphic(null);
                tfi.setText(getGazePlay().getTranslator().translate("ChooseImage"));
            }
        };

        reset.addEventFilter(MouseEvent.MOUSE_CLICKED, resetHandler);

        imageField.getChildren().addAll(ti, tfi, reset);

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
            event = new EventHandler<Event>() {
                @Override
                public void handle(Event mouseEvent) {
                    int temp = nbUser;
                    nbUser++;

                    ImagePattern ip = null;
                    if (tfi.getGraphic() != null) {
                        ip = new ImagePattern(((ImageView) tfi.getGraphic()).getImage());
                    }

                    User newser = createUser(choicePanel, gazePlay, tf.getText(), ip, temp);

                    if (checkNewName(newser.name)) {

                        choicePanel.getChildren().remove(user);
                        choicePanel.getChildren().add(newser);
                        choicePanel.getChildren().add(user);

                        Configuration.setCONFIGPATH(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR
                                + newser.name + Utils.FILESEPARATOR + "GazePlay.properties");
                        Configuration.setInstance(Configuration.createFromPropertiesResource());
                        Configuration conf2 = Configuration.getInstance();
                        File userDirectory = new File(
                                Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + newser.name);
                        userDirectory.mkdirs();

                        conf2.setUserName(newser.name);

                        log.info("THE NAME OF THE NEW USER IS = {}", conf2.getUserName());

                        if (!tfi.getText().equals(getGazePlay().getTranslator().translate("ChooseImage"))) {

                            File src = new File(tfi.getText());
                            File dst = new File(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR
                                    + newser.name + Utils.FILESEPARATOR + src.getName());
                            copyFileUsingStream(src, dst);

                            conf2.setUserPicture(dst.getAbsolutePath());
                        }

                        conf2.setFileDir(Configuration.getFileDirectoryUserValue(newser.name));

                        conf2.saveConfigIgnoringExceptions();

                        dialog.close();
                        primaryStage.getScene().getRoot().setEffect(null);

                    } else {
                        Text error = new Text(getGazePlay().getTranslator().translate("AlreadyUsed"));
                        error.setFill(Color.RED);
                        choicePane.getChildren().add(error);
                    }

                }
            };
        } else {
            event = new EventHandler<Event>() {
                @Override
                public void handle(Event mouseEvent) {
                    int temp = nbUser;
                    ImagePattern ip = null;
                    if (tfi.getGraphic() != null) {
                        ip = new ImagePattern(((ImageView) tfi.getGraphic()).getImage());
                    }
                    modifUser(user, choicePanel, gazePlay, user.name, ip, temp);

                    Configuration.setCONFIGPATH(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + user.name
                            + Utils.FILESEPARATOR + "GazePlay.properties");
                    Configuration conf2 = Configuration.createFromPropertiesResource();

                    if (!tfi.getText().equals(getGazePlay().getTranslator().translate("ChooseImage"))) {

                        File src = new File(tfi.getText());
                        File dst = new File(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + user.name
                                + Utils.FILESEPARATOR + src.getName());
                        copyFileUsingStream(src, dst);

                        conf2.setUserPicture(dst.getAbsolutePath());
                    }
                    conf2.saveConfigIgnoringExceptions();

                    dialog.close();
                    primaryStage.getScene().getRoot().setEffect(null);
                }
            };
        }
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);

        return dialog;
    }

    private boolean checkNewName(String s) {
        boolean isNew = true;

        if (allUsers != null) {
            for (String names : allUsers) {
                if (names.equals(s)) {
                    isNew = false;
                    break;
                }
            }
        }

        return (isNew && !s.equals(getGazePlay().getTranslator().translate("DefaultUser")));
    }

    private void modifUser(HBox user, FlowPane choicePanel, GazePlay gazePlay, String name, ImagePattern ip, int temp) {

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

    private String getImage(Button tfi, Stage primaryStage) {
        String s = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff"),
                new ExtensionFilter("PNG Files", "*.png"), new ExtensionFilter("JPeg Files", "*.jpg", "*.jpeg"),
                new ExtensionFilter("GIF Files", "*.gif"), new ExtensionFilter("BMP Files", "*.bmp"),
                new ExtensionFilter("TIFF Files", "*.tiff"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
            ImageView iv;
            try {
                iv = new ImageView(new Image(new FileInputStream(selectedFile)));
                iv.setPreserveRatio(true);
                iv.setFitHeight(primaryStage.getHeight() / 10);
                tfi.setGraphic(iv);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    private static boolean copyFileUsingStream(File source, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            is.close();
            os.close();
            return true;
        } catch (Exception e) {
            log.info("Unable to copy the profile picture");
            return false;
        }
    }

}
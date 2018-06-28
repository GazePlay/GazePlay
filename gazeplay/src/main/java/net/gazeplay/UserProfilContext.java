package net.gazeplay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.GamePanelDimensionProvider;

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

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    private final double cardHeight;
    private final double cardWidth;

    public UserProfilContext(GazePlay gazePlay, BorderPane root, GamePanelDimensionProvider gamePanelDimensionProvider,
            Configuration config) {
        super(gazePlay, root);

        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
        cardHeight = gamePanelDimensionProvider.getDimension2D().getHeight() / 4;
        ;
        cardWidth = gamePanelDimensionProvider.getDimension2D().getWidth() / 8;
        ;

        Node logo = createLogo();
        StackPane topLogoPane = new StackPane();
        topLogoPane.getChildren().add(logo);

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        CustomButton exitButton = createExitButton();
        topRightPane.getChildren().addAll(exitButton);

        ProgressIndicator indicator = new ProgressIndicator(0);
        Node userPickerChoicePane = createuUserPickerChoicePane(gazePlay, config, indicator);

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

    private ScrollPane createuUserPickerChoicePane(GazePlay gazePlay, Configuration config,
            ProgressIndicator indicator) {

        final int flowpaneGap = 40;
        FlowPane choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(flowpaneGap);
        choicePanel.setVgap(flowpaneGap);
        choicePanel.setPadding(new Insets(20, 60, 20, 60));

        ScrollPane choicePanelScroller = new ScrollPane(choicePanel);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        BorderPane userCard = createUser(choicePanel, gazePlay, null, null, 0);
        choicePanel.getChildren().add(userCard);

        for (int i = 1; i <= nbUser; i++) {
            userCard = createUser(choicePanel, gazePlay, null, null, i);
            choicePanel.getChildren().add(userCard);
        }

        return choicePanelScroller;
    }

    private BorderPane createUser(FlowPane choicePanel, GazePlay gazePlay, String name, ImagePattern ip, int i) {
        BorderPane c = new BorderPane();
        c.getStyleClass().add("gameChooserButton");
        c.getStyleClass().add("button");
        c.setPadding(new Insets(40, 50, 10, 50));

        Rectangle r = new Rectangle(0, 0, cardWidth, cardHeight);
        if (ip != null) {
            r.setFill(ip);
        } else {
            r.setFill(Color.DIMGRAY);
        }
        c.setCenter(r);
        String userName;
        if (i == 0) {
            userName = "Default User";
        } else if (i == nbUser) {
            userName = "Add User";
        } else {
            if ((name != null) && (!name.equals(""))) {
                userName = name;
            } else {
                userName = "User" + i;
            }

            CustomButton rm = createRemoveButton(getGazePlay(), choicePanel, c);
            BorderPane.setAlignment(rm, Pos.TOP_RIGHT);
            c.setTop(rm);
        }
        Text t = new Text(userName);
        t.setFill(Color.WHITE);
        t.getStyleClass().add("gameChooserButtonTitle");
        BorderPane.setAlignment(t, Pos.BOTTOM_CENTER);
        c.setBottom(t);
        EventHandler<Event> enterh;

        enterh = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                gazePlay.getHomeMenuScreen().setUpOnStage(gazePlay.getPrimaryScene());
            }
        };

        if (i == nbUser) {
            enterh = new EventHandler<Event>() {
                @Override
                public void handle(Event event) {

                    log.info("Adding user");
                    root.setEffect(new BoxBlur());
                    Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, c);

                    String dialogTitle = "New User";
                    dialog.setTitle(dialogTitle);
                    dialog.show();

                    dialog.toFront();
                    dialog.setAlwaysOnTop(true);
                }
            };
        }

        c.getCenter().addEventFilter(MouseEvent.MOUSE_PRESSED, enterh);
        return c;
    }

    private CustomButton createRemoveButton(GazePlay gazePlay, FlowPane choicePanel, BorderPane c) {
        CustomButton removeButton = new CustomButton("data/common/images/power-off.png");
        EventHandler<Event> removeHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                Stage dialog = createRemoveDialog(gazePlay, gazePlay.getPrimaryStage(), choicePanel, c);

                String dialogTitle = "Do you really want to remove this user ?";
                dialog.setTitle(dialogTitle);
                dialog.show();

                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            }
        };
        removeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, removeHandler);
        return removeButton;
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

    private Stage createRemoveDialog(GazePlay gazePlay, Stage primaryStage, FlowPane choicePanel, BorderPane c) {
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

        Button yes = new Button("Yes, remove !");
        yes.getStyleClass().add("gameChooserButton");
        yes.getStyleClass().add("gameVariation");
        yes.getStyleClass().add("button");
        yes.setMinHeight(primaryStage.getHeight() / 10);
        yes.setMinWidth(primaryStage.getWidth() / 10);
        choicePane.getChildren().add(yes);

        EventHandler<Event> yesHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                choicePanel.getChildren().remove(c);
                dialog.close();
            }
        };

        yes.addEventFilter(MouseEvent.MOUSE_CLICKED, yesHandler);

        Button no = new Button("No, cancel.");
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

        final Configuration config = Configuration.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);

        return dialog;
    }

    private Stage createDialog(GazePlay gazePlay, Stage primaryStage, FlowPane choicePanel, BorderPane c) {
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

        final Configuration config = Configuration.getInstance();

        HBox nameField = new HBox();
        nameField.setAlignment(Pos.CENTER);

        Text t = new Text("Name : ");
        t.setFill(Color.WHITE);

        TextField tf = new TextField();
        tf.setPromptText("enter a name");
        tf.setMaxWidth(primaryStage.getWidth() / 10);

        HBox imageField = new HBox();
        imageField.setAlignment(Pos.CENTER);

        Text ti = new Text("Image : ");
        ti.setFill(Color.WHITE);

        Button tfi = new Button("choose an image");
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

        Button reset = new Button("reset");
        reset.getStyleClass().add("gameChooserButton");
        reset.getStyleClass().add("gameVariation");
        reset.getStyleClass().add("button");
        reset.setMinHeight(primaryStage.getHeight() / 20);
        reset.setMinWidth(primaryStage.getWidth() / 20);

        EventHandler<Event> resetHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tfi.setGraphic(null);
                tfi.setText("choose an image");
            }
        };

        reset.addEventFilter(MouseEvent.MOUSE_CLICKED, resetHandler);

        imageField.getChildren().addAll(ti, tfi, reset);

        nameField.getChildren().addAll(t, tf);

        choicePane.getChildren().addAll(imageField, nameField, iv);

        Button button = new Button("Validate");
        button.getStyleClass().add("gameChooserButton");
        button.getStyleClass().add("gameVariation");
        button.getStyleClass().add("button");
        button.setMinHeight(primaryStage.getHeight() / 10);
        button.setMinWidth(primaryStage.getWidth() / 10);
        choicePane.getChildren().add(button);

        EventHandler<Event> event = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                int temp = nbUser;
                nbUser++;
                choicePanel.getChildren().remove(c);

                ImagePattern ip = null;
                if (tfi.getGraphic() != null) {
                    ip = new ImagePattern(((ImageView) tfi.getGraphic()).getImage());
                }

                choicePanel.getChildren().add(createUser(choicePanel, gazePlay, tf.getText(), ip, temp));
                choicePanel.getChildren().add(c);
                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            }
        };

        button.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);

        return dialog;
    }

    private String getImage(Button tfi, Stage primaryStage) {
        String s = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PNG Files", "*.png"),
                new ExtensionFilter("JPeg Files", "*.jpg", "*.jpeg"));
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

}
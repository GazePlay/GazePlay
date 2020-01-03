package net.gazeplay.ui.scenes.ingame;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static net.gazeplay.ui.scenes.ingame.GameContext.updateConfigPane;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class GameContextFactoryBean implements FactoryBean<GameContext> {

    private static final double BUTTON_MIN_HEIGHT = 64;

    private static double computeButtonSize(Stage primaryStage) {
        return primaryStage.getWidth() / 10;
    }

    private static HBox createControlPanel() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(hbox);

        hbox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        hbox.setStyle("-fx-background-color: rgba(0, 0, 0, 1);" + " -fx-background-radius: 8px;"
            + " -fx-border-radius: 8px;" + " -fx-border-width: 5px;" + " -fx-border-color: rgba(60, 63, 65, 0.7);"
            + " -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

        return hbox;
    }

    private static void updateConfigButton(Button button, ImageView btnImg, Stage primaryStage) {
        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < BUTTON_MIN_HEIGHT) {
            buttonSize = BUTTON_MIN_HEIGHT;
        }

        btnImg.setFitWidth(buttonSize);
        btnImg.setFitHeight(buttonSize);

        button.setPrefHeight(buttonSize);
        button.setPrefWidth(buttonSize);
    }

    @Autowired
    private GazePlay gazePlay;

    @Autowired
    private Translator translator;

    @Autowired
    private GazeDeviceManagerFactory gazeDeviceManagerFactory;

    private boolean menuOpen = false;

    @Override
    public GameContext getObject() {
        final Stage primaryStage = gazePlay.getPrimaryStage();
        Assert.notNull(primaryStage, "primaryStage is required");

        Pane root = new Pane();

        root.prefWidthProperty().bind(primaryStage.widthProperty());
        root.prefHeightProperty().bind(primaryStage.heightProperty());
        root.minWidthProperty().bind(primaryStage.widthProperty());
        root.minHeightProperty().bind(primaryStage.heightProperty());

        Bravo bravo = new Bravo();

        Pane gamingRoot = new Pane();
        gamingRoot.prefWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.prefHeightProperty().bind(primaryStage.heightProperty());
        gamingRoot.minWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.minHeightProperty().bind(primaryStage.heightProperty());

        Configuration config = ActiveConfigurationContext.getInstance();
        Color color = (config.isBackgroundWhite()) ? Color.WHITE : Color.BLACK;
        gamingRoot.setBackground(new Background(new BackgroundFill(color, null, null)));

        HBox controlPanel = createControlPanel();
        // Adapt the size and position of buttons to screen width
        controlPanel.maxWidthProperty().bind(root.widthProperty());
        controlPanel.toFront();

        double buttonSize = computeButtonSize(primaryStage);

        // Button bt = new Button();
        ImageView buttonImg = new ImageView(new Image("data/common/images/configuration-button-alt4.png"));
        buttonImg.setFitWidth(buttonSize);
        buttonImg.setFitHeight(buttonSize);

        final Button bt = new Button();
        bt.setMinHeight(BUTTON_MIN_HEIGHT);
        bt.setGraphic(buttonImg);
        bt.setStyle("-fx-background-color: transparent;");
        updateConfigButton(bt, buttonImg, primaryStage);
        /*
         * bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; " +
         * "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; " + "-fx-max-height: " +
         * buttonSize + "px;");
         */

        final HBox configPane = new HBox(2);
        configPane.setAlignment(Pos.CENTER_LEFT);
        // Pane configPane = new Pane();
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> updateConfigPane(configPane, primaryStage));
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> updateConfigButton(bt, buttonImg, primaryStage));
        configPane.heightProperty().addListener((observable) -> updateConfigPane(configPane, primaryStage));

        EventHandler<MouseEvent> mousePressedControlPanelEventHandler = mouseEvent -> {
            double from = 0;
            double to = 1;
            double angle = 360;
            if (menuOpen) {
                from = 1;
                to = 0;
                angle = -1 * angle;
            } else if (!configPane.getChildren().contains(controlPanel)) {
                configPane.getChildren().add(controlPanel);
            }
            RotateTransition rt = new RotateTransition(Duration.millis(500), bt);
            rt.setByAngle(angle);
            FadeTransition ft = new FadeTransition(Duration.millis(500), controlPanel);
            ft.setFromValue(from);
            ft.setToValue(to);
            ParallelTransition pt = new ParallelTransition();
            pt.getChildren().addAll(rt, ft);
            controlPanel.setDisable(menuOpen);
            controlPanel.setMouseTransparent(menuOpen);
            controlPanel.setVisible(true);
            menuOpen = !menuOpen;
            pt.setOnFinished(actionEvent -> {
                if (!menuOpen) {
                    configPane.getChildren().remove(controlPanel);
                }
            });
            pt.play();
        };

        log.info("the value of the control bar is : =" + controlPanel.getPrefWidth());
        controlPanel.setPrefWidth(primaryStage.getWidth() / 2.5);
        controlPanel.setVisible(false);
        controlPanel.setDisable(true);
        controlPanel.setMouseTransparent(true);
        menuOpen = false;

        bt.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedControlPanelEventHandler);
        bt.getStyleClass().add("button");

        buttonTransparentHandler(bt);

        configPane.getChildren().add(bt);
        root.getChildren().add(gamingRoot);
        root.getChildren().add(configPane);

        return new GameContext(gazePlay, translator, root, gamingRoot, bravo, controlPanel, gazeDeviceManagerFactory.get(), configPane);
    }

    private void buttonTransparentHandler(Button bt) {
        FadeTransition fd = new FadeTransition(Duration.millis(500), bt);
        fd.setFromValue(1);
        fd.setToValue(0.1);

        FadeTransition initialFd = new FadeTransition(Duration.seconds(1), bt);
        initialFd.setFromValue(1);
        initialFd.setToValue(0.1);
        initialFd.setDelay(Duration.seconds(2));

        EventHandler<MouseEvent> mouseEnterControlPanelEventHandler = mouseEvent -> {
            if (!menuOpen) {
                fd.stop();
                initialFd.stop();
                bt.setOpacity(1);
            }
        };

        bt.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEnterControlPanelEventHandler);

        EventHandler<MouseEvent> mouseExitControlPanelEventHandler = mouseEvent -> {
            if (!menuOpen) {
                fd.play();
            }
        };

        bt.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitControlPanelEventHandler);

        initialFd.play();

    }

    @Override
    public Class<GameContext> getObjectType() {
        return GameContext.class;
    }

}

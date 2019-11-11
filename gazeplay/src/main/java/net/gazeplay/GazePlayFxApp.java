package net.gazeplay;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.ApplicationIconImageLocator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.latestnews.LatestNewPopup;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;
import java.awt.event.InputEvent;

@Slf4j
@SpringBootApplication
public class GazePlayFxApp extends Application {

    private ConfigurableApplicationContext context;

    private GazePlay gazePlay;

    private ApplicationIconImageLocator applicationIconImageLocator;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        SpringApplicationBuilder builder = new SpringApplicationBuilder(GazePlayFxApp.class, SpringApplication.class);
        context = builder.run(args);

        applicationIconImageLocator = context.getBean(ApplicationIconImageLocator.class);
        gazePlay = context.getBean(GazePlay.class);
    }

    @Override
    public void start(Stage primaryStage) {
        autosize(primaryStage);

        final Scene primaryScene = createPrimaryScene(primaryStage);
        configureKeysHandler(primaryScene);

        configurePrimaryStage(primaryStage);

        LatestNewPopup.displayIfNeeded();

        gazePlay.setPrimaryScene(primaryScene);
        gazePlay.setPrimaryStage(primaryStage);
        gazePlay.goToUserPage();

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private Scene createPrimaryScene(Stage primaryStage) {
        Scene primaryScene = new Scene(new Pane(), primaryStage.getWidth(), primaryStage.getHeight(), Color.BLACK);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), primaryScene);
        primaryStage.setScene(primaryScene);
        return primaryScene;
    }

    private void configurePrimaryStage(Stage primaryStage) {
        primaryStage.setTitle("GazePlay");
        primaryStage.setOnCloseRequest((WindowEvent we) -> primaryStage.close());

        Image icon = applicationIconImageLocator.findApplicationIcon();
        if (icon != null) {
            primaryStage.getIcons().add(icon);
        }
    }

    private void autosize(Stage primaryStage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D screenBounds = screen.getBounds();
        primaryStage.setWidth(screenBounds.getWidth() * 0.95);
        primaryStage.setHeight(screenBounds.getHeight() * 0.90);
        primaryStage.setMaximized(false);

        primaryStage.setFullScreen(true);
    }

    private void configureKeysHandler(Scene primaryScene) {
        primaryScene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.SPACE && ActiveConfigurationContext.getInstance().isGazeMouseEnable()) {
                Platform.runLater(() -> {
                    try {
                        Robot robot = new Robot();
                        robot.mousePress(InputEvent.BUTTON1_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    } catch (AWTException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
            }
        });
        primaryScene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            final Configuration activeConfig = ActiveConfigurationContext.getInstance();
            if (ke.getCode() == KeyCode.S && activeConfig.isGazeMouseEnable()) {
                activeConfig.setMouseFree(!activeConfig.isMouseFree());
            }
        });
    }

}

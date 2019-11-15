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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ApplicationIconImageLocator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.latestnews.LatestNewPopup;
import net.gazeplay.ui.scenes.gamemenu.GameMenuFactory;
import org.springframework.context.ApplicationContext;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;

@Slf4j
public class GazePlayFxApp extends Application {

    @Setter
    private static ApplicationContext applicationContext;

    @Setter
    private static ReusableOptions applicationOptions;

    private ApplicationContext context;

    private ReusableOptions options;

    private GazePlay gazePlay;

    private ApplicationIconImageLocator applicationIconImageLocator;

    private GameMenuFactory gameMenuFactory;

    private GamesLocator gamesLocator;
    
    private Translator translator;

    @Override
    public void init() {
        this.context = applicationContext;
        this.options = applicationOptions;
        //
        log.info("options = {}", options);
        //
        applicationIconImageLocator = context.getBean(ApplicationIconImageLocator.class);
        gazePlay = context.getBean(GazePlay.class);
        gameMenuFactory = context.getBean(GameMenuFactory.class);
        gamesLocator = context.getBean(GamesLocator.class);
        translator = context.getBean(Translator.class);
    }

    @Override
    public void start(Stage primaryStage) {
        autosize(primaryStage);

        boolean showUserSelectPage = true;
        if (options != null) {
            if (options.getUserid() != null) {
                showUserSelectPage = false;
                ActiveConfigurationContext.switchToUser(options.getUserid());
            }
        }
        log.info("showUserSelectPage = {}", showUserSelectPage);

        final Scene primaryScene = createPrimaryScene(primaryStage);
        configureKeysHandler(primaryScene);

        configurePrimaryStage(primaryStage);

        Configuration config = ActiveConfigurationContext.getInstance();

        LatestNewPopup.displayIfNeeded(config, gazePlay.getTranslator());

        gazePlay.setPrimaryScene(primaryScene);
        gazePlay.setPrimaryStage(primaryStage);

        if (showUserSelectPage) {
            gazePlay.goToUserPage();
        } else {
            if (options.getGameNameCode() != null) {
                List<GameSpec> gameSpecs = gamesLocator.listGames(translator);
                GameSpec selectedGameSpec = gameSpecs.stream()
                    .filter(gameSpec -> gameSpec.getGameSummary().getNameCode().equals(options.getGameNameCode()))
                    .findFirst()
                    .orElse(null);

                log.info("gameSpecs = {}", gameSpecs);
                gameMenuFactory.chooseGame(gazePlay, selectedGameSpec, null);
            } else {
                gazePlay.onReturnToMenu();
            }
        }

        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), gazePlay.getPrimaryScene());
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

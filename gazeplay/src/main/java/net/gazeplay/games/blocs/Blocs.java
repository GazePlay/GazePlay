package net.gazeplay.games.blocs;

/**
 * Created by schwab on 29/10/2016.
 */

import com.sun.glass.ui.Screen;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.gaze.SecondScreen;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

public class Blocs extends Application {

    private final EventHandler<Event> enterEvent;

    private final GameContext gameContext;
    private final int nbLines;
    private final int nbColomns;
    private final boolean colors;
    private final float percents4Win;
    private final boolean useTrail;
    private final HiddenItemsGamesStats stats;

    private final int initCount;

    private final boolean hasColors;
    private final Bravo bravo = Bravo.getBravo();
    private final Bloc[][] blocs;
    private final int trail = 10;
    private final Image[] images;

    private int count;
    private boolean finished;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Blocs");

        primaryStage.setFullScreen(true);

        Group blockRoot = new Group();

        Scene theScene = new Scene(blockRoot, Screen.getScreens().get(0).getWidth(),
                Screen.getScreens().get(0).getHeight(), Color.BLACK);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(theScene);

        HiddenItemsGamesStats stats = new HiddenItemsGamesStats(theScene);

        GameContext gameContext = new GameContext(null, blockRoot, theScene);

        Blocs blocs = new Blocs(gameContext, 2, 2, true, 1, false, stats);
        blocs.makeBlocks();

        primaryStage.show();

        SecondScreen.launch();
    }

    public Blocs(GameContext gameContext, int nbLines, int nbColomns, boolean colors, float percents4Win,
            boolean useTrail, HiddenItemsGamesStats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColomns = nbColomns;
        this.colors = colors;
        this.percents4Win = percents4Win;
        this.useTrail = useTrail;
        this.stats = stats;

        images = Utils.images(Utils.getImagesFolder() + "blocs" + Utils.FILESEPARATOR);

        finished = false;

        hasColors = colors;

        blocs = new Bloc[nbColomns][nbLines];

        int value = (int) Math.floor(Math.random() * images.length);

        Scene scene = gameContext.getScene();

        scene.setFill(new ImagePattern(images[value]));

        enterEvent = buildEvent(gameContext, stats, useTrail);

        initCount = nbColomns * nbLines;

        count = initCount;
    }

    public void makeBlocks() {
        Scene scene = gameContext.getScene();

        double width = scene.getWidth() / nbColomns;
        double height = scene.getHeight() / nbLines;

        for (int i = 0; i < nbColomns; i++)
            for (int j = 0; j < nbLines; j++) {

                Bloc bloc = new Bloc(i * width, j * height, width + 1, height + 1, i, j);// width+1, height+1 to avoid
                // spaces between blocks for
                // Scratchcard
                if (colors)
                    bloc.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
                else
                    bloc.setFill(Color.BLACK);
                gameContext.getChildren().add(bloc);
                blocs[i][j] = bloc;

                bloc.toBack();

                GazeUtils.addEventFilter(bloc);

                bloc.addEventFilter(MouseEvent.ANY, enterEvent);

                bloc.addEventFilter(GazeEvent.ANY, enterEvent);

                stats.start();
            }
    }

    private void removeAllBlocs() {

        int maxX = blocs.length;
        int maxY = blocs[0].length;

        final Service<Void> calculateService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        for (int i = 0; i < maxX; i++) {
                            for (int j = 0; j < maxY; j++) {

                                removeBloc(blocs[i][j]);

                            }
                        }
                        return null;
                    }
                };
            }
        };
        calculateService.start();
    }

    private void removeBloc(Bloc toRemove) {

        /*
         * log.info("##############"); log.info("#####TO REMOVE#########"); log.info(toRemove.posX);
         * log.info(toRemove.posY); log.info("##############");
         */
        if (toRemove == null)
            return;

        toRemove.removeEventFilter(MouseEvent.ANY, enterEvent);
        toRemove.removeEventFilter(GazeEvent.ANY, enterEvent);
        GazeUtils.removeEventFilter(toRemove);
        toRemove.setTranslateX(-10000);
        toRemove.setOpacity(0);
        // blockRoot.getChildren().remove(toRemove);
        count--;
    }

    private EventHandler<Event> buildEvent(GameContext gameContext, HiddenItemsGamesStats stats, boolean useTrail) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                // log.info("useTrail = " + useTrail);
                // log.info("e.getEventType() = " + e.getEventType());

                if (e.getEventType().equals(MouseEvent.MOUSE_ENTERED)
                        || e.getEventType().equals(GazeEvent.GAZE_ENTERED)) {

                    if (!useTrail) {

                        Bloc bloc = (Bloc) e.getTarget();
                        removeBloc(bloc);
                    } else {

                        Bloc bloc = (Bloc) e.getTarget();

                        int posX = bloc.posX;
                        int posY = bloc.posY;

                        // log.info(bloc.posX);
                        // log.info(bloc.posY);

                        int maxX = blocs.length;
                        int maxY = blocs[0].length;

                        for (int i = -trail; i < trail; i++) {
                            for (int j = -trail; j < trail; j++) {

                                // log.info(Math.sqrt(i * i + j * j) + " : " + maxX + ", " + maxY + ", " + (posX + i) +
                                // ", " + (posY + j));
                                if (Math.sqrt(i * i + j * j) <= trail && posX + i >= 0 && posY + j >= 0
                                        && posX + i < maxX && posY + j < maxY) {
                                    // log.info("à supprimer");
                                    removeBloc(blocs[posX + i][posY + j]);
                                    blocs[posX + i][posY + j] = null;
                                }
                            }
                        }
                    }

                    if (((float) initCount - count) / initCount >= percents4Win && !finished) {

                        finished = true;

                        stats.incNbGoals();

                        removeAllBlocs();

                        gameContext.hideHomeButton();

                        bravo.playWinTransition(gameContext.getScene(), event -> {
                            gameContext.clear();
                            Blocs.this.makeBlocks();
                            // HomeUtils.home(theScene, blockRoot, choiceBox, stats);
                        });
                    }
                }
            }
        };
    }

    private static class Bloc extends Rectangle {

        public final int posX;
        public final int posY;

        public Bloc(double x, double y, double width, double height, int posX, int posY) {
            super(x, y, width, height);
            this.posX = posX;
            this.posY = posY;
        }
    }

}

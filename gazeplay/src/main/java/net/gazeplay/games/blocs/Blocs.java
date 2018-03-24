package net.gazeplay.games.blocs;

/**
 * Created by schwab on 29/10/2016.
 */

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Data;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.AspectRatioImageRectangleUtil;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

public class Blocs implements GameLifeCycle {

    private final EventHandler<Event> enterEvent;

    private final GameContext gameContext;
    private final int nbLines;
    private final int nbColomns;
    private final boolean colors;
    private final float percents4Win;
    private final boolean useTrail;
    private final Stats stats;

    private final int initCount;

    private final boolean hasColors;

    private final int trail = 10;
    private final Image[] images;

    @Data
    public static class CurrentRoundDetails {

        private int remainingCount;

        private boolean finished;

        private final Bloc[][] blocs;

        public CurrentRoundDetails(int initCount, int nbLines, int nbColumns) {
            this.remainingCount = initCount;
            this.finished = false;
            this.blocs = new Bloc[nbColumns][nbLines];
        }

    }

    private CurrentRoundDetails currentRoundDetails;

    public Blocs(GameContext gameContext, int nbLines, int nbColumns, boolean colors, float percents4Win,
            boolean useTrail, Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColomns = nbColumns;
        this.colors = colors;
        this.percents4Win = percents4Win;
        this.useTrail = useTrail;
        this.stats = stats;

        images = Utils.images(Utils.getImagesFolder() + "blocs" + Utils.FILESEPARATOR);

        hasColors = colors;

        enterEvent = buildEvent(gameContext, stats, useTrail);

        initCount = nbColumns * nbLines;
    }

    private void setHiddenPicture(GameContext gameContext) {
        final int randomPictureIndex = (int) Math.floor(Math.random() * images.length);
        final Image randomPicture = images[randomPictureIndex];

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(randomPicture, 0, 0, 1, 1, true));

        AspectRatioImageRectangleUtil aspectRatioImageRectangleUtil = new AspectRatioImageRectangleUtil();
        aspectRatioImageRectangleUtil.setFillImageKeepingAspectRatio(imageRectangle, randomPicture, dimension2D);

        gameContext.getChildren().add(imageRectangle);
    }

    @Override
    public void launch() {
        this.currentRoundDetails = new CurrentRoundDetails(initCount, nbLines, nbColomns);

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        setHiddenPicture(gameContext);

        double width = dimension2D.getWidth() / nbColomns;
        double height = dimension2D.getHeight() / nbLines;

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
                currentRoundDetails.blocs[i][j] = bloc;

                bloc.toFront();

                gameContext.getGazeDeviceManager().addEventFilter(bloc);

                bloc.addEventFilter(MouseEvent.ANY, enterEvent);

                bloc.addEventFilter(GazeEvent.ANY, enterEvent);

                stats.start();
            }
    }

    @Override
    public void dispose() {

    }

    public void removeAllBlocs() {

        final Bloc[][] blocs = currentRoundDetails.blocs;

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
        if (toRemove == null) {
            return;
        }

        toRemove.removeEventFilter(MouseEvent.ANY, enterEvent);
        toRemove.removeEventFilter(GazeEvent.ANY, enterEvent);
        gameContext.getGazeDeviceManager().removeEventFilter(toRemove);
        toRemove.setTranslateX(-10000);
        toRemove.setOpacity(0);
        currentRoundDetails.remainingCount--;
    }

    private EventHandler<Event> buildEvent(GameContext gameContext, Stats stats, boolean useTrail) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType().equals(MouseEvent.MOUSE_ENTERED)
                        || e.getEventType().equals(GazeEvent.GAZE_ENTERED)) {

                    if (!useTrail) {

                        Bloc bloc = (Bloc) e.getTarget();
                        removeBloc(bloc);
                    } else {

                        Bloc bloc = (Bloc) e.getTarget();

                        int posX = bloc.posX;
                        int posY = bloc.posY;

                        final Bloc[][] blocs = currentRoundDetails.blocs;

                        int maxX = blocs.length;
                        int maxY = blocs[0].length;

                        for (int i = -trail; i < trail; i++) {
                            for (int j = -trail; j < trail; j++) {

                                if (Math.sqrt(i * i + j * j) <= trail && posX + i >= 0 && posY + j >= 0
                                        && posX + i < maxX && posY + j < maxY) {

                                    removeBloc(blocs[posX + i][posY + j]);
                                    blocs[posX + i][posY + j] = null;
                                }
                            }
                        }
                    }

                    if (((float) initCount - currentRoundDetails.remainingCount) / initCount >= percents4Win
                            && !currentRoundDetails.finished) {

                        currentRoundDetails.finished = true;

                        stats.incNbGoals();

                        removeAllBlocs();

                        gameContext.playWinTransition(0, event -> {
                            gameContext.clear();
                            Blocs.this.launch();
                            gameContext.onGameStarted();
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

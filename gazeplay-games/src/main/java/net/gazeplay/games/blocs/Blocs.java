package net.gazeplay.games.blocs;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.AspectRatioImageRectangleUtil;

@Slf4j
public class Blocs implements GameLifeCycle {

    private final EventHandler<Event> enterEvent;

    private final IGameContext gameContext;
    private final int nbLines;
    private final int nbColomns;
    private final boolean colors;
    private final float percents4Win;
    private final Stats stats;

    private final int initCount;

    private final int trail = 10;
    private final ImageLibrary imageLibrary;

    @Data
    public static class CurrentRoundDetails {

        private int remainingCount;

        private boolean finished;

        private final Bloc[][] blocs;

        CurrentRoundDetails(final int initCount, final int nbLines, final int nbColumns) {
            this.remainingCount = initCount;
            this.finished = false;
            this.blocs = new Bloc[nbColumns][nbLines];
        }

    }

    private CurrentRoundDetails currentRoundDetails;

    public Blocs(final IGameContext gameContext, final int nbLines, final int nbColumns, final boolean colors, final float percents4Win,
                 final boolean useTrail, final Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColomns = nbColumns;
        this.colors = colors;
        this.percents4Win = percents4Win;
        this.stats = stats;

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("blocs"));

        enterEvent = buildEvent(gameContext, stats, useTrail);

        initCount = nbColumns * nbLines;
    }

    private void setHiddenPicture(final IGameContext gameContext) {
        final Image randomPicture = imageLibrary.pickRandomImage();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(randomPicture, 0, 0, 1, 1, true));

        final AspectRatioImageRectangleUtil aspectRatioImageRectangleUtil = new AspectRatioImageRectangleUtil();
        aspectRatioImageRectangleUtil.setFillImageKeepingAspectRatio(imageRectangle, randomPicture, dimension2D);

        gameContext.getChildren().add(imageRectangle);
    }

    @Override
    public void launch() {
        this.currentRoundDetails = new CurrentRoundDetails(initCount, nbLines, nbColomns);

        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        setHiddenPicture(gameContext);

        final double width = dimension2D.getWidth() / nbColomns;
        final double height = dimension2D.getHeight() / nbLines;

        for (int i = 0; i < nbColomns; i++) {
            for (int j = 0; j < nbLines; j++) {

                final Bloc bloc = new Bloc(i * width, j * height, width + 1, height + 1, i, j);// width+1, height+1 to avoid
                // spaces between blocks for
                // Scratchcard
                if (colors) {
                    bloc.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
                } else {
                    final Color c = (gameContext.getConfiguration().isBackgroundWhite()) ? Color.WHITE : Color.BLACK;
                    bloc.setFill(c);
                }
                gameContext.getChildren().add(bloc);
                currentRoundDetails.blocs[i][j] = bloc;

                bloc.toFront();

                gameContext.getGazeDeviceManager().addEventFilter(bloc);

                bloc.addEventFilter(MouseEvent.ANY, enterEvent);

                bloc.addEventFilter(GazeEvent.ANY, enterEvent);

            }
        }
        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {

    }

    private void removeAllBlocs() {
        final Bloc[][] blocs = currentRoundDetails.blocs;
        final int maxY = blocs[0].length;
        for (final Bloc[] bloc : blocs) {
            for (int j = 0; j < maxY; j++) {

                removeBloc(bloc[j]);
            }
        }
    }

    private void removeBloc(final Bloc toRemove) {

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

    private EventHandler<Event> buildEvent(final IGameContext gameContext, final Stats stats, final boolean useTrail) {
        return e -> {

            if (e.getEventType().equals(MouseEvent.MOUSE_ENTERED)
                || e.getEventType().equals(GazeEvent.GAZE_ENTERED)) {

                if (!useTrail) {

                    final Bloc bloc = (Bloc) e.getTarget();
                    removeBloc(bloc);
                } else {

                    final Bloc bloc = (Bloc) e.getTarget();

                    final int posX = bloc.posX;
                    final int posY = bloc.posY;

                    final Bloc[][] blocs = currentRoundDetails.blocs;

                    final int maxX = blocs.length;
                    final int maxY = blocs[0].length;

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
        };
    }

    private static class Bloc extends Rectangle {

        final int posX;
        final int posY;

        Bloc(final double x, final double y, final double width, final double height, final int posX, final int posY) {
            super(x, y, width, height);
            this.posX = posX;
            this.posY = posY;
        }

    }

}

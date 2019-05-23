package net.gazeplay.games.literacy;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Data;
import lombok.Getter;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.AspectRatioImageRectangleUtil;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Letters implements GameLifeCycle {
    private final GameContext gameContext;
    private final int nbLines;
    private final int nbColomns;
    private final int initCount;
    private final Stats stats;

    @Getter
    final String currentLanguage;

    private final ImageLibrary imageLibrary;

    CurrentRoundDetails currentRoundDetails;

    private int correctCount;

    @Data
    public static class CurrentRoundDetails {

        int remainingCount;

        // private boolean finished;

        private final Bloc[][] blocs;

        public CurrentRoundDetails(int nbLines, int nbColumns) {
            int initCount = nbColumns * nbLines;
            this.remainingCount = initCount;
            // this.finished = false;
            this.blocs = new Bloc[nbLines][nbColumns];
        }

    }

    public Letters(GameContext gameContext, int nbLines, int nbColumns, Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColomns = nbColumns;
        this.initCount = nbColumns * nbLines;

        this.stats = stats;

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("blocs"));

        String language = gameContext.currentLanguage;

        if (language.equalsIgnoreCase("fra")) {
            this.currentLanguage = "fra";
        } else {
            this.currentLanguage = "eng";
        }

    }

    private void setHiddenPicture(GameContext gameContext) {
        final Image randomPicture = imageLibrary.pickRandomImage();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(randomPicture, 0, 0, 1, 1, true));

        AspectRatioImageRectangleUtil aspectRatioImageRectangleUtil = new AspectRatioImageRectangleUtil();
        aspectRatioImageRectangleUtil.setFillImageKeepingAspectRatio(imageRectangle, randomPicture, dimension2D);

        gameContext.getChildren().add(imageRectangle);
    }

    @Override
    public void launch() {

        final Configuration config = Configuration.getInstance();

        this.currentRoundDetails = new CurrentRoundDetails(nbLines, nbColomns);

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        setHiddenPicture(gameContext);

        double width = dimension2D.getWidth() / nbColomns;
        double height = dimension2D.getHeight() / nbLines;

        Random r = new Random();
        String alphabet[] = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                "s", "t", "u", "v", "w", "x", "y", "z" };

        final String mainLetter = alphabet[(r.nextInt(alphabet.length))];

        Bloc[][] blocksList = createCards(mainLetter, r, alphabet, width, height, config);
        this.currentRoundDetails.remainingCount = correctCount;

        // gameContext.getChildren().addAll(blocksList);

        stats.notifyNewRoundReady();
    }

    private Bloc[][] createCards(String mainLetter, Random r, String alphabet[], double width, double height,
            Configuration config) {
        correctCount = 0;
        // List<Bloc> result = new ArrayList<>();
        Bloc[][] blocs = new Bloc[nbLines][nbColomns];

        final int fixationlength = config.getFixationLength();

        while (correctCount == 0 || correctCount == (nbLines * nbColomns)) {
            correctCount = 0;
            for (int i = 0; i < nbLines; i++) {
                for (int j = 0; j < nbColomns; j++) {
                    String currentLetter;

                    float f = r.nextFloat();
                    Boolean isMainLetter;

                    if (f < 0.65) {
                        isMainLetter = true;
                    } else {
                        isMainLetter = false;
                    }

                    if (isMainLetter) {
                        currentLetter = mainLetter;
                        correctCount++;
                    } else {
                        String temp = alphabet[(r.nextInt(alphabet.length))];
                        while (temp.equals(mainLetter)) {
                            temp = alphabet[(r.nextInt(alphabet.length))];
                        }
                        currentLetter = temp;
                    }

                    Bloc bloc = new Bloc(j * width, i * height, width + 1, height + 1, i, j, currentLetter,
                            isMainLetter, this, stats, gameContext, fixationlength);

                    // result.add(bloc);
                    blocs[i][j] = bloc;
                    this.currentRoundDetails.blocs[i][j] = bloc;
                    this.gameContext.getChildren().add(bloc);
                    bloc.toFront();

                }
            }
        }

        return blocs;
    }

    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.blocs != null) {
                gameContext.getChildren().removeAll(Collections.singleton(currentRoundDetails.blocs));
            }
            currentRoundDetails = null;
        }
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

    public void removeBloc(Bloc toRemove) {
        if (toRemove == null) {
            return;
        }
        toRemove.removeEventFilter(MouseEvent.ANY, toRemove.enterEvent);
        toRemove.removeEventFilter(GazeEvent.ANY, toRemove.enterEvent);
        gameContext.getGazeDeviceManager().removeEventFilter(toRemove);
        toRemove.setTranslateX(-10000);
        toRemove.setOpacity(0);
        // currentRoundDetails.remainingCount--;
    }

}

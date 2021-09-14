package net.gazeplay.games.cakes;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CakeFactory extends Parent implements GameLifeCycle {

    private static final int NB_BASES = 4;
    private static final int NB_NAPPAGES = 5;
    private static final int NB_BONBONS = 3;
    private static final int NB_DECORS = 2;

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private FadeTransition ft;
    @Getter
    public Rectangle background;

    final Color[] col = {Color.LIGHTPINK, Color.LIGHTYELLOW, Color.LIGHTGREEN, Color.LIGHTBLUE, Color.LIGHTCORAL};

    private final int[][] layers = new int[3][4];

    private final int[][] model = new int[3][4];

    @Getter
    private double buttonSize;

    private double centerX;

    private double centerY;

    @Getter
    private final int fixationLength;

    @Getter
    @Setter
    private int currentCake;

    @Getter
    @Setter
    private int maxCake;

    @Getter
    @Setter
    private CakeGameVariant variant;

    @Getter
    @Setter
    private boolean nappage;

    @Getter
    @Setter
    private List<ProgressButton>[] p;

    @Getter
    @Setter
    private StackPane[] cake;

    private Pane randomCake;

    @Getter
    @Setter
    private ProgressButton[] buttons;

    private final ReplayablePseudoRandom random;

    private boolean win = true;

    CakeFactory(final IGameContext gameContext, final Stats stats, final CakeGameVariant variant) {
        this.gameContext = gameContext;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension2D = {}", dimension2D);
        this.stats = stats;
        this.gameContext.startTimeLimiter();
        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;
        buttonSize = dimension2D.getWidth() / 8;
        cake = new StackPane[3];
        currentCake = 0;
        maxCake = 0;
        setNappage(false);
        this.variant = variant;
        buttons = new ProgressButton[6];
        this.fixationLength = gameContext.getConfiguration().getFixationLength();

        initBackground(dimension2D);
    }

    CakeFactory(final IGameContext gameContext, final Stats stats, final CakeGameVariant variant, double gameSeed) {
        this.gameContext = gameContext;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension2D = {}", dimension2D);
        this.stats = stats;
        this.gameContext.startTimeLimiter();
        this.random = new ReplayablePseudoRandom(gameSeed);
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;
        buttonSize = dimension2D.getWidth() / 8;
        cake = new StackPane[3];
        currentCake = 0;
        maxCake = 0;
        setNappage(false);
        this.variant = variant;
        buttons = new ProgressButton[6];
        this.fixationLength = gameContext.getConfiguration().getFixationLength();

        initBackground(dimension2D);
    }

    private void initBackground(Dimension2D dimension2D) {
        if (gameContext.getConfiguration().isBackgroundEnabled()) {
            background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            updateBackgroundColor(col[0]);
            this.getChildren().add(background);
            Rectangle back = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            back.setFill(new ImagePattern(new Image("data/cake/images/background.png")));
            back.setMouseTransparent(true);

            gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Void>() {
                @Override
                public Void visitLight() {
                    ColorAdjust colorAdjust = new ColorAdjust();
                    colorAdjust.setBrightness(0.5);
                    back.setEffect(colorAdjust);
                    return null;
                }

                @Override
                public Void visitDark() {
                    return null;
                }
            });

            this.getChildren().add(back);
        }
    }

    void updateBackgroundColor(Color c) {
        if (background != null) {
            background.setFill(c);

            gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Void>() {
                @Override
                public Void visitLight() {
                    background.setOpacity(0.5);
                    return null;
                }

                @Override
                public Void visitDark() {
                    return null;
                }
            });
        }
    }

    void winButton(final boolean winOnly) {
        win = true;
        boolean currentOk = true;
        if (!variant.equals(CakeGameVariant.ONELAYER)) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (layers[i][j] != model[i][j]) {
                        win = false;
                        if (i == currentCake) {
                            currentOk = false;
                        }
                    }
                }
            }
            if (layers[2][3] != model[2][3]) {
                win = false;
                if (2 == currentCake) {
                    currentOk = false;
                }
            }
        } else {
            for (int j = 0; j <= 3; j++) {
                if (layers[0][j] != model[0][j]) {
                    win = false;
                    if (0 == currentCake) {
                        currentOk = false;
                    }
                }
            }
        }

        if (!winOnly) {
            buttons[2].disable(!nappage);
            if (!nappage) {
                buttons[2].setOpacity(0.5);
            }
        }

        if (winOnly && currentOk && !variant.equals(CakeGameVariant.ONELAYER) && getMaxCake() < 2) {
            backtoScreenZero();
            setMaxCake(getMaxCake() + 1);
            setCurrentCake(getMaxCake());
            createCake(getMaxCake());
            buttons[2].disable();
            buttons[2].setOpacity(0.5);
        }

        if (win) {
            winFunction();
        }

    }

    public void active(final int i) {
        if (i == -1) {
            for (final List<ProgressButton> progressButtons : p) {
                for (final Node child : progressButtons) {
                    ((ProgressButton) child).disable();
                }
            }
        } else {
            for (int j = 0; j < p.length; j++) {
                final boolean b = true;
                if (j == i) {
                    for (final Node child : p[j]) {
                        ((ProgressButton) child).active();
                    }
                } else {
                    for (final Node child : p[j]) {
                        ((ProgressButton) child).disable();

                    }
                }
            }
        }

    }

    void winFunction() {
        active(-1);
        final FadeTransition ft = new FadeTransition(Duration.millis(500), randomCake);
        ft.setToValue(1);
        ft.setOnFinished(actionEvent -> {
            gameContext.updateScore(stats, this);
            playWin();
        });
        ft.play();
    }

    private void checkGoodAnswer() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (layers[i][j] == model[i][j]) {
                    stats.incrementNumberOfGoalsReached();
                }
            }
        }
        if (layers[2][3] == model[2][3]) {
            stats.incrementNumberOfGoalsReached();
        }
    }

    private void playWin() {
        gameContext.playWinTransition(500, actionEvent -> {

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            launch();
        });
    }

    private void createStack() {
        p = new List[6];
        for (int i = 0; i < 6; i++) {
            p[i] = new LinkedList<>();
            p[i].addAll(new ScreenCake(i, this, variant.equals(CakeGameVariant.ONELAYER)));
        }

        for (int j = 1; j < 5; j++) {
            int k = 0;
            if (j == 1) {
                k = NB_BASES + 1;
            }
            if (j == 2) {
                k = NB_NAPPAGES + 1;
            }
            if (j == 3) {
                k = NB_BONBONS + 1;
            }
            if (j == 4) {
                k = NB_DECORS + 1;
            }
            otherPages(j, k);
        }

        for (int i = 5; i >= 0; i--) {
            this.getChildren().addAll(p[i]);
        }

    }

    private void execAnim(final int i, final int j) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (j == 1) {
            aerographAnimation(i, j, dimension2D);
        } else {
            final ImageView temp = new ImageView(new Image("data/cake/images/" + (j - 1) + "" + (i + 1) + ".png"));
            temp.setFitWidth(dimension2D.getWidth() / (4 + currentCake));
            temp.setPreserveRatio(true);
            cake[currentCake].getChildren().set(j - 1, temp);
        }

        layers[currentCake][j - 1] = i + 1;
    }

    private void aerographAnimation(final int i, final int j, final Dimension2D dimension2D) {
        final double cakeheight = (((ImageView) cake[currentCake].getChildren().get(0)).getImage().getHeight()
            * ((ImageView) cake[currentCake].getChildren().get(0)).getFitWidth())
            / ((ImageView) cake[currentCake].getChildren().get(0)).getImage().getWidth();
        final double cakewidth = ((ImageView) cake[currentCake].getChildren().get(0)).getFitWidth();

        double posY = cake[currentCake]
            .localToParent(cake[currentCake].getChildren().get(0).localToParent(0, 0)).getY();
        final double updatedPosY = posY + 7 * cakeheight / 8;
        posY = posY + 1.9 * cakeheight / 8;

        final ImageView aerograph = new ImageView(new Image("data/cake/images/aero.png"));
        final ImageView aerograph2 = new ImageView(new Image("data/cake/images/aero.png"));
        aerograph.setFitWidth(dimension2D.getWidth() / 2.5);
        aerograph2.setFitWidth(dimension2D.getWidth() / 2.5);
        aerograph2.setScaleX(-1);
        aerograph.setPreserveRatio(true);
        aerograph2.setPreserveRatio(true);
        final double height = ((aerograph.getImage().getHeight()) * (dimension2D.getWidth() / 2.5))
            / aerograph.getImage().getWidth();
        final double offset = aerograph.getFitWidth();
        aerograph.setLayoutX(-offset);
        aerograph2.setLayoutX(dimension2D.getWidth());
        aerograph.setLayoutY(dimension2D.getHeight() / (3 * (currentCake + 1)));
        aerograph2.setLayoutY(dimension2D.getHeight() / (3 * (currentCake + 1)));
        gameContext.getChildren().addAll(aerograph, aerograph2);

        final Polygon spray = new Polygon();
        spray.getPoints()
            .addAll(offset, 9 * height / 11 + aerograph.localToParent(0, 0).getY(),
                dimension2D.getWidth() / 2 + cakewidth / 4, posY, dimension2D.getWidth() / 2 + cakewidth / 3,
                updatedPosY, dimension2D.getWidth() / 2 - cakewidth / 3, updatedPosY);

        final Polygon spray2 = new Polygon();
        spray2.getPoints()
            .addAll(dimension2D.getWidth() - offset,
                9 * height / 11 + aerograph.localToParent(0, 0).getY(),
                dimension2D.getWidth() / 2 - cakewidth / 4, posY, dimension2D.getWidth() / 2 - cakewidth / 3,
                updatedPosY, dimension2D.getWidth() / 2 + cakewidth / 3, updatedPosY);

        spray.setOpacity(0);
        spray2.setOpacity(0);
        gameContext.getChildren().addAll(spray, spray2);
        final TranslateTransition tt = new TranslateTransition(Duration.millis(500), aerograph);
        tt.setToX(offset);
        final TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), aerograph2);
        tt2.setToX(-offset);

        final Color[] c = {Color.rgb(232, 193, 136), Color.rgb(255, 114, 113), Color.rgb(113, 171, 255),
            Color.rgb(128, 70, 50)};

        spray.setFill(c[i]);
        spray2.setFill(c[i]);

        final ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(tt, tt2);

        final FadeTransition ft = new FadeTransition(Duration.seconds(2.5), spray);
        ft.setToValue(0.5);
        final FadeTransition ft2 = new FadeTransition(Duration.seconds(2.5), spray2);
        ft2.setToValue(0.5);
        final ImageView temp2 = (ImageView) cake[currentCake].getChildren().get(j - 1);
        final ImageView temp = new ImageView(new Image("data/cake/images/" + (j - 1) + "" + (i + 1) + ".png"));
        temp.setFitWidth(dimension2D.getWidth() / (4 + currentCake));
        temp.setPreserveRatio(true);
        cake[currentCake].getChildren().set(j - 1, temp);
        cake[currentCake].getChildren().add(j, temp2);
        final FadeTransition ft3 = new FadeTransition(Duration.seconds(1.5), temp2);
        ft3.setToValue(0);

        final ParallelTransition pt2 = new ParallelTransition();
        pt2.getChildren().addAll(ft, ft2, ft3);

        final SequentialTransition sq = new SequentialTransition();
        sq.getChildren().addAll(pt, pt2);

        final String soundResource = "data/cake/sounds/spray.mp3";
        gameContext.getSoundManager().add(soundResource);

        sq.setOnFinished(actionEvent -> {
            final TranslateTransition tt1 = new TranslateTransition(Duration.millis(500), aerograph);
            tt1.setToX(0);
            final TranslateTransition tt21 = new TranslateTransition(Duration.millis(500), aerograph2);
            tt21.setToX(0);
            final ParallelTransition pt1 = new ParallelTransition();
            pt1.getChildren().addAll(tt1, tt21);
            pt1.play();
            cake[currentCake].getChildren().remove(temp2);
            gameContext.getChildren().removeAll(spray, spray2);
        });
        sq.play();
    }

    private void otherPages(final int j, final int k) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        // Other pages
        for (int i = 0; i < k; i++) { // HomePage of the game
            final ProgressButton bt = new ProgressButton();
            bt.getButton().setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                + "-fx-max-height: " + buttonSize + "px;");
            if (i < k - 1) {
                createSelectionButton(bt, i, j, k, dimension2D);
            } else {
                createReturnButton(bt, j, dimension2D);
            }

        }
    }

    private void createSelectionButton(final ProgressButton bt, final int i, final int j, final int k, final Dimension2D dimension2D) {
        final EventHandler<Event> buttonHandler;
        bt.setLayoutX((i + 1) * dimension2D.getWidth() / k - buttonSize / 2);
        final int jndex = j - 1;
        final ImageView iv = new ImageView(new Image("data/cake/images/" + (j - 1) + "" + (i + 1) + ".png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.getButton().setRadius(buttonSize / 2);
        bt.setImage(iv);
        buttonHandler = e -> {
            if (jndex == 1) {
                setNappage(true);
            }
            execAnim(i, j);
            winButton(true);
        };
        bt.assignIndicatorUpdatable(buttonHandler, this.gameContext);
        bt.active();
        gameContext.getGazeDeviceManager().addEventFilter(bt.getButton());
        p[j].add(bt);
    }

    private void createReturnButton(final ProgressButton bt, final int j, final Dimension2D dimension2D) {
        final EventHandler<Event> buttonHandler;
        final ImageView iv = new ImageView(new Image("data/cake/images/return.png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.getButton().setRadius(buttonSize / 2);
        bt.setImage(iv);
        bt.setLayoutX(dimension2D.getWidth() - buttonSize);
        bt.setLayoutY(dimension2D.getHeight() - (1.2 * buttonSize));
        buttonHandler = e -> {
            backtoScreenZero();
        };
        bt.assignIndicatorUpdatable(buttonHandler, this.gameContext);
        bt.active();
        gameContext.getGazeDeviceManager().addEventFilter(bt.getButton());
        p[j].add(bt);
    }

    private void backtoScreenZero() {
        for (int c = 0; c <= maxCake; c++) {
            cake[c].toFront();
        }
        active(0);

        winButton(false);

        for (final Node child : p[0]) {
            child.toFront();
        }

        updateBackgroundColor(col[0]);
    }

    private void createCake(final int i) {
        layers[i][0] = 1;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        setNappage(false);
        if (i != 0) {
            cake[i - 1].getChildren().set(3, new ImageView());
        }

        final Pane grab = new Pane();
        final ImageView cakeGrabed = new ImageView(new Image("data/cake/images/01.png"));
        cakeGrabed.setFitWidth(dimension2D.getWidth() / (4 + i));
        cakeGrabed.setPreserveRatio(true);
        final ImageView grabs = new ImageView(new Image("data/cake/images/grab.png"));
        grabs.setFitWidth(dimension2D.getWidth() / (4 + i));
        grabs.setPreserveRatio(true);
        final double height = ((grabs.getImage().getHeight()) * (dimension2D.getWidth() / (4 + i)))
            / grabs.getImage().getWidth();
        final double cakeheight = ((cakeGrabed.getImage().getHeight()) * (dimension2D.getWidth() / (4 + i)))
            / cakeGrabed.getImage().getWidth();
        grabs.setY(cakeheight - height);
        final double offset = cakeGrabed.getFitWidth();
        grab.setLayoutX(-cakeGrabed.getFitWidth());
        grab.getChildren().addAll(cakeGrabed, grabs);
        gameContext.getChildren().add(grab);

        if (i != 0) {
            centerY = centerY - cakeheight / 2;
        }
        final TranslateTransition tt = new TranslateTransition(Duration.millis(1500), grab);
        tt.setToX(centerX - cakeGrabed.getFitWidth() / 2 + offset);

        final TranslateTransition tt2 = new TranslateTransition(Duration.millis(1000), grab);
        tt2.setToY(centerY);
        final SequentialTransition sq = new SequentialTransition();
        sq.getChildren().addAll(tt, tt2);
        sq.setOnFinished(actionEvent -> {

            grab.getChildren().remove(cakeGrabed);

            cake[i] = new StackPane();
            final ImageView base = new ImageView(new Image("data/cake/images/01.png"));
            base.setFitWidth(dimension2D.getWidth() / (4 + i));
            base.setPreserveRatio(true);
            cake[i].getChildren().addAll(base, new ImageView(), new ImageView(), new ImageView());

            cake[i].setLayoutX(centerX - base.getFitWidth() / 2);
            cake[i].setLayoutY(centerY);
            gameContext.getChildren().add(cake[i]);

            grab.toFront();

            final TranslateTransition tt21 = new TranslateTransition(Duration.millis(500), grab);
            tt21.setToY(-cakeheight);
            final TranslateTransition tt1 = new TranslateTransition(Duration.millis(500), grab);
            tt1.setToX(-cakeGrabed.getFitWidth());
            final SequentialTransition sq1 = new SequentialTransition();
            sq1.getChildren().addAll(tt21, tt1);
            sq1.play();

        });

        final String soundResource = "data/cake/sounds/grabcoming.mp3";
        gameContext.getSoundManager().add(soundResource);

        sq.play();
    }

    private void generateRandomCake() {
        if (!variant.equals(CakeGameVariant.ONELAYER)) {
            for (int i = 0; i < 3; i++) {
                model[i][0] = 1 + random.nextInt(4);
                model[i][1] = 1 + random.nextInt(5);
                model[i][2] = 1 + random.nextInt(3);
            }
            model[2][3] = 1 + random.nextInt(2);
        } else {
            model[0][0] = 1 + random.nextInt(4);
            model[0][1] = 1 + random.nextInt(5);
            model[0][2] = 1 + random.nextInt(3);
            model[0][3] = 1 + random.nextInt(2);
        }

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        randomCake = new Pane();
        final double originX = 0;
        double originY = 0;
        double cakeheight;
        int nbFloors;
        int nbDecoration;

        if (variant.equals(CakeGameVariant.ONELAYER)) {
            nbFloors = 1;
            nbDecoration = 4;
        } else {
            nbFloors = 3;
            nbDecoration = 3;
        }

        for (int i = 0; i < nbFloors; i++) {
            if (i == 2) {
                nbDecoration = 4;
            }
            for (int j = 0; j < nbDecoration; j++) {
                log.info("I = " + i + " et J = " + j + " data/cake/images/" + j + "" + model[i][j] + ".png");
                final ImageView cakemodel = new ImageView(new Image("data/cake/images/" + j + "" + model[i][j] + ".png"));
                cakemodel.setFitWidth(dimension2D.getWidth() / (2 * (4 + i)));
                cakemodel.setPreserveRatio(true);
                cakeheight = ((cakemodel.getImage().getHeight()) * (dimension2D.getWidth() / (2 * (4 + i))))
                    / cakemodel.getImage().getWidth();
                if ((i != 0) && (j == 0)) {
                    originY = originY - cakeheight / 2;
                }
                randomCake.getChildren().add(cakemodel);
                cakemodel.setLayoutX(originX - cakemodel.getFitWidth() / 2);
                cakemodel.setLayoutY(originY);
            }
        }

        randomCake.setLayoutX(3 * dimension2D.getWidth() / 4);
        randomCake.setLayoutY(dimension2D.getHeight() / 2);
        gameContext.getChildren().add(randomCake);
        if (variant.equals(CakeGameVariant.THREELAYERSHIDEN)) {

            final EventHandler<Event> cakeVanisher = e -> {
                log.debug("cake is vanishing");
                ft = new FadeTransition(Duration.millis(500), randomCake);
                ft.setDelay(Duration.millis(500));
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.play();
            };

            final EventHandler<Event> cakeDisplay = e -> {
                ft.stop();
                randomCake.setOpacity(1);
            };
            randomCake.addEventFilter(MouseEvent.MOUSE_ENTERED, cakeDisplay);
            randomCake.addEventFilter(MouseEvent.MOUSE_EXITED, cakeVanisher);

            log.debug("cake is vanishing");
            final FadeTransition ft0 = new FadeTransition(Duration.millis(5000), randomCake);
            ft0.setDelay(Duration.millis(5000));
            ft0.setToValue(0);
            ft0.play();
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        gameContext.getChildren().add(this);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                layers[i][j] = -1;
            }
        }

        for (int c = 0; c < 3; c++) {
            cake[c] = new StackPane();
        }

        createCake(0);
        createStack();
        active(0);

        generateRandomCake();
        winButton(false);

        this.gameContext.resetBordersToFront();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        this.gameContext.firstStart();
        gameContext.onGameStarted(2000);
    }

    @Override
    public void dispose() {
        checkGoodAnswer();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;
        buttonSize = dimension2D.getWidth() / 8;
        cake = new StackPane[3];
        currentCake = 0;
        maxCake = 0;
        setNappage(false);
    }

}

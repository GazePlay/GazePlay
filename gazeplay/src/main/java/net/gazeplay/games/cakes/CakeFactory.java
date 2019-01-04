
package net.gazeplay.games.cakes;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CakeFactory extends Parent implements GameLifeCycle {

    @Getter
    private final GameContext gameContext;

    private final Stats stats;

    @Getter
    private double buttonSize;

    private double centerX;
    private double centerY;

    @Getter
    private int fixationLength;

    @Getter
    @Setter
    private int currentCake;

    @Getter
    @Setter
    private int maxCake;

    @Getter
    @Setter
    private int mode;

    @Getter
    @Setter
    private boolean nappage;

    final int NB_BASES = 4;
    final int NB_NAPPAGES = 5;
    final int NB_BONBONS = 3;
    final int NB_DECORS = 2;

    public FadeTransition ft;

    Color[] col = { Color.LIGHTPINK, Color.LIGHTYELLOW, Color.LIGHTGREEN, Color.LIGHTBLUE, Color.LIGHTCORAL };
    @Getter
    public Rectangle r;

    @Getter
    @Setter
    private List<ProgressButton>[] p;

    int[][] layers = new int[3][4];
    int[][] model = new int[3][4];

    @Getter
    @Setter
    private StackPane[] cake;

    private Pane randomCake;

    @Getter
    @Setter
    private ProgressButton[] buttons;

    public CakeFactory(GameContext gameContext, Stats stats, int mode) {
        this.gameContext = gameContext;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension2D = {}", dimension2D);
        this.stats = stats;
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;
        buttonSize = dimension2D.getWidth() / 8;
        cake = new StackPane[3];
        currentCake = 0;
        maxCake = 0;
        setNappage(false);
        this.mode = mode;
        buttons = new ProgressButton[6];
        this.fixationLength = Configuration.getInstance().getFixationLength();

        r = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        r.setFill(col[0]);
        this.getChildren().add(r);
        Rectangle back = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        back.setFill(new ImagePattern(new Image("data/cake/images/background.png")));
        back.setMouseTransparent(true);
        this.getChildren().add(back);

    }

    public void winButton(boolean winOnly) {
        boolean win = true;
        boolean currentOk = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (layers[i][j] == model[i][j]) {
                    win = win && true;
                    if (i == currentCake) {
                        currentOk = currentOk && true;
                    }
                } else {
                    win = false;
                    if (i == currentCake) {
                        currentOk = false;
                    }
                }
            }
        }
        if (layers[2][3] == model[2][3]) {
            win = win && true;
            if (2 == currentCake) {
                currentOk = currentOk && true;
            }
        } else {
            win = false;
            if (2 == currentCake) {
                currentOk = false;
            }
        }
        if (!winOnly) {
            buttons[4].disable(!currentOk);
            buttons[2].disable(!nappage);
            if (!currentOk) {
                buttons[4].setOpacity(0.5);
            }
            if (!nappage) {
                buttons[2].setOpacity(0.5);
            }
        }

        if (win && (mode != 0)) {
            winFunction();
        }
    }

    public void active(int i) {
        if (i == -1) {
            for (int j = 0; j < p.length; j++) {
                for (Node child : p[j]) {
                    ((ProgressButton) child).disable();
                }
            }
        } else {
            for (int j = 0; j < p.length; j++) {
                boolean b = true;
                if (j == i) {
                    for (Node child : p[j]) {
                        ((ProgressButton) child).active();
                    }
                } else {
                    for (Node child : p[j]) {
                        ((ProgressButton) child).disable();

                    }
                }
            }
        }

    }

    public EventHandler<Event> createprogessButtonHandler(int i) {
        EventHandler<Event> buttonHandler;
        if (i != 4) {
            buttonHandler = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    for (Node child : p[i + 1]) {
                        child.toFront();
                    }
                    for (int c = 0; c <= maxCake; c++) {
                        cake[c].toFront();
                    }
                    active(i + 1);
                }
            };
        } else {
            buttonHandler = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    if (mode != 0) {
                        winButton(false);
                    }
                    if (maxCake < 2) {
                        maxCake++;
                        currentCake = maxCake;
                        createCake(maxCake);
                    }
                    if (mode != 0) {
                        winButton(false);
                    }
                    if (maxCake >= 2) {
                        p[0].get(p[0].size() - 2).disable();
                        p[0].get(p[0].size() - 2).setOpacity(0.5);

                    }

                }
            };
        }

        return buttonHandler;
    }

    public void winFunction() {
        active(-1);
        if (mode != 0) {
            FadeTransition ft = new FadeTransition(Duration.millis(500), randomCake);
            ft.setToValue(1);
            ft.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            if (layers[i][j] == model[i][j]) {
                                stats.incNbGoals();
                                stats.notifyNewRoundReady();
                            }
                        }
                    }
                    if (layers[2][3] == model[2][3]) {
                        stats.incNbGoals();
                        stats.notifyNewRoundReady();
                    }
                    playWin();
                }
            });

            ft.play();
        } else {
            stats.incNbGoals();
            stats.notifyNewRoundReady();

        }
    }

    public void playWin() {
        gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                dispose();

                gameContext.getGazeDeviceManager().clear();

                gameContext.clear();

                launch();

                stats.notifyNewRoundReady();

                gameContext.onGameStarted();
            }
        });
    }

    public void createStack() {
        p = new List[6];
        for (int i = 0; i < 6; i++) {
            p[i] = new LinkedList<ProgressButton>();
            p[i].addAll(new ScreenCake(i, this));
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

    public void execAnim(int i, int j) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (j == 1) {
            aerographAnimation(i, j, dimension2D);
        } else {
            ImageView temp = new ImageView(new Image("data/cake/images/" + (j - 1) + "" + (i + 1) + ".png"));
            temp.setFitWidth(dimension2D.getWidth() / (4 + currentCake));
            temp.setPreserveRatio(true);
            cake[currentCake].getChildren().set(j - 1, temp);
        }

        layers[currentCake][j - 1] = i + 1;
    }

    public void aerographAnimation(int i, int j, Dimension2D dimension2D) {
        double cakeheight = (((ImageView) cake[currentCake].getChildren().get(0)).getImage().getHeight()
                * ((ImageView) cake[currentCake].getChildren().get(0)).getFitWidth())
                / ((ImageView) cake[currentCake].getChildren().get(0)).getImage().getWidth();
        double cakewidth = ((ImageView) cake[currentCake].getChildren().get(0)).getFitWidth();

        double Ypos = cake[currentCake]
                .localToParent(((ImageView) cake[currentCake].getChildren().get(0)).localToParent(0, 0)).getY();
        double Yppos = Ypos + 7 * cakeheight / 8;
        Ypos = Ypos + 1.9 * cakeheight / 8;

        ImageView aerograph = new ImageView(new Image("data/cake/images/aero.png"));
        ImageView aerograph2 = new ImageView(new Image("data/cake/images/aero.png"));
        aerograph.setFitWidth(dimension2D.getWidth() / 2.5);
        aerograph2.setFitWidth(dimension2D.getWidth() / 2.5);
        aerograph2.setScaleX(-1);
        aerograph.setPreserveRatio(true);
        aerograph2.setPreserveRatio(true);
        double height = ((aerograph.getImage().getHeight()) * (dimension2D.getWidth() / 2.5))
                / aerograph.getImage().getWidth();
        double offset = aerograph.getFitWidth();
        aerograph.setLayoutX(-offset);
        aerograph2.setLayoutX(dimension2D.getWidth());
        aerograph.setLayoutY(dimension2D.getHeight() / (3 * (currentCake + 1)));
        aerograph2.setLayoutY(dimension2D.getHeight() / (3 * (currentCake + 1)));
        gameContext.getChildren().addAll(aerograph, aerograph2);

        Polygon spray = new Polygon();
        spray.getPoints()
                .addAll(new Double[] { offset, 9 * height / 11 + aerograph.localToParent(0, 0).getY(),
                        dimension2D.getWidth() / 2 + cakewidth / 4, Ypos, dimension2D.getWidth() / 2 + cakewidth / 3,
                        Yppos, dimension2D.getWidth() / 2 - cakewidth / 3, Yppos });

        Polygon spray2 = new Polygon();
        spray2.getPoints()
                .addAll(new Double[] { dimension2D.getWidth() - offset,
                        9 * height / 11 + aerograph.localToParent(0, 0).getY(),
                        dimension2D.getWidth() / 2 - cakewidth / 4, Ypos, dimension2D.getWidth() / 2 - cakewidth / 3,
                        Yppos, dimension2D.getWidth() / 2 + cakewidth / 3, Yppos });

        spray.setOpacity(0);
        spray2.setOpacity(0);
        gameContext.getChildren().addAll(spray, spray2);
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), aerograph);
        tt.setToX(offset);
        TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), aerograph2);
        tt2.setToX(-offset);

        Color[] c = { Color.rgb(232, 193, 136), Color.rgb(255, 114, 113), Color.rgb(113, 171, 255),
                Color.rgb(128, 70, 50) };

        spray.setFill(c[i]);
        spray2.setFill(c[i]);

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(tt, tt2);

        FadeTransition ft = new FadeTransition(Duration.seconds(2.5), spray);
        ft.setToValue(0.5);
        FadeTransition ft2 = new FadeTransition(Duration.seconds(2.5), spray2);
        ft2.setToValue(0.5);
        ImageView temp2 = (ImageView) cake[currentCake].getChildren().get(j - 1);
        ImageView temp = new ImageView(new Image("data/cake/images/" + (j - 1) + "" + (i + 1) + ".png"));
        temp.setFitWidth(dimension2D.getWidth() / (4 + currentCake));
        temp.setPreserveRatio(true);
        cake[currentCake].getChildren().set(j - 1, temp);
        cake[currentCake].getChildren().add(j, temp2);
        FadeTransition ft3 = new FadeTransition(Duration.seconds(1.5), temp2);
        ft3.setToValue(0);

        ParallelTransition pt2 = new ParallelTransition();
        pt2.getChildren().addAll(ft, ft2, ft3);

        SequentialTransition sq = new SequentialTransition();
        sq.getChildren().addAll(pt, pt2);
        Utils.playSound("data/cake/sounds/spray.mp3");
        sq.play();

        sq.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                TranslateTransition tt = new TranslateTransition(Duration.millis(500), aerograph);
                tt.setToX(0);
                TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), aerograph2);
                tt2.setToX(0);
                ParallelTransition pt = new ParallelTransition();
                pt.getChildren().addAll(tt, tt2);
                pt.play();
                cake[currentCake].getChildren().remove(temp2);
                gameContext.getChildren().removeAll(spray, spray2);

            }
        });
    }

    public void otherPages(int j, int k) {

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        // Other pages
        for (int i = 0; i < k; i++) { // HomePage of the game
            ProgressButton bt = new ProgressButton();
            bt.button.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                    + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                    + "-fx-max-height: " + buttonSize + "px;");
            if (i < k - 1) {
                createSelectionButton(bt, i, j, k, dimension2D);
            } else {
                createReturnButton(bt, j, dimension2D);
            }

        }
    }

    public void createSelectionButton(ProgressButton bt, int i, int j, int k, Dimension2D dimension2D) {
        EventHandler<Event> buttonHandler;
        bt.setLayoutX((i + 1) * dimension2D.getWidth() / k - buttonSize / 2);
        int index = i;
        int jndex = j - 1;
        ImageView iv = new ImageView(new Image("data/cake/images/" + (j - 1) + "" + (i + 1) + ".png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.button.setRadius(buttonSize / 2);
        bt.setImage(iv);
        buttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (jndex == 1) {
                    setNappage(true);
                }
                execAnim(index, j);
                winButton(true);
            }
        };
        bt.assignIndicator(buttonHandler, fixationLength);
        bt.active();
        gameContext.getGazeDeviceManager().addEventFilter(bt.button);
        p[j].add(bt);
    }

    public void createReturnButton(ProgressButton bt, int j, Dimension2D dimension2D) {
        EventHandler<Event> buttonHandler;
        ImageView iv = new ImageView(new Image("data/cake/images/return.png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.button.setRadius(buttonSize / 2);
        bt.setImage(iv);
        bt.setLayoutX(dimension2D.getWidth() - buttonSize);
        bt.setLayoutY(dimension2D.getHeight() - (1.2 * buttonSize));
        buttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                for (int c = 0; c <= maxCake; c++) {
                    cake[c].toFront();
                }
                active(0);
                if (mode != 0) {
                    winButton(false);
                }

                for (Node child : p[0]) {
                    child.toFront();
                }

                r.setFill(col[0]);
            }
        };
        bt.assignIndicator(buttonHandler, fixationLength);
        bt.active();
        gameContext.getGazeDeviceManager().addEventFilter(bt.button);
        p[j].add(bt);
    }

    public void createCake(int i) {
        layers[i][0] = 1;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        setNappage(false);
        if (i != 0) {
            cake[i - 1].getChildren().set(3, new ImageView());
            buttons[2].disable(true);
        }

        Pane grab = new Pane();
        ImageView cakeGrabed = new ImageView(new Image("data/cake/images/01.png"));
        cakeGrabed.setFitWidth(dimension2D.getWidth() / (4 + i));
        cakeGrabed.setPreserveRatio(true);
        ImageView grabs = new ImageView(new Image("data/cake/images/grab.png"));
        grabs.setFitWidth(dimension2D.getWidth() / (4 + i));
        grabs.setPreserveRatio(true);
        double height = ((grabs.getImage().getHeight()) * (dimension2D.getWidth() / (4 + i)))
                / grabs.getImage().getWidth();
        double cakeheight = ((cakeGrabed.getImage().getHeight()) * (dimension2D.getWidth() / (4 + i)))
                / cakeGrabed.getImage().getWidth();
        grabs.setY(cakeheight - height);
        double offset = cakeGrabed.getFitWidth();
        grab.setLayoutX(-cakeGrabed.getFitWidth());
        grab.getChildren().addAll(cakeGrabed, grabs);
        gameContext.getChildren().add(grab);

        if (i != 0) {
            centerY = centerY - cakeheight / 2;
        }
        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), grab);
        tt.setToX(centerX - cakeGrabed.getFitWidth() / 2 + offset);

        TranslateTransition tt2 = new TranslateTransition(Duration.millis(1000), grab);
        tt2.setToY(centerY);
        SequentialTransition sq = new SequentialTransition();
        sq.getChildren().addAll(tt, tt2);
        sq.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                grab.getChildren().remove(cakeGrabed);

                cake[i] = new StackPane();
                ImageView base = new ImageView(new Image("data/cake/images/01.png"));
                base.setFitWidth(dimension2D.getWidth() / (4 + i));
                base.setPreserveRatio(true);
                cake[i].getChildren().addAll(base, new ImageView(), new ImageView(), new ImageView());

                cake[i].setLayoutX(centerX - base.getFitWidth() / 2);
                cake[i].setLayoutY(centerY);
                gameContext.getChildren().add(cake[i]);

                grab.toFront();

                TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), grab);
                tt2.setToY(-cakeheight);
                TranslateTransition tt = new TranslateTransition(Duration.millis(500), grab);
                tt.setToX(-cakeGrabed.getFitWidth());
                SequentialTransition sq = new SequentialTransition();
                sq.getChildren().addAll(tt2, tt);
                sq.play();

            }
        });
        Utils.playSound("data/cake/sounds/grabcoming.mp3");
        sq.play();
    }

    public void generateRandomCake() {
        for (int i = 0; i < 3; i++) {
            model[i][0] = 1 + (int) (Math.random() * 4);
            model[i][1] = 1 + (int) (Math.random() * 5);
            model[i][2] = 1 + (int) (Math.random() * 3);
        }
        model[2][3] = 1 + (int) (Math.random() * 2);

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        randomCake = new Pane();
        double originX = 0;
        double originY = 0;
        double cakeheight = 0;
        for (int i = 0; i < 3; i++) {
            int k = 3;
            if (i == 2) {
                k = 4;
            }
            ;
            cakeheight = 0;
            for (int j = 0; j < k; j++) {
                ImageView cakemodel = new ImageView(new Image("data/cake/images/" + j + "" + model[i][j] + ".png"));
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
        if (mode == 2) {

            EventHandler<Event> cakeVanisher = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    log.debug("cake is vanishing");
                    ft = new FadeTransition(Duration.millis(500), randomCake);
                    ft.setDelay(Duration.millis(500));
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.play();
                }
            };

            EventHandler<Event> cakeDisplay = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    ft.stop();
                    randomCake.setOpacity(1);
                }
            };
            randomCake.addEventFilter(MouseEvent.MOUSE_ENTERED, cakeDisplay);
            randomCake.addEventFilter(MouseEvent.MOUSE_EXITED, cakeVanisher);

            log.debug("cake is vanishing");
            FadeTransition ft0 = new FadeTransition(Duration.millis(5000), randomCake);
            ft0.setDelay(Duration.millis(5000));
            ft0.setToValue(0);
            ft0.play();
        }
    }

    @Override
    public void launch() {

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

        if (mode != 0) {
            generateRandomCake();
            winButton(false);
        }

        stats.notifyNewRoundReady();

        this.gameContext.resetBordersToFront();

    }

    @Override
    public void dispose() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;
        buttonSize = dimension2D.getWidth() / 8;
        cake = new StackPane[3];
        currentCake = 0;
        maxCake = 0;
        setNappage(false);
    }

}

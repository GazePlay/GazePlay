package net.gazeplay.games.ticTacToe;

import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;


@Slf4j
public class TicTacToe extends Parent implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private TicTacToeGameVariant variant;

    private final Dimension2D dimension2D;

    private final ReplayablePseudoRandom random;

    private final int[][] game;

    private final ProgressButton[][] gamebutton;

    private boolean player1;

    private double size;
    private double ecart;
    private double zone;


    TicTacToe(final IGameContext gameContext, final Stats stats, final TicTacToeGameVariant variant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        random = new ReplayablePseudoRandom();

        game = new int[][]
            {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
            };

        gamebutton = new ProgressButton[][]
            {
                {new ProgressButton(), new ProgressButton(), new ProgressButton()},
                {new ProgressButton(), new ProgressButton(), new ProgressButton()},
                {new ProgressButton(), new ProgressButton(), new ProgressButton()}
            };
    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                game[i][j] = 0;
            }
        }

        background();
        button();
        player1 = true;

        gameContext.onGameStarted(2000);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    private void win() {
        stats.stop();

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void background() {
        Rectangle back = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        back.setFill(Color.WHITE);
        gameContext.getChildren().add(back);
        size = 0.05 * dimension2D.getHeight();
        ecart = (dimension2D.getWidth() - dimension2D.getHeight()) / 2;
        zone = (dimension2D.getHeight() - 4 * size) / 3;
        Rectangle wall;
        wall = new Rectangle(ecart + size, zone + size, dimension2D.getHeight() - 2 * size, size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
        wall = new Rectangle(ecart + size, 2 * zone + 2 * size, dimension2D.getHeight() - 2 * size, size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
        wall = new Rectangle(ecart + zone + size, size, size, dimension2D.getHeight() - 2 * size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
        wall = new Rectangle(ecart + 2 * zone + 2 * size, size, size, dimension2D.getHeight() - 2 * size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
    }

    private void button() {
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[0][0] = new ProgressButton();
            gamebutton[0][0].setLayoutX(ecart + size);
            gamebutton[0][0].setLayoutY(size);
            gamebutton[0][0].getButton().setRadius(zone / 2);
            gamebutton[0][0].setImage(white);
            gamebutton[0][0].assignIndicatorUpdatable(event -> {
                gamebutton[0][0].disable();
                gamebutton[0][0].setOpacity(1);
                if (player1) {
                    game[0][0] = 1;
                    gamebutton[0][0].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[0][0] = 2;
                    gamebutton[0][0].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[0][0]);
            gamebutton[0][0].active();
            gameContext.getChildren().add(gamebutton[0][0]);
        }
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[0][1] = new ProgressButton();
            gamebutton[0][1].setLayoutX(ecart + 2 * size + zone);
            gamebutton[0][1].setLayoutY(size);
            gamebutton[0][1].getButton().setRadius(zone / 2);
            gamebutton[0][1].setImage(white);
            gamebutton[0][1].assignIndicatorUpdatable(event -> {
                gamebutton[0][1].disable();
                gamebutton[0][1].setOpacity(1);
                if (player1) {
                    game[0][1] = 1;
                    gamebutton[0][1].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[0][1] = 2;
                    gamebutton[0][1].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[0][1]);
            gamebutton[0][1].active();
            gameContext.getChildren().add(gamebutton[0][1]);
        }
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[0][2] = new ProgressButton();
            gamebutton[0][2].setLayoutX(ecart + 3 * size + 2 * zone);
            gamebutton[0][2].setLayoutY(size);
            gamebutton[0][2].getButton().setRadius(zone / 2);
            gamebutton[0][2].setImage(white);
            gamebutton[0][2].assignIndicatorUpdatable(event -> {
                gamebutton[0][2].disable();
                gamebutton[0][2].setOpacity(1);
                if (player1) {
                    game[0][2] = 1;
                    gamebutton[0][2].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[0][2] = 2;
                    gamebutton[0][2].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[0][2]);
            gamebutton[0][2].active();
            gameContext.getChildren().add(gamebutton[0][2]);
        }

        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[1][0] = new ProgressButton();
            gamebutton[1][0].setLayoutX(ecart + size);
            gamebutton[1][0].setLayoutY(2 * size + zone);
            gamebutton[1][0].getButton().setRadius(zone / 2);
            gamebutton[1][0].setImage(white);
            gamebutton[1][0].assignIndicatorUpdatable(event -> {
                gamebutton[1][0].disable();
                gamebutton[1][0].setOpacity(1);
                if (player1) {
                    game[1][0] = 1;
                    gamebutton[1][0].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[1][0] = 2;
                    gamebutton[1][0].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[1][0]);
            gamebutton[1][0].active();
            gameContext.getChildren().add(gamebutton[1][0]);
        }
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[1][1] = new ProgressButton();
            gamebutton[1][1].setLayoutX(ecart + 2 * size + zone);
            gamebutton[1][1].setLayoutY(2 * size + zone);
            gamebutton[1][1].getButton().setRadius(zone / 2);
            gamebutton[1][1].setImage(white);
            gamebutton[1][1].assignIndicatorUpdatable(event -> {
                gamebutton[1][1].disable();
                gamebutton[1][1].setOpacity(1);
                if (player1) {
                    game[1][1] = 1;
                    gamebutton[1][1].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[1][1] = 2;
                    gamebutton[1][1].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[1][1]);
            gamebutton[1][1].active();
            gameContext.getChildren().add(gamebutton[1][1]);
        }
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[1][2] = new ProgressButton();
            gamebutton[1][2].setLayoutX(ecart + 3 * size + 2 * zone);
            gamebutton[1][2].setLayoutY(2 * size + zone);
            gamebutton[1][2].getButton().setRadius(zone / 2);
            gamebutton[1][2].setImage(white);
            gamebutton[1][2].assignIndicatorUpdatable(event -> {
                gamebutton[1][2].disable();
                gamebutton[1][2].setOpacity(1);
                if (player1) {
                    game[1][2] = 1;
                    gamebutton[1][2].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[1][2] = 2;
                    gamebutton[1][2].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[1][2]);
            gamebutton[1][2].active();
            gameContext.getChildren().add(gamebutton[1][2]);
        }

        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[2][0] = new ProgressButton();
            gamebutton[2][0].setLayoutX(ecart + size);
            gamebutton[2][0].setLayoutY(3 * size + 2 * zone);
            gamebutton[2][0].getButton().setRadius(zone / 2);
            gamebutton[2][0].setImage(white);
            gamebutton[2][0].assignIndicatorUpdatable(event -> {
                gamebutton[2][0].disable();
                gamebutton[2][0].setOpacity(1);
                if (player1) {
                    game[2][0] = 1;
                    gamebutton[2][0].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[2][0] = 2;
                    gamebutton[2][0].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[2][0]);
            gamebutton[2][0].active();
            gameContext.getChildren().add(gamebutton[2][0]);
        }
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[2][1] = new ProgressButton();
            gamebutton[2][1].setLayoutX(ecart + 2 * size + zone);
            gamebutton[2][1].setLayoutY(3 * size + 2 * zone);
            gamebutton[2][1].getButton().setRadius(zone / 2);
            gamebutton[2][1].setImage(white);
            gamebutton[2][1].assignIndicatorUpdatable(event -> {
                gamebutton[2][1].disable();
                gamebutton[2][1].setOpacity(1);
                if (player1) {
                    game[2][1] = 1;
                    gamebutton[2][1].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[2][1] = 2;
                    gamebutton[2][1].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[2][1]);
            gamebutton[2][1].active();
            gameContext.getChildren().add(gamebutton[2][1]);
        }
        {
            ImageView white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
            white.setFitHeight(zone);
            white.setFitWidth(zone);
            ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
            nought.setFitHeight(zone);
            nought.setFitWidth(zone);
            ImageView crosse = new ImageView(new Image("data/noughtsandcrosses/crosse.png"));
            crosse.setFitHeight(zone);
            crosse.setFitWidth(zone);
            gamebutton[2][2] = new ProgressButton();
            gamebutton[2][2].setLayoutX(ecart + 3 * size + 2 * zone);
            gamebutton[2][2].setLayoutY(3 * size + 2 * zone);
            gamebutton[2][2].getButton().setRadius(zone / 2);
            gamebutton[2][2].setImage(white);
            gamebutton[2][2].assignIndicatorUpdatable(event -> {
                gamebutton[2][2].disable();
                gamebutton[2][2].setOpacity(1);
                if (player1) {
                    game[2][2] = 1;
                    gamebutton[2][2].setImage(crosse);
                    player1 = false;
                    if (testgame() && variant.equals(TicTacToeGameVariant.IA)) {
                        robot();
                    }
                } else {
                    game[2][2] = 2;
                    gamebutton[2][2].setImage(nought);
                    player1 = true;
                    testgame();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(gamebutton[2][2]);
            gamebutton[2][2].active();
            gameContext.getChildren().add(gamebutton[2][2]);
        }
    }

    private boolean testgame() {
        if (game[0][0] * game[0][1] * game[0][2] == 1 ||
            game[1][0] * game[1][1] * game[1][2] == 1 ||
            game[2][0] * game[2][1] * game[2][2] == 1 ||
            game[0][0] * game[1][0] * game[2][0] == 1 ||
            game[0][1] * game[1][1] * game[2][1] == 1 ||
            game[0][2] * game[1][2] * game[2][2] == 1 ||
            game[0][0] * game[1][1] * game[2][2] == 1 ||
            game[0][2] * game[1][1] * game[2][0] == 1) {
            win();
            return false;
        } else if (game[0][0] * game[0][1] * game[0][2] == 8 ||
            game[1][0] * game[1][1] * game[1][2] == 8 ||
            game[2][0] * game[2][1] * game[2][2] == 8 ||
            game[0][0] * game[1][0] * game[2][0] == 8 ||
            game[0][1] * game[1][1] * game[2][1] == 8 ||
            game[0][2] * game[1][2] * game[2][2] == 8 ||
            game[0][0] * game[1][1] * game[2][2] == 8 ||
            game[0][2] * game[1][1] * game[2][0] == 8) {
            if (variant.equals(TicTacToeGameVariant.P2)) {
                win();
                return false;
            } else {
                restart();
                return true;
            }
        } else if (game[0][0] * game[0][1] * game[0][2] *
            game[1][0] * game[1][1] * game[1][2] *
            game[2][0] * game[2][1] * game[2][2] != 0) {
            restart();
            return true;
        }
        return true;
    }

    private void robot() {
        ImageView nought = new ImageView(new Image("data/noughtsandcrosses/nought.png"));
        nought.setFitHeight(zone);
        nought.setFitWidth(zone);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gamebutton[i][j].disable();
                gamebutton[i][j].setOpacity(1);
            }
        }
        PauseTransition wait = new PauseTransition(Duration.millis(1000));
        wait.setOnFinished(e -> {
            int x = 0;
            int y = 0;
            while (game[x][y] != 0) {
                x = random.nextInt(3);
                y = random.nextInt(3);
            }
            game[x][y] = 2;
            gamebutton[x][y].setImage(nought);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (game[i][j] == 0) {
                        gamebutton[i][j].active();
                    }
                }
            }
            testgame();
            player1 = true;
        });
        wait.play();
    }

    private void restart() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gamebutton[i][j].disable();
                gamebutton[i][j].setOpacity(1);
            }
        }
        PauseTransition wait = new PauseTransition(Duration.millis(2000));
        wait.setOnFinished(e -> {
            ImageView white;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    white = new ImageView(new Image("data/noughtsandcrosses/white.png"));
                    white.setFitHeight(zone);
                    white.setFitWidth(zone);
                    game[i][j] = 0;
                    gamebutton[i][j].setImage(white);
                    gamebutton[i][j].active();
                }
            }
            if (!player1 && variant.equals(TicTacToeGameVariant.IA)) {
                robot();
            }
        });
        wait.play();
    }
}

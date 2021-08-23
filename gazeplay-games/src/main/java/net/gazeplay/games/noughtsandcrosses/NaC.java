package net.gazeplay.games.noughtsandcrosses;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;

import java.util.ArrayList;


@Slf4j
public class NaC extends Parent implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private NaCGameVariant variant;

    private final Dimension2D dimension2D;

    private final ReplayablePseudoRandom random;

    private final int[][] game;

    private final ProgressButton[][] gamebutton;

    private boolean player1;

    private double size;
    private double ecart;
    private double zone;

    private final ArrayList<Rectangle> rectangles;


    NaC(final IGameContext gameContext, final Stats stats, final NaCGameVariant variant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        random = new ReplayablePseudoRandom();

        game = new int[][]
            {
                {0,0,0},
                {0,0,0},
                {0,0,0}
            };

        gamebutton = new ProgressButton[][]
            {
                {new ProgressButton(), new ProgressButton(), new ProgressButton()},
                {new ProgressButton(), new ProgressButton(), new ProgressButton()},
                {new ProgressButton(), new ProgressButton(), new ProgressButton()}
            };

        rectangles = new ArrayList<>();
    }

    @Override
    public void launch(){
        gameContext.getChildren().clear();

        background();
        button();
        makerec();
        player1 = true;

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose(){
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

    private void background(){
        Rectangle back = new Rectangle(0,0, dimension2D.getWidth(), dimension2D.getHeight());
        back.setFill(Color.WHITE);
        gameContext.getChildren().add(back);
        size = 0.05 * dimension2D.getHeight();
        ecart = (dimension2D.getWidth() - dimension2D.getHeight())/2;
        zone = (dimension2D.getHeight() - 4*size)/3;
        Rectangle wall;
        wall = new Rectangle(ecart + size, zone + size, dimension2D.getHeight() - 2*size, size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
        wall = new Rectangle(ecart + size, 2*zone + 2*size, dimension2D.getHeight() - 2*size, size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
        wall = new Rectangle(ecart + zone + size, size, size, dimension2D.getHeight() - 2*size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
        wall = new Rectangle(ecart + 2*zone + 2*size, size, size, dimension2D.getHeight() - 2*size);
        wall.setFill(Color.RED);
        gameContext.getChildren().add(wall);
    }

    private void button(){
        //mettre les images
        Image nought;
        Image crosse;
    }

    private void testgame(){
        if (game[0][0] * game[0][1] * game[0][2] == 1 ||
            game[1][0] * game[1][1] * game[1][2] == 1 ||
            game[2][0] * game[2][1] * game[2][2] == 1 ||
            game[0][0] * game[1][0] * game[2][0] == 1 ||
            game[0][1] * game[1][1] * game[2][2] == 1 ||
            game[0][2] * game[1][2] * game[2][1] == 1 ||
            game[0][0] * game[1][1] * game[2][2] == 1 ||
            game[0][2] * game[1][1] * game[2][0] == 1){
                win();
        }
        else if (game[0][0] * game[0][1] * game[0][2] == 8 ||
            game[1][0] * game[1][1] * game[1][2] == 8 ||
            game[2][0] * game[2][1] * game[2][2] == 8 ||
            game[0][0] * game[1][0] * game[2][0] == 8 ||
            game[0][1] * game[1][1] * game[2][2] == 8 ||
            game[0][2] * game[1][2] * game[2][1] == 8 ||
            game[0][0] * game[1][1] * game[2][2] == 8 ||
            game[0][2] * game[1][1] * game[2][0] == 8){
                if (variant.equals(NaCGameVariant.P2)){
                    win();
                } else {
                    restart();
                }
        }
        else if (game[0][0] * game[0][1] * game[0][2] *
            game[1][0] * game[1][1] * game[1][2] *
            game[2][0] * game[2][1] * game[2][2] != 0){
                restart();
        }
    }

    private void makerec(){
        rectangles.add(new Rectangle(ecart + size, size, zone, zone));
        rectangles.add(new Rectangle(ecart + 2*size + zone, size, zone, zone));
        rectangles.add(new Rectangle(ecart + 3*size + 2*zone, size, zone, zone));
        rectangles.add(new Rectangle(ecart + size, 2*size + zone, zone, zone));
        rectangles.add(new Rectangle(ecart + 2*size + zone, 2*size + zone, zone, zone));
        rectangles.add(new Rectangle(ecart + 3*size + 2*zone, 2*size + zone, zone, zone));
        rectangles.add(new Rectangle(ecart + size, 3*size + 2*zone, zone, zone));
        rectangles.add(new Rectangle(ecart + 2*size + zone, 3*size + 2*zone, zone, zone));
        rectangles.add(new Rectangle(ecart + 3*size + 2*zone, 3*size + 2*zone, zone, zone));
    }

    private void robot(){
        for (int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                gamebutton[i][j].disable();
            }
        }
        int x = 0;
        int y = 0;
        while (game[x][y]!=0){
            x = random.nextInt(3);
            y = random.nextInt(3);
        }
        game[x][y]=2;
        /*
        picture
         */
        for (int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                if (game[i][j]!=0){
                    gamebutton[i][j].active();
                }
            }
        }
        player1 = true;
    }

    private void restart(){
        for (int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                game[i][j]=0;
                gamebutton[i][j].active();
                if (!player1 && variant.equals(NaCGameVariant.IA)){
                    robot();
                }
            }
        }
        for (Rectangle r : rectangles){
            r.setFill(Color.WHITE);
        }
    }
}

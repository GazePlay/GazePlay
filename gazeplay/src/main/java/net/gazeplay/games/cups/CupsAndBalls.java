package net.gazeplay.games.cups;

import java.awt.Point;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

public class CupsAndBalls implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;
    private Ball ball;
    private Cup cups[];
    private int nbCups;
    private final int nbLines;
    private final int nbColumns;
    
    private javafx.geometry.Dimension2D dimension2D;
    
    Random random = new Random();

    public CupsAndBalls(GameContext gameContext, Stats stats, int nbCups, int nbLines, int nbColumns) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbCups = nbCups;
        this.cups = new Cup[nbCups];
        this.nbColumns = nbColumns;
        this.nbLines = nbLines;
    }
    
    private void init(){
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Image cupPicture = new Image("data/cups/images/cup.png");
        double imageWidth = dimension2D.getHeight()/(nbColumns*1.5);
        double imageHeight = dimension2D.getHeight()/nbColumns;
        PositionCup position = new PositionCup(0, nbColumns/2, nbColumns, nbLines, dimension2D.getHeight(), dimension2D.getWidth(), imageWidth, imageHeight);
        int ballInCup = random.nextInt(nbCups);
        for (int index = 0; index < cups.length; index++) {
            Point cupPos = position.calculateXY(position.getCellX(), position.getCellY());
            Rectangle cupRectangle = new Rectangle(cupPos.getX(), cupPos.getY(), imageWidth, imageHeight);
            cupRectangle.setFill(new ImagePattern(cupPicture, 0, 0, 1, 1, true));
            cups[index] = new Cup(cupRectangle, position, gameContext);
            if (index == ballInCup){
                cups[index].setBall(true);
                ball = new Ball(10, Color.RED, cups[index], gameContext);
                gameContext.getChildren().add(ball.getItem());
            }else{
                cups[index].setBall(false);
            }
            position.setCellX(position.getCellX() + 1);
            position.setCellY(position.getCellY());
            gameContext.getChildren().add(cupRectangle);
        }
    }

    @Override
    public void launch() {
        init();
        
        gameContext.getChildren().add(cups[2]);
    }

    @Override
    public void dispose() {

    }

}

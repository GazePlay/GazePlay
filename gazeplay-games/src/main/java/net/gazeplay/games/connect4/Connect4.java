package net.gazeplay.games.connect4;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Random;

public class Connect4 implements GameLifeCycle {

    private final Stats stats;
    private final IGameContext gameContext;

    // Parameters
    private final int nbRows = 6;
    private final int nbColumns = 7;
    private final Color player1Color = Color.RED;
    private final Color player2Color = Color.ORANGE;
    private final Color grid1Color = Color.BLUE;
    private double fallingDuration = 300.0;

    // Grid management
    private int[][] grid;
    private double gridWidth;
    private double gridHeight;
    private double gridXOffset;
    private double gridYOffset;
    private double cellSize;

    // Display
    private Timeline progressTimeline;
    private ArrayList<Rectangle> topRectangles;
    private Rectangle gridRectangle;
    private Pane centerPane;
    private Pane topPane;

    // Game management
    private IntegerProperty currentPlayer;

    Connect4(final IGameContext gameContext, final Stats stats){
        this.stats = stats;
        this.gameContext = gameContext;
        grid = new int[nbColumns][nbRows];
        topRectangles = new ArrayList<>();
        currentPlayer = new SimpleIntegerProperty(1);
    }

    Connect4(final IGameContext gameContext, final Stats stats, double gameSeed){
        this.stats = stats;
        this.gameContext = gameContext;
    }

    @Override
    public void launch() {

        updateSize();

        BorderPane mainPane = new BorderPane();
        gameContext.getChildren().add(mainPane);

        // Right pane
        VBox rightPane = new VBox();
        mainPane.setRight(rightPane);
        rightPane.setAlignment(Pos.CENTER);

        Label currentPlayerLabel = new I18NLabel(gameContext.getTranslator(), "CurrentPlayer");
        currentPlayerLabel.setTextFill(Color.WHITE);
        currentPlayerLabel.setFont(new Font(25));
        currentPlayerLabel.setPadding(new Insets(0,40,0,40));
        rightPane.getChildren().add(currentPlayerLabel);

        Circle currentPlayerCircle = new Circle();
        currentPlayerCircle.setRadius(cellSize*0.5);
        currentPlayerCircle.setFill(player1Color);
        rightPane.getChildren().add(currentPlayerCircle);
        currentPlayer.addListener(observable -> {
            currentPlayerCircle.setFill(currentPlayer.getValue() == 1 ? player1Color:player2Color);
        });

        // Center pane
        centerPane = new Pane();
        mainPane.setCenter(centerPane);

        gridRectangle = new Rectangle(gridXOffset, 0, gridWidth, gridHeight);
        gridRectangle.setFill(grid1Color);
        centerPane.getChildren().add(gridRectangle);

        // Top pane
        topPane = new Pane();
        mainPane.setTop(topPane);

        // Create top rectangles
        for(int i=0; i<nbColumns; i++){
            int tempi = i;

            Rectangle topRectangle = new Rectangle(gridXOffset + i*cellSize,0,cellSize,gridYOffset);
            topRectangle.setFill(i%2==0 ? player1Color:player2Color);
            topRectangles.add(topRectangle);

            ProgressIndicator pi = new ProgressIndicator(0);
            pi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            pi.setOpacity(0);

            Group group = new Group();
            group.getChildren().add(topRectangle);
            group.getChildren().add(pi);
            topPane.getChildren().add(group);

            group.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(currentPlayer.getValue()==1 && getPossiblePlays().contains(tempi)) {
                        //Progress Indicator
                        double progressSize = Math.min(topRectangle.getHeight(), topRectangle.getWidth());
                        pi.setTranslateX(gridXOffset + topRectangle.getWidth() * tempi + (topRectangle.getWidth() - progressSize) / 2);
                        pi.setTranslateY((topRectangle.getHeight() - progressSize) / 2);
                        pi.setMinSize(progressSize, progressSize);
                        pi.setManaged(true);
                        pi.setProgress(0);
                        pi.setOpacity(1);

                        progressTimeline = new Timeline();
                        progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(pi.progressProperty(), 1)));
                        progressTimeline.setOnFinished(actionEvent -> {
                            pi.setMinSize(0, 0);
                            pi.setOpacity(0);
                            putToken(tempi);
                        });
                        progressTimeline.play();
                    }
                }
            });

            group.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if(currentPlayer.getValue()==1) {
                            progressTimeline.stop();
                            pi.setMinSize(0, 0);
                            pi.setOpacity(0);
                        }
                    }
            });
        }

        // Resize events
        gameContext.getPrimaryScene().widthProperty().addListener(e -> {
            updateSize();
            resize();
        });
        gameContext.getPrimaryScene().heightProperty().addListener(e -> {
            updateSize();
            resize();
        });

        drawTokens();

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    public void updateSize(){
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        cellSize = Math.min(dimension2D.getWidth()*0.8/nbColumns, dimension2D.getHeight()*0.85/nbRows);
        gridWidth = cellSize*nbColumns;
        gridHeight = cellSize*nbRows;
        gridXOffset = (dimension2D.getWidth() - gridWidth)/2;
        gridYOffset = dimension2D.getHeight()-gridHeight;
    }

    private void resize(){
        // Clear grid
        centerPane.getChildren().clear();

        // Resize grid rectangle
        gridRectangle.setHeight(gridHeight);
        gridRectangle.setWidth(gridWidth);
        gridRectangle.setX(gridXOffset);
        gridRectangle.setY(0);
        centerPane.getChildren().add(gridRectangle);

        // Resize grid token
        drawTokens();

        // Resize top rectangles
        for (int i = 0; i < topRectangles.size(); i++) {
            topRectangles.get(i).setX(gridXOffset + i*cellSize);
            topRectangles.get(i).setWidth(cellSize);
            topRectangles.get(i).setHeight(gridYOffset);
        }
    }

    public void clearGrid(){
        for(int i = 0; i<nbColumns; i++){
            for(int j = 0; j<nbRows; j++){
                grid[i][j] = 0;
            }
        }
    }

    public void drawTokens(){
        Color color;
        for(int i = 0; i<nbColumns; i++){
            for(int j = 0; j<nbRows; j++){
                switch(grid[i][j]){
                    case 0:
                        color = Color.WHITE;
                        break;
                    case 1:
                        color = player1Color;
                        break;
                    case 2:
                        color = player2Color;
                        break;
                    default:
                        color = Color.WHITE;
                }
                double radius = cellSize*0.4;
                double centerx = gridXOffset + (i+0.5)*cellSize;
                double centery = (j+0.5)*cellSize;
                Circle c = new Circle(centerx,centery,radius);
                c.setFill(color);
                centerPane.getChildren().add(c);
            }
        }
    }

    public void putToken(int column){
        int j = nbRows-1;
        while(j>=0){
            if(grid[column][j]==0){
                // Update the grid
                grid[column][j]=currentPlayer.getValue();

                // Play animation
                double radius = cellSize*0.4;
                double centerx = gridXOffset + (column+0.5)*cellSize;
                double centery = 0.5*cellSize;
                Circle c = new Circle(centerx,centery,radius);
                c.setFill(currentPlayer.getValue() == 1 ? player1Color:player2Color);
                centerPane.getChildren().add(c);

                TranslateTransition transition = new TranslateTransition();
                transition.setDuration(Duration.millis(fallingDuration/nbRows*(j+1)));
                transition.setNode(c);

                transition.setByY(j*cellSize);
                transition.setOnFinished(e -> {
                    drawTokens();
                    gameContext.getSoundManager().add("data/connect4/sounds/tokenFalling.wav");
                });
                transition.play();
                break;
            }
            j--;
        }

        if(checkVictory()!=0){
            gameContext.playWinTransition(50, e -> restart());
        }

        currentPlayer.set(1 + currentPlayer.getValue()%2);

        if(currentPlayer.getValue() == 2){
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000)));
            timeline.setOnFinished(e -> playIA());
            timeline.play();
        }
    }

    private void playIA(){
        Random r = new Random();
        int play = r.nextInt(getPossiblePlays().size());
        play = getPossiblePlays().get(play);
        putToken(play);
    }

    private ArrayList<Integer> getPossiblePlays(){
        ArrayList<Integer> plays = new ArrayList<>();
        for(int i = 0; i<nbColumns; i++) {
            if(grid[i][0]==0){
                plays.add(i);
            }
        }
        return plays;
    }


    public int checkVictory(){
        int[][] directions = {{1,0}, {1,-1}, {1,1}, {0,1}};
        for (int[] d : directions) {
            int dx = d[0];
            int dy = d[1];
            for (int x = 0; x < nbColumns; x++) {
                for (int y = 0; y < nbRows; y++) {
                    int lastx = x + 3*dx;
                    int lasty = y + 3*dy;
                    if (0 <= lastx && lastx < nbColumns && 0 <= lasty && lasty < nbRows) {
                        int w = grid[x][y];
                        if (w != 0 && w == grid[x+dx][y+dy]
                            && w == grid[x+2*dx][y+2*dy]
                            && w == grid[lastx][lasty]) {
                            return w;
                        }
                    }
                }
            }
        }
        return 0;
    }

    private void restart(){
        gameContext.endWinTransition();
        clearGrid();
        drawTokens();
        currentPlayer.setValue(1);
    }

}

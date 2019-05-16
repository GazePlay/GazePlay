/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;


/**
 *
 * @author Peter Bardawil
 * 
 */

@Slf4j
public class slidingpuzzle implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        private final List<slidingpuzzlecard> cardList;
        private final int winnerImageIndexAmongDisplayedImages;
    }
    
    public slidingpuzzle(Stats stats, GameContext gameContext, int nbLines, int nbColumns) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

    }

    private static final float cardRatio = 0.8f;
    private static final float zoom_factor = 1.16f;
    private static final int minHeight = 30;

    private final Stats stats;
    
    private final GameContext gameContext;

    private final int nbLines;

    private final int nbColumns;
    
    private slidingpuzzle.RoundDetails currentRoundDetails;

    private javafx.geometry.Dimension2D gameDimension2D;

    @Override
    public void launch() {
        final Configuration config = Configuration.getInstance();

        // Background Color
        Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(Color.rgb(255, 0, 0));

        gameContext.getChildren().add(imageRectangle);
        
        List<slidingpuzzlecard> cardList = createCards(config);
        currentRoundDetails = new slidingpuzzle.RoundDetails(cardList, 1);

        gameContext.getChildren().addAll(cardList);
    }

    @Override
    public void dispose() {
        
    }

    private static double computeCardBoxHeight(Dimension2D gameDimension2D, int nbLines) {
        return gameDimension2D.getHeight() / nbLines;
    }

    private static double computeCardBoxWidth(Dimension2D gameDimension2D, int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

    private static double computeCardHeight(double boxHeight) {
        if ((boxHeight / zoom_factor) < minHeight) {
            return minHeight;
        } else {
            return boxHeight / zoom_factor;
        }
    }

    private List<slidingpuzzlecard> createCards(Configuration config) {

        final double boxHeight = computeCardBoxHeight(gameDimension2D, nbLines);
        final double boxWidth = computeCardBoxWidth(gameDimension2D, nbColumns);

        final double cardHeight = 200;
        final double cardWidth = 200;

        final int fixationlength = config.getFixationLength();

        List<slidingpuzzlecard> result = new ArrayList<>();
        int counter=1;
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                
                double positionX = computePositionX(boxWidth, cardWidth, j);
                double positionY = computePositionY(boxHeight, cardHeight, i);
                log.info("index" + counter);
                log.info("position x" + positionX);
                log.info("position y" + positionY);
                log.info("width" + cardWidth);
                log.info("height" + cardHeight);
                slidingpuzzlecard card = new slidingpuzzlecard(counter, positionX, positionY, cardWidth, cardHeight,"data/tiles/tile1.png", fixationlength, gameContext, this, stats);
                counter++;
                result.add(card);

            }

        }
        Collections.shuffle(result);
        return result;
    }

    private static double computeCardWidth(double cardHeight) {
        return cardHeight * cardRatio;
    }

    private static double computePositionX(double cardBoxWidth, double cardWidth, int colIndex) {
        return (cardBoxWidth/3) + (colIndex * 200);
    }

    private static double computePositionY(double cardboxHeight, double cardHeight, int rowIndex) {
        return  (rowIndex * 200) ;
    }
}

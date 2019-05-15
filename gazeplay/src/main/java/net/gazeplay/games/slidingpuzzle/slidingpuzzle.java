/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import java.util.List;
import javafx.geometry.Dimension2D;
import javafx.scene.text.Text;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.math101.Card;

/**
 *
 * @author Peter Bardawil
 */
public class slidingpuzzle implements GameLifeCycle {

    public slidingpuzzle(Stats stats, GameContext gameContext, int nbLines, int nbColumns) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        
    }
    
    private static final float cardRatio = 0.8f;
    private static final float zoom_factor = 1.16f;
    private static final int minHeight = 30;
    
    private final Stats stats;
    private final GameContext gameContext;

    private final int nbLines;
    
    private final int nbColumns;
    
    private javafx.geometry.Dimension2D gameDimension2D;
    
    @Override
    public void launch() {
        final Configuration config = Configuration.getInstance();
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
    private List<Card> createCards(Configuration config, String operator) {
    
    }
    private static double computeCardWidth(double cardHeight) {
        return cardHeight * cardRatio;
    }

    private static double computePositionX(double cardBoxWidth, double cardWidth, int colIndex) {
        return ((cardBoxWidth - cardWidth) / 2) + (colIndex * cardBoxWidth);
    }

    private static double computePositionY(double cardboxHeight, double cardHeight, int rowIndex) {
        return (cardboxHeight - cardHeight) / 2 + (rowIndex * cardboxHeight) / zoom_factor;
    }
}

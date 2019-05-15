/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.math101.Math101;

/**
 *
 * @author Peter Bardawil
 */

public class slidingpuzzlecard extends Parent {

    public slidingpuzzlecard(double fixationlength, Rectangle card, Image image, Text text, GameContext gameContext, double initWidth, double initHeight, double initX, double initY, slidingpuzzle gameInstance, ProgressIndicator progressIndicator, Stats stats, EventHandler<Event> enterEvent) {
        this.fixationlength = fixationlength;
        this.card = card;
        this.image = image;
        this.text = text;
        this.gameContext = gameContext;
        this.initWidth = initWidth;
        this.initHeight = initHeight;
        this.initX = initX;
        this.initY = initY;
        this.gameInstance = gameInstance;
        this.progressIndicator = progressIndicator;
        this.stats = stats;
        this.enterEvent = enterEvent;
    }
    private static final float zoom_factor = 1.05f;

    private final double fixationlength;

    private final Rectangle card;
    
    private final Image image; // REPLACE IMAGE WITH TEXT
   
    private final Text text;
    
    private final GameContext gameContext;

    private final double initWidth;
    private final double initHeight;

    private final double initX;
    private final double initY;

    private final slidingpuzzle gameInstance;
    
    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    
    final Stats stats;

    final EventHandler<Event> enterEvent;

    /**
     * Use a comme Timeline object so we can stop the current animation to prevent overlapses.
     */
    private Timeline currentTimeline;
}

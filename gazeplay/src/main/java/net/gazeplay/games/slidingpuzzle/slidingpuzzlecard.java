/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.math101.Math101;

/**
 *
 * @author Peter Bardawil
 */

public class slidingpuzzlecard extends Parent {

    public slidingpuzzlecard(int id, double positionX, double positionY, double width, double height, String fileName,
            double fixationlength, GameContext gameContext, slidingpuzzle gameInstance, Stats stats) {
        this.fixationlength = fixationlength;
        this.CardId = id;
        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image(fileName), 0, 0, 1, 1, true));
        this.gameContext = gameContext;
        this.initWidth = width;
        this.initHeight = height;
        this.initX = positionX;
        this.initY = positionY;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);
        this.getChildren().add(this.card);
        // this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        // this.addEventFilter(MouseEvent.ANY, enterEvent);
        // this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Prevent null pointer exception
        currentTimeline = new Timeline();
    }

    private static final float zoom_factor = 1.05f;

    private final double fixationlength;

    private final Rectangle card;

    private final int CardId;

    private final GameContext gameContext;

    private final double initWidth;
    private final double initHeight;

    private final double initX;
    private final double initY;

    private final slidingpuzzle gameInstance;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;

    final Stats stats;

    // final EventHandler<Event> enterEvent;

    /**
     * Use a comma Timeline object so we can stop the current animation to prevent overlapses.
     */
    private Timeline currentTimeline;

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    /*
     * private EventHandler<Event> buildEvent() {
     * 
     * return new EventHandler<Event>() ; }
     */
}

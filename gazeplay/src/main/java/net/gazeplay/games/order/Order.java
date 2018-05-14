/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package net.gazeplay.games.order;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
public class Order implements GameLifeCycle {
    private final GameContext gameContext;
    private final Stats stats;
    private final Spawner sp;
    // private final ProgressIndicator progressIndicator;
    private int currentNum;
    private final int nbTarget;

    public Order(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.sp = new Spawner(gameContext.getRandomPositionGenerator(), stats, gameContext);
        this.currentNum = 0;
        this.nbTarget = 5;

        // this.progressIndicator = createProgressIndicator(100.0, 100.0);
        // this.gameContext.getChildren().add(this.progressIndicator);

    }

    @Override
    public void launch() {
        sp.spawn(nbTarget, this);
        this.stats.notifyNewRoundReady();
    }

    public void enter(int num, Target t) {
        Order gameInstance = this;
        /*
         * progressIndicator.setOpacity(1); progressIndicator.setProgress(0);
         * 
         * timelineProgressBar = new Timeline();
         * 
         * timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength), new
         * KeyValue(progressIndicator.progressProperty(), 1)));
         * 
         * timelineProgressBar.play(); timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
         * 
         * @Override public void handle(ActionEvent actionEvent) { gameInstance.nbTurnedCards =
         * gameInstance.nbTurnedCards + 1; turned = true; card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
         * 
         * for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
         * gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = id; } progressIndicator.setOpacity(0);
         * 
         * } });
         */

        if (this.currentNum == num - 1) {
            stats.incNbGoals();
            this.gameContext.getChildren().remove(t);
            this.currentNum++;
        }

        if (this.currentNum == nbTarget) {
            gameContext.playWinTransition(30, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    gameInstance.dispose();
                    gameContext.clear();
                    gameInstance.launch();
                }
            });
        }
    }

    /*
     * private ProgressIndicator createProgressIndicator(double width, double height) { ProgressIndicator indicator =
     * new ProgressIndicator(0); indicator.setTranslateX(card.getX() + width * 0.05);
     * indicator.setTranslateY(card.getY() + height * 0.2); indicator.setMinWidth(width * 0.9);
     * indicator.setMinHeight(width * 0.9); indicator.setOpacity(0); return indicator; }
     */

    @Override
    public void dispose() {
        this.gameContext.getChildren().removeAll();
    }
}

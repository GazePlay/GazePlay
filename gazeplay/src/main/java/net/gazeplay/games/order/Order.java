/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package net.gazeplay.games.order;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
@Slf4j
public class Order implements GameLifeCycle {
    private final GameContext gameContext;
    private final Stats stats;
    private final Spawner sp;
    private int currentNum;
    private final int nbTarget;

    public Order(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.sp = new Spawner(gameContext.getRandomPositionGenerator(), stats, gameContext);
        this.currentNum = 0;
        this.nbTarget = 3;
    }

    @Override
    public void launch() {
        sp.spawn(nbTarget, this);
        this.stats.notifyNewRoundReady();
    }

    public void enter(Target t) {
        if (this.currentNum == t.getNum() - 1) {
            success(t);
        } else {
            fail(t);
        }

        if (this.currentNum == nbTarget) {
            gameContext.playWinTransition(20, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Order.this.restart();
                }
            });
        }
    }

    private void success(Target t) {
        stats.incNbGoals();

        Rectangle r = new Rectangle(t.getPos().getX(), t.getPos().getY(), 150, 150);
        r.setFill(new ImagePattern(new Image("data/order/images/success.png"), 0, 0, 1, 1, true));
        this.gameContext.getChildren().add(r);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                Order.this.gameContext.getChildren().remove(r);
            }
        });

        this.gameContext.getChildren().remove(t);
        pause.play();
        this.currentNum++;
    }

    private void fail(Target t) {
        stats.incNbGoals();

        Rectangle r = new Rectangle(t.getPos().getX(), t.getPos().getY(), 150, 150);
        r.setFill(new ImagePattern(new Image("data/order/images/fail.png"), 0, 0, 1, 1, true));
        this.gameContext.getChildren().add(r);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                Order.this.gameContext.getChildren().remove(r);
                Order.this.restart();
            }
        });

        this.gameContext.getChildren().remove(t);
        pause.play();
    }

    private void restart() {
        this.dispose();
        this.launch();
    }

    @Override
    public void dispose() {
        this.currentNum = 0;
        gameContext.clear();
    }
}

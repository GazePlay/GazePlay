/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.order;

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
    private int currentNum;

    public Order(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.sp = new Spawner(gameContext.getRandomPositionGenerator(), stats, gameContext);
        this.currentNum = 0;
    }

    @Override
    public void launch() {
        sp.spawn(5, this);
    }

    public void enter(int num, Target t, int x, int y) {
        if (this.currentNum == num - 1) {
            this.gameContext.getChildren().remove(t);
            this.currentNum++;
        }
    }

    @Override
    public void dispose() {
        this.gameContext.getChildren().removeAll();
    }
}

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

    public Order(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.sp = new Spawner(gameContext.getRandomPositionGenerator(), stats, gameContext);
    }

    @Override
    public void launch() {
        sp.spawn(5);
    }

    @Override
    public void dispose() {
        this.gameContext.getChildren().removeAll();
    }
}

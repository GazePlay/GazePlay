package net.gazeplay.games.TowerDefense;

import javafx.animation.AnimationTimer;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import java.util.ArrayList;

public class TowerDefense implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;

    ArrayList<Enemy> enemies;
    ResizableCanvas canvas;
    Map map;

    TowerDefense(final IGameContext gameContext, final Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        enemies = new ArrayList<>();
        map = new Map(1);
        canvas = new ResizableCanvas(map.getMap(), enemies);
        gameContext.getRoot().getChildren().add(canvas);

        gameContext.getRoot().heightProperty().addListener((observableValue, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            map.setScreenHeight(newVal.doubleValue());
        });

        gameContext.getRoot().widthProperty().addListener((observableValue, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            map.setScreenWidth(newVal.doubleValue());
        });
    }

    @Override
    public void launch() {

        gameContext.start();
        stats.notifyNewRoundReady();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long l) {

                //// UPDATE

                // Spawn enemies
                if(enemies.isEmpty()){
                    enemies.add(new Enemy(map));
                }

                // Enemies movement
                for (Enemy enemy : enemies) {
                    enemy.move();
                }

                //// RENDER
                canvas.draw();
            }
        };

        gameLoop.start();

    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

}

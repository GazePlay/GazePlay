package net.gazeplay.games.rushHour;

import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.DefaultGamesLocator;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;

@Slf4j
public class RushHour extends Parent implements GameLifeCycle {

    public Pane city;
    public GameContext gameContext;

    public RushHour(GameContext gameContext) {
        this.gameContext = gameContext;
        city = new Pane();
        setLevel(0);

        gameContext.getChildren().add(city);
    }

    public void setLevel(int i) {

        Car red = new Car(1, 2, Color.RED, true, city, 50, gameContext);
        red.setX(0);
        red.setY(0);
        city.getChildren().add(red);

    }

    @Override
    public void launch() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}

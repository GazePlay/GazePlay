package net.gazeplay.games.magicPotions;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
import java.util.concurrent.locks.Condition;


@Slf4j
public class MagicPotions extends Parent implements GameLifeCycle {

    private final GameContext gameContext;
//    private final Color red = Color.RED;
//    private final Color yellow = Color.YELLOW;
//    private final Color blue = Color.BLUE;
    private final Potion potions[];

    private StackPane potionsOnTable;

   // private int fixationLength;
    private final Client client;

    private boolean potionMixAchieved = false;

    private final Stats stats;

    public MagicPotions(GameContext gameContext ,Stats stats ){
        this.gameContext = gameContext;
        this.stats = stats;
        this.potions = new Potion[3];
        potions[1] = new Potion(0,0,5,10,
                new Image("data/potions/images/potion1.png"),"red",this.gameContext,this.stats,Configuration.getInstance().getFixationLength());

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle imageRec = new Rectangle(0,0, (int)dimension2D.getWidth(),(int) dimension2D.getHeight());
        imageRec.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRec.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRec.setFill(new ImagePattern(new Image("data/potions/images/background-potions.jpg")));

        client = new Client(gameContext,new Image("data/potions/images/Biboule-Client.png"));

        this.potionsOnTable = new StackPane();
        this.getChildren().addAll(potions);

        int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;
        imageRec.setOpacity(1 - coef* 0.9);

        gameContext.getChildren().add(imageRec);
        gameContext.getChildren().add(this);


    }
    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}

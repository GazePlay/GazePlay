package net.gazeplay.games.oddshape;

import javafx.scene.shape.Circle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
import java.util.ArrayList;

public class OddShapeGame implements GameLifeCycle {


    private final IGameContext gameContext;
    private final Stats stats;

    //constructeur du jeu
    //rien à rajouter, c'est tout ce qu'il te faut
    public OddShapeGame(final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
    }



    //méthode pour démarrer le jeu
    @Override
    public void launch() {

    }

    //méthode pour dispose (?)
    @Override
    public void dispose() {


    }

    //wip
    //à remplacer avec des formes randoms quand tout sera fonctionnel
   public Rectangle createSquare(){
        return new Rectangle(50,50);
   }

   public
}

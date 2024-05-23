package net.gazeplay.games.oddshape;

import java.util.ArrayList;

public enum OddShapes {
    RECTANGLE,
    CIRCLE,
    TRIANGLE,
    CARRE;

    //fonction pour récupérer toutes les valeurs
    //car values() renvoie un Array, et on ne peut le convertir qu'en List
    //donc on construit un ArrayList nous-mêmes
    public static ArrayList<OddShapes> getAllShapes(){
        ArrayList<OddShapes> allShapes = new ArrayList<>();
        for(OddShapes shape : OddShapes.values()){
            allShapes.add(shape);
        }
        return allShapes;
    }
}

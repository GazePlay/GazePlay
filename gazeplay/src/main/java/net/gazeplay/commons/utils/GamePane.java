/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.commons.utils;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.Getter;

/**
 * This is a pane to represent a game choice at the home menu.
 * @author thomas
 */
public class GamePane extends BorderPane{
    
    @Getter
    private final Label gameLabel;
    
    public GamePane(String gameName) {
        // The bottom of the main pane
        HBox bottomBox = new HBox();

        // Add the game title
        this.gameLabel = new Label(gameName);
        bottomBox.getChildren().add(gameLabel);
        
        this.setBottom(bottomBox);
        
        // TODO : add some css
    }
}

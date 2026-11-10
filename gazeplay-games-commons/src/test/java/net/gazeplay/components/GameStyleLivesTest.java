package net.gazeplay.components;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The lives shared by the reviewed games (maintainer's request of 10/11/2026): a red heart for each life left, an
 * empty one for each life lost, in a row, smaller when it would be too wide.
 */
@ExtendWith(ApplicationExtension.class)
class GameStyleLivesTest {

    @Test
    void shouldDrawARedHeartForEachLifeLeftAndAnEmptyOneForEachLifeLost() {
        final Group lives = GameStyle.lives(2, 3, 60, 1000, false);
        assertEquals(3, lives.getChildren().size());
        assertEquals(GameStyle.LIFE, ((SVGPath) lives.getChildren().get(0)).getFill());
        assertEquals(GameStyle.LIFE, ((SVGPath) lives.getChildren().get(1)).getFill());
        assertEquals(GameStyle.LIFE_LOST, ((SVGPath) lives.getChildren().get(2)).getFill());
        // (The icons themselves, a full heart and an empty one, are in gazeplay-core: not read by these tests)
        assertEquals(GameStyle.LIFE_LOST_ON_DARK, ((SVGPath) GameStyle.lives(0, 3, 60, 1000, true).getChildren()
            .get(0)).getFill());
        assertTrue(lives.isMouseTransparent());
    }

    @Test
    void shouldShowMoreThanFiveLivesAsANumberAndAGreenBar() {
        // Five lives: still hearts
        assertEquals(5, GameStyle.lives(5, 5, 60, 1000, false).getChildren().size());
        // Fifteen: a heart, "10 / 15", the back of the bar and the green bar, two thirds long
        final Group lives = GameStyle.lives(10, 15, 60, 1000, true);
        assertEquals(4, lives.getChildren().size());
        assertEquals("10 / 15", ((javafx.scene.text.Text) lives.getChildren().get(1)).getText());
        final javafx.scene.shape.Rectangle back = (javafx.scene.shape.Rectangle) lives.getChildren().get(2);
        final javafx.scene.shape.Rectangle bar = (javafx.scene.shape.Rectangle) lives.getChildren().get(3);
        assertEquals(javafx.scene.paint.Color.web(GameStyle.SUCCESS_COLOR), bar.getFill());
        assertEquals(back.getWidth() * 10 / 15, bar.getWidth(), 1e-9);
        assertEquals(back.getX(), bar.getX(), 1e-9);
        // No life left: an empty bar
        assertEquals(0, ((javafx.scene.shape.Rectangle) GameStyle.lives(0, 10, 60, 1000, true).getChildren().get(3))
            .getWidth(), 1e-9);
    }

    @Test
    void shouldPutTheHeartsInARowSmallerWhenItWouldBeTooWide() {
        final Group lives = GameStyle.lives(3, 3, 60, 1000, false);
        assertEquals(0, lives.getChildren().get(0).getLayoutX(), 1e-9);
        assertEquals(66, lives.getChildren().get(1).getLayoutX(), 1e-9);
        assertEquals(132, lives.getChildren().get(2).getLayoutX(), 1e-9);
        final Group many = GameStyle.lives(5, 5, 60, 200, false);
        double right = 0;
        for (final Node heart : many.getChildren()) {
            right = Math.max(right, heart.getLayoutX());
        }
        // 5 hearts of 200 / 5.5 pixels, the last one starting 4 hearts and 4 gaps from the left
        assertEquals(4 * 1.1 * 200 / 5.5, right, 1e-9);
    }
}

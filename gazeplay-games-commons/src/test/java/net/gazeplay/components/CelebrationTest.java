package net.gazeplay.components;

import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The reward of a game won (27/11/2026, tester's feedback point 69): confetti falling, two Biboules jumping beside the
 * free zone, over after its length, nothing left; no Biboule without room on the sides.
 */
@ExtendWith(ApplicationExtension.class)
class CelebrationTest {

    @Test
    void shouldRainConfettiAndMakeTwoBiboulesJumpThenEnd() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final Celebration celebration = new Celebration(1600, 900);
            assertFalse(celebration.isVisible());
            celebration.start(new WritableImage(500, 400), 400, 100, 850);
            assertTrue(celebration.isRunning());
            assertTrue(celebration.isVisible());
            assertEquals(2, celebration.bibouleCount());
            assertEquals(Celebration.CONFETTI, celebration.confettiCount());
            assertEquals(850, celebration.getBibouleBottom(), 1e-9);
            final javafx.scene.Node paper = celebration.getChildren().get(2);
            final double before = paper.getLayoutY();
            celebration.step(Celebration.JUMP / 2);
            assertTrue(paper.getLayoutY() > before, "the confetti fall");
            assertTrue(celebration.getChildren().get(0).getTranslateY() < 0, "the first Biboule jumps");
            celebration.step(Celebration.LENGTH);
            assertFalse(celebration.isRunning());
            assertFalse(celebration.isVisible());
            assertTrue(celebration.getChildren().isEmpty());
            celebration.step(1);
            assertFalse(celebration.isRunning());
            return null;
        }).get();
    }

    @Test
    void shouldShowNoBibouleWithoutRoomOnTheSides() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final Celebration celebration = new Celebration(800, 900);
            celebration.start(new WritableImage(500, 400), 10, 100, 850);
            assertEquals(0, celebration.bibouleCount());
            assertEquals(Celebration.CONFETTI, celebration.confettiCount());
            celebration.stop();
            assertFalse(celebration.isVisible());
            return null;
        }).get();
    }
}

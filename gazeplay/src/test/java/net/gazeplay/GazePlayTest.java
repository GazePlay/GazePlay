package net.gazeplay;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class GazePlayTest {

    @Test
    void canFindTheApplicationIcon() {
        GazePlay gazePlay = new GazePlay();

        String iconUrl = "data/common/images/gazeplayicone.png";
        Image icon = gazePlay.findApplicationIcon(iconUrl);

        assert(icon != null);
    }

    @Test
    void returnsNullWhenCannotFindTheApplicationIcon() {
        GazePlay gazePlay = new GazePlay();

        String iconUrl = "wrong/path";
        Image icon = gazePlay.findApplicationIcon(iconUrl);

        assert(icon == null);
    }
}
package net.gazeplay.commons.utils;

import javafx.application.Platform;
import net.gazeplay.TestingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
class FixationSequenceTest {

    private final List<List<FixationPoint>> fixationPoints =
        new ArrayList<>(List.of(
            new LinkedList<>(List.of(
                new FixationPoint(123, 200, 20, 40),
                new FixationPoint(123, 200, 500, 400),
                new FixationPoint(123, 200, 120, 150),
                new FixationPoint(123, 50, 300, 450),
                new FixationPoint(123, 50, 300, 400),
                new FixationPoint(123, 50, 300, 400),
                new FixationPoint(123, 50, 300, 400))
            ),
            new LinkedList<>(List.of(
                new FixationPoint(123, 200, 20, 40),
                new FixationPoint(123, 200, 500, 400),
                new FixationPoint(123, 200, 120, 150),
                new FixationPoint(123, 50, 300, 450),
                new FixationPoint(123, 50, 300, 400),
                new FixationPoint(123, 50, 300, 400),
                new FixationPoint(123, 50, 300, 400))
            ))
        );

    @Test
    void shouldCreateFixationSequence() throws InterruptedException {
        Platform.runLater(() -> {
            FixationSequence sequence = new FixationSequence(1920, 1080, fixationPoints, FixationSequence.MOUSE_FIXATION_SEQUENCE);
            assertEquals(3, sequence.getSequence().size());
            assertEquals(1080, sequence.getImage().heightProperty().get());
            assertEquals(1920, sequence.getImage().widthProperty().get());
        });
        TestingUtils.waitForRunLater();

        Platform.runLater(() -> {
            FixationSequence sequence = new FixationSequence(1920, 1080, fixationPoints, FixationSequence.GAZE_FIXATION_SEQUENCE);
            assertEquals(3, sequence.getSequence().size());
            assertEquals(1080, sequence.getImage().heightProperty().get());
            assertEquals(1920, sequence.getImage().widthProperty().get());
        });
        TestingUtils.waitForRunLater();
    }

    @Test
    void shouldSaveImageToFile() throws InterruptedException {
        Platform.runLater(() -> {
            File testFile = new File("image.png");
            FixationSequence sequence = new FixationSequence(1920, 1080, fixationPoints, FixationSequence.MOUSE_FIXATION_SEQUENCE);
            sequence.saveToFile(testFile);

            assertTrue(testFile.isFile());
            testFile.delete();
        });
        TestingUtils.waitForRunLater();

        Platform.runLater(() -> {
            File testFile = new File("image.png");
            FixationSequence sequence = new FixationSequence(1920, 1080, fixationPoints, FixationSequence.GAZE_FIXATION_SEQUENCE);
            sequence.saveToFile(testFile);

            assertTrue(testFile.isFile());
            testFile.delete();
        });
        TestingUtils.waitForRunLater();
    }
}

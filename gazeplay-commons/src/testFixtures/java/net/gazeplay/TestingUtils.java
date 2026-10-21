package net.gazeplay;

import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Semaphore;

import static java.nio.file.StandardOpenOption.APPEND;

public class TestingUtils {

    public static MouseEvent clickOnTarget(EventTarget target) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY,
            1, false, false, false, false, false,
            false, false, false, false, false,
            new PickResult(target, 0, 0));
    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }

    public static void writeElementsInCSV(File file, List<String> listOfElements) throws IOException {
        try (
            OutputStream fileOutputStream = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, APPEND);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))
        ) {
            bw.write(listOfElements.get(0));
            for (int i = 1; i < listOfElements.size(); i++) {
                bw.write("\n" + listOfElements.get(i));
            }
        }
    }

}

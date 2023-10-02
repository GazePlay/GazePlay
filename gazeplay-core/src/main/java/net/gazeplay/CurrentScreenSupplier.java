package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.screen.PrimaryScreenSupplier;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class CurrentScreenSupplier implements Supplier<Screen> {

    @NonNull
    private final GazePlay gazePlay;

    private final PrimaryScreenSupplier primaryScreenSupplier = new PrimaryScreenSupplier();

    @Override
    public Screen get() {
        if (gazePlay.getPrimaryScene() == null) {
            log.warn("primaryScene is null, using primary screen dimension");
            return primaryScreenSupplier.get();
        }

        double x;
        double y;
        Window window = gazePlay.getPrimaryScene().getWindow();
        if (window == null) {
            log.warn("window is null");
            Stage primaryStage = gazePlay.getPrimaryStage();
            if (primaryStage == null) {
                log.warn("primaryStage is null, using primary screen dimension");
                return primaryScreenSupplier.get();
            }
            x = primaryStage.getX() + 1;
            y = primaryStage.getY() + 1;
        } else {
            x = window.getX() + 1;
            y = window.getY() + 1;
        }

        log.debug("window position : x = {}, y = {}", x, y);
        ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(x, y, x, y);
        if (screensForRectangle.isEmpty()) {
            return primaryScreenSupplier.get();
        }
        Screen screen = screensForRectangle.get(0);
        //log.info("current screen = {}", screen);
        return screen;
    }
}

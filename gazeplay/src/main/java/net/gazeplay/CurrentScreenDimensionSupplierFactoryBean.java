package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.screen.PrimaryScreenSupplier;
import net.gazeplay.commons.utils.screen.ScreenDimensionSupplier;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component("currentScreenDimensionSupplier")
public class CurrentScreenDimensionSupplierFactoryBean implements FactoryBean<Supplier<Dimension2D>> {

    @Autowired
    @Setter
    private GazePlay gazePlay;

    @Override
    public Supplier<Dimension2D> getObject() {
        return new CachingSupplier<>(
            new ScreenDimensionSupplier(
                new CurrentScreenSupplier(gazePlay)
            ),
            2, TimeUnit.SECONDS
        );
    }

    @Override
    public Class<?> getObjectType() {
        return Supplier.class;
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class CurrentScreenSupplier implements Supplier<Screen> {

        @NonNull
        private final GazePlay gazePlay;

        private final PrimaryScreenSupplier primaryScreenSupplier = new PrimaryScreenSupplier();

        @Override
        public Screen get() {
            log.warn("get()");
            if (gazePlay.getPrimaryScene() == null) {
                log.warn("primaryScene is null");
                return primaryScreenSupplier.get();
            }
            //double x = gazePlay.primaryScene.getX();
            //double y = gazePlay.primaryScene.getY();

            double x;
            double y;

            Window window = gazePlay.getPrimaryScene().getWindow();
            if (window == null) {
                log.warn("window is null");
                Stage primaryStage = gazePlay.getPrimaryStage();
                if (primaryStage == null) {
                    log.warn("primaryStage is null");
                    return primaryScreenSupplier.get();
                }
                x = primaryStage.getX() + 1;
                y = primaryStage.getY() + 1;
            } else {
                x = window.getX();
                y = window.getY();
            }

            log.info("x = {}, y = {}", x, y);
            ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(x, y, x, y);
            log.info("screensForRectangle.size() = {}", screensForRectangle.size());
            if (screensForRectangle.isEmpty()) {
                return primaryScreenSupplier.get();
            }
            Screen screen = screensForRectangle.get(0);
            log.info("current screen = {}", screen);
            return screen;
        }
    }

}

package net.gazeplay;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@AllArgsConstructor
@Slf4j
public class GamePanelDimensionProvider {

    private final Supplier<Pane> paneSupplier;

    private final Supplier<Scene> sceneSupplier;

    public Dimension2D getDimension2D() {
        final Pane pane = paneSupplier.get();
        Dimension2D result = new Dimension2D(pane.getWidth(), pane.getHeight());
        log.debug("result = {}", result);

        // on the first round, the pane size may not be ready yet
        // in which case we get a width = 0 and heigth = 0
        // until this is fixed, we need a fallback method that may not be perfect, but does the trick for the moment

        // fallback method
        if (result.getWidth() == 0 || result.getHeight() == 0) {
            final Scene scene = sceneSupplier.get();
            result = new Dimension2D(scene.getWidth(), scene.getHeight() * 0.9 - 24);
            log.debug("result = {}", result);
        }

        return result;
    }

}

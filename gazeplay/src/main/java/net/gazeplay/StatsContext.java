package net.gazeplay;

import com.sun.glass.ui.Screen;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatsContext extends GraphicalContext {

    public static StatsContext newInstance(GazePlay gazePlay) {
        Group root = new Group();

        final Screen screen = Screen.getScreens().get(0);
        log.info("Screen size: {} x {}", screen.getWidth(), screen.getHeight());

        Scene scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.BLACK);
        return new StatsContext(gazePlay, root, scene);
    }

    private StatsContext(GazePlay gazePlay, Group root, Scene scene) {
        super(gazePlay, root, scene);
    }
}

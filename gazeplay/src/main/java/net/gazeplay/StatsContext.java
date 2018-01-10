package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.CssUtil;

@Slf4j
public class StatsContext extends GraphicalContext<Group> {

    public static StatsContext newInstance(GazePlay gazePlay) {
        Group root = new Group();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

        return new StatsContext(gazePlay, root, scene);
    }

    private StatsContext(GazePlay gazePlay, Group root, Scene scene) {
        super(gazePlay, root, scene);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}

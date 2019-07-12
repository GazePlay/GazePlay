package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import net.gazeplay.commons.utils.stats.Stats;

public class ScanpathView extends GraphicalContext<BorderPane> {

    // private Stats stats;

    public static ScanpathView newInstance(GazePlay gazePlay, Stats stats) {
        BorderPane root = new BorderPane();
        return new ScanpathView(gazePlay, root, stats);
    }

    private ScanpathView(GazePlay gazePlay, BorderPane root, Stats stats) {
        super(gazePlay, root);
        // this.stats = stats;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}

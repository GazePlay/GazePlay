package net.gazeplay;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;

@Data
@Slf4j
public class UserProfilContext extends GraphicalContext<BorderPane> {

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public static UserProfilContext newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        return new UserProfilContext(gazePlay, games, root, config);
    }

    public UserProfilContext(GazePlay gazePlay, List<GameSpec> games, BorderPane root, Configuration config) {
        super(gazePlay, root);
    }

}
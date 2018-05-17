package net.gazeplay.games.room;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ExplorationGamesStats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoomStats extends ExplorationGamesStats {
    public RoomStats(Scene scene) {
        super(scene);
        this.gameName = "room";
    }
}
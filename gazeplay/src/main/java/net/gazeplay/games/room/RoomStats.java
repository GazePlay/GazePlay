package net.gazeplay.games.room;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.ExplorationGamesStats;

@Slf4j
public class RoomStats extends ExplorationGamesStats {
    public RoomStats(Scene scene) {
        super(scene);
        this.gameName = "room";
    }
}
package net.gazeplay.games.colors;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

@Slf4j
public class GazeProgressIndicator extends AbstractGazeIndicator {

    public GazeProgressIndicator(IGameContext gameContext, double width, double height) {
        super(gameContext);

        this.setMinWidth(width);
        this.setMinHeight(height);
    }
}

package net.gazeplay.games.colors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GazeProgressIndicator extends AbstractGazeIndicator {

    public GazeProgressIndicator(double width, double height) {
        super();

        this.setMinWidth(width);
        this.setMinHeight(height);
    }
}

package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

/**
 * Size of the window, instead of the full screen: {@code --width} and {@code --height}, in pixels.
 */
@Data
public class SizeOptions {

    @CommandLine.Option(names = {"-gw", "--width"})
    private int gameWidth;


    @CommandLine.Option(names = {"-gh", "--height"})
    private int gameHeight;

}

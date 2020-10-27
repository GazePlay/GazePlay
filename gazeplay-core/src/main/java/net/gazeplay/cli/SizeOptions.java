package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class SizeOptions {

    @CommandLine.Option(names = {"-gw", "--width"})
    private int gameWidth;


    @CommandLine.Option(names = {"-gh", "--height"})
    private int gameHeight;

}

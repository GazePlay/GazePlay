package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

/**
 * All the options of the command line, also given to the JVM started for each game.
 */
@Data
public class ReusableOptions {

    @CommandLine.ArgGroup(exclusive = true)
    private UserSelectionOptions userSelectionOptions = new UserSelectionOptions();

    @CommandLine.ArgGroup(exclusive = true)
    private GameSelectionOptions gameSelectionOptions = new GameSelectionOptions();

    @CommandLine.ArgGroup(exclusive = true)
    private VariantSelectionOptions variantSelectionOptions = new VariantSelectionOptions();

    @CommandLine.ArgGroup(exclusive = true)
    private ReplayJsonFileOptions replayJsonFileOptions = new ReplayJsonFileOptions();

    @CommandLine.ArgGroup(exclusive = false)
    private SizeOptions sizeOptions = new SizeOptions();

}

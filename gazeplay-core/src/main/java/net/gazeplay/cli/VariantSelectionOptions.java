package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class VariantSelectionOptions {

    @CommandLine.Option(names = {"-v", "--variant"})
    private String gameVariant;

    @CommandLine.Option(names = {"--afsrgazeplay"}, defaultValue = "true")
    private boolean afsrGazeplay;

    @CommandLine.Option(names = {"--bera"}, defaultValue = "true")
    private boolean bera;

    @CommandLine.Option(names = {"--emmanuel"}, defaultValue = "true")
    private boolean emmanuel;
}

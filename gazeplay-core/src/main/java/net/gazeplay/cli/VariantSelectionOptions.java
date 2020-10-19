package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class VariantSelectionOptions {

    @CommandLine.Option(names = {"-v", "--variant"})
    private String gameVariant;

}

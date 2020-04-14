package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class ReusableOptions {

    @CommandLine.ArgGroup(exclusive = true)
    private UserSelectionOptions userSelectionOptions = new UserSelectionOptions();

    @CommandLine.ArgGroup(exclusive = true)
    private GameSelectionOptions gameSelectionOptions = new GameSelectionOptions();


}

package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class GameSelectionOptions {

    @CommandLine.Option(names = {"-g", "--game"})
    private String gameNameCode;

    @CommandLine.Option(names = {"--random-game"}, defaultValue = "false")
    private boolean randomGame;

}

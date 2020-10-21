package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class ReplayJsonFileOptions {

    @CommandLine.Option(names = {"-json", "--replayJsonFile"})
    private String jsonFileName;

}

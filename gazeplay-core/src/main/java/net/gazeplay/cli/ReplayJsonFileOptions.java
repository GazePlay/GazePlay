package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

/**
 * Replay of a game: {@code --replayJsonFile <path of a replayData.json file>}, with {@code --game}.
 */
@Data
public class ReplayJsonFileOptions {

    @CommandLine.Option(names = {"-json", "--replayJsonFile"})
    private String jsonFileName;

}

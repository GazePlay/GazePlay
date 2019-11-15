package net.gazeplay;

import lombok.Data;
import picocli.CommandLine;

@Data
public class ReusableOptions {

    @CommandLine.Option(names = {"-u", "--user"})
    private String userid;

    @CommandLine.Option(names = {"-g", "--game"})
    private String gameNameCode;

}

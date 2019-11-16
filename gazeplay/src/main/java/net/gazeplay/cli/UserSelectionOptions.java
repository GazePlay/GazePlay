package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
public class UserSelectionOptions {

    @CommandLine.Option(names = {"-u", "--user"})
    private String userid;

    @CommandLine.Option(names = {"--default-user"}, defaultValue = "false")
    private boolean defaultUser;

}

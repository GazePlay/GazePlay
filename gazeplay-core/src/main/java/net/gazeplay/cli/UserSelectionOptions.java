package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

/**
 * Profile to use, without showing the user selection page: {@code --user <name>} (created if missing), or
 * {@code --default-user}.
 */
@Data
public class UserSelectionOptions {

    @CommandLine.Option(names = {"-u", "--user"})
    private String userid;

    @CommandLine.Option(names = {"--default-user"}, defaultValue = "false")
    private boolean defaultUser;

}

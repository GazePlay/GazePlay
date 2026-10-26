package net.gazeplay;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.cli.ReusableOptions;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * The command line of GazePlay (picocli): its options (see {@link ReusableOptions}) are given to the JavaFX
 * application, which is then launched until its window is closed.
 */
@Slf4j
@CommandLine.Command(name = "main", mixinStandardHelpOptions = true)
public class MainCommand implements Callable<Integer> {

    private final CompositionRoot compositionRoot;

    @CommandLine.ArgGroup(exclusive = false)
    private ReusableOptions options;

    public MainCommand(final CompositionRoot compositionRoot) {
        this.compositionRoot = compositionRoot;
    }

    @Override
    public Integer call() {
        GazePlayFxApp.setCompositionRoot(compositionRoot);
        GazePlayFxApp.setApplicationOptions(options);
        Application.launch(GazePlayFxApp.class, (String[]) null);
        return 0;
    }

}

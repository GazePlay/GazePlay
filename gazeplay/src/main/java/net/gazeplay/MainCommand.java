package net.gazeplay;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.cli.ReusableOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@Slf4j
@Component
@CommandLine.Command(name = "main", mixinStandardHelpOptions = true)
public class MainCommand implements Callable<Integer> {

    @Autowired
    private ApplicationContext applicationContext;

    @CommandLine.ArgGroup(exclusive = false)
    private ReusableOptions options;

    @Override
    public Integer call() {
        GazePlayFxApp.setApplicationContext(applicationContext);
        GazePlayFxApp.setApplicationOptions(options);
        //
        Application.launch(GazePlayFxApp.class, (String[]) null);
        //
        return 0;
    }

}

package net.gazeplay;

import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import java.util.List;
import java.util.concurrent.Callable;

@Component
public class GazePlayCommandLineRunner implements CommandLineRunner, ExitCodeGenerator {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GamesLocator gamesLocator;

    @Autowired
    private Translator translator;

    @Autowired
    private GameMenuController gameMenuController;

    @Autowired
    private GazePlay gazeplay;

    private final MainCommand command;

    private final IFactory factory; // auto-configured to inject PicocliSpringFactory

    private int exitCode;

    public GazePlayCommandLineRunner(MainCommand command, IFactory factory) {
        this.command = command;
        this.factory = factory;
    }

    @Override
    public void run(String... args) {

        CommandLine commandLine = new CommandLine(command, factory);
        //
        commandLine.addSubcommand("hello", applicationContext.getBean(HelloCommand.class));
        //
        List<GameSpec> gameSpecs = gamesLocator.listGames(translator);
        for (GameSpec gameSpec : gameSpecs) {
            commandLine.addSubcommand(gameSpec.getGameSummary().getNameCode(), new GameRunnerCommand(gameSpec));
        }
        //
        exitCode = commandLine.execute(args);
    }

    @CommandLine.Command
    @RequiredArgsConstructor
    class GameRunnerCommand implements Callable<Integer> {

        private final GameSpec gameSpec;

        @Override
        public Integer call() throws Exception {
            gameMenuController.chooseGame(gazeplay, gameSpec, null);
            return 0;
        }

    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}

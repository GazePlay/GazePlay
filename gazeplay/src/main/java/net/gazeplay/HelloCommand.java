package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@Slf4j
@Component
@CommandLine.Command(name = "hello", mixinStandardHelpOptions = true)
public class HelloCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        log.info("Hello World");
        return 0;
    }

}

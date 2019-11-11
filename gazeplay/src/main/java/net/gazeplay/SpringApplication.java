package net.gazeplay;

import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.gameslocator.CachingGamesLocator;
import net.gazeplay.gameslocator.DefaultGamesLocator;
import net.gazeplay.gameslocator.GamesLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringApplication {

    @Bean
    public GamesLocator gamesLocator() {
        return new CachingGamesLocator(new DefaultGamesLocator());
    }

    @Bean
    public GazeDeviceManager gazeDeviceManager() {
        return GazeDeviceManagerFactory.getInstance().createNewGazeListener();
    }

}

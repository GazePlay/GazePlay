package net.gazeplay;

import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.gameslocator.CachingGamesLocator;
import net.gazeplay.gameslocator.CatalogBasedGamesLocator;
import net.gazeplay.gameslocator.GamesLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringApplication {

    @Bean
    public GamesLocator gamesLocator() {
        return new CachingGamesLocator(new CatalogBasedGamesLocator());
    }

    @Bean
    public Translator translator() {
        net.gazeplay.commons.configuration.Configuration config = ActiveConfigurationContext.getInstance();
        final Multilinguism multilinguism = Multilinguism.getSingleton();
        return new DefaultTranslator(config, multilinguism);
    }

}

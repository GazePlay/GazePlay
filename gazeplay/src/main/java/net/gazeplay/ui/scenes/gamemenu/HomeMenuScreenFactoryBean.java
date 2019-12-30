package net.gazeplay.ui.scenes.gamemenu;

import net.gazeplay.GazePlay;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.gameslocator.GamesLocator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HomeMenuScreenFactoryBean implements FactoryBean<HomeMenuScreen> {

    @Autowired
    private GazePlay gazePlay;

    @Autowired
    private GazeDeviceManagerFactory gazeDeviceManagerFactory;

    @Autowired
    private GameMenuFactory gameMenuFactory;

    @Autowired
    private GamesLocator gamesLocator;

    @Override
    public HomeMenuScreen getObject() {
        return new HomeMenuScreen(gazePlay, gazeDeviceManagerFactory.get(), gameMenuFactory, gamesLocator);
    }

    @Override
    public Class<HomeMenuScreen> getObjectType() {
        return HomeMenuScreen.class;
    }

}

package net.gazeplay;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.Getter;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.soundsmanager.SoundsManagerFactory;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.screen.ScreenDimensionSupplier;
import net.gazeplay.commons.utils.screen.ScreenPositionSupplier;
import net.gazeplay.gameslocator.CachingGamesLocator;
import net.gazeplay.gameslocator.CatalogBasedGamesLocator;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import net.gazeplay.ui.scenes.gamemenu.GameMenuFactory;
import net.gazeplay.ui.scenes.gamemenu.HomeMenuScreen;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.ingame.GameContextFactory;
import net.gazeplay.ui.scenes.userselect.UserProfileContext;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Builds the components of the application and wires them together, in one place.
 * <p>
 * Replaces the Spring Boot application context, which was only used to locate these few components. Long-lived
 * components are created once; the scenes are created each time they are displayed, as the former Spring prototype
 * beans were.
 */
public class CompositionRoot implements GazePlay.Screens {

    @Getter
    private final Translator translator;

    @Getter
    private final GamesLocator gamesLocator;

    @Getter
    private final GazePlay gazePlay;

    @Getter
    private final GameMenuController gameMenuController;

    private final GameMenuFactory gameMenuFactory;

    private final SoundsManagerFactory soundsManagerFactory;

    @Getter
    private final Supplier<GameContext> gameContextFactory;

    /**
     * Creates and connects the components of the application (translator, games catalogue, menus, game screen...).
     */
    public CompositionRoot() {
        translator = new DefaultTranslator(ActiveConfigurationContext.getInstance(), MultilinguismFactory.getSingleton());
        gamesLocator = new CachingGamesLocator(new CatalogBasedGamesLocator());

        gazePlay = new GazePlay(translator, gamesLocator, this);
        final CurrentScreenSupplier currentScreenSupplier = new CurrentScreenSupplier(gazePlay);
        final Supplier<Dimension2D> currentScreenDimensionSupplier =
            new CachingSupplier<>(new ScreenDimensionSupplier(currentScreenSupplier), 2, TimeUnit.SECONDS);
        final Supplier<Point2D> currentScreenPositionSupplier =
            new CachingSupplier<>(new ScreenPositionSupplier(currentScreenSupplier), 2, TimeUnit.SECONDS);
        gazePlay.setCurrentScreenDimensionSupplier(currentScreenDimensionSupplier);

        final GazeDeviceManagerFactory gazeDeviceManagerFactory =
            new GazeDeviceManagerFactory(currentScreenDimensionSupplier, currentScreenPositionSupplier);
        soundsManagerFactory = new SoundsManagerFactory();
        gameContextFactory = new GameContextFactory(gazePlay, translator, gazeDeviceManagerFactory, soundsManagerFactory);
        gameMenuController = new GameMenuController(gameContextFactory);
        gameMenuFactory = new GameMenuFactory(gameMenuController);
    }

    @Override
    public HomeMenuScreen newHomeMenuScreen() {
        return new HomeMenuScreen(gazePlay, soundsManagerFactory.get(), gameMenuFactory, gamesLocator);
    }

    @Override
    public ConfigurationContext newConfigurationContext() {
        return new ConfigurationContext(gazePlay);
    }

    @Override
    public UserProfileContext newUserProfileContext() {
        return new UserProfileContext(gazePlay);
    }
}

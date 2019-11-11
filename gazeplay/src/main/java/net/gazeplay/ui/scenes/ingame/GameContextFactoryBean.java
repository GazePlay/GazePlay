package net.gazeplay.ui.scenes.ingame;

import net.gazeplay.GazePlay;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.Translator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GameContextFactoryBean implements FactoryBean<GameContext> {

    @Autowired
    private GazePlay gazePlay;

    @Autowired
    private Translator translator;

    @Autowired
    private GazeDeviceManager gazeDeviceManager;

    @Override
    public GameContext getObject() {
        return GameContext.newInstance(
            gazePlay,
            gazePlay.getPrimaryStage(),
            gazePlay.getPrimaryScene(),
            translator,
            gazeDeviceManager
        );
    }

    @Override
    public Class<GameContext> getObjectType() {
        return GameContext.class;
    }

}

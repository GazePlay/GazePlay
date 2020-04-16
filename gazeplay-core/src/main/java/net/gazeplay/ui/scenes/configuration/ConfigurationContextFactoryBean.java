package net.gazeplay.ui.scenes.configuration;

import net.gazeplay.GazePlay;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConfigurationContextFactoryBean implements FactoryBean<ConfigurationContext> {

    @Autowired
    private GazePlay gazePlay;

    @Override
    public ConfigurationContext getObject() {
        return new ConfigurationContext(gazePlay);
    }

    @Override
    public Class<ConfigurationContext> getObjectType() {
        return ConfigurationContext.class;
    }

}

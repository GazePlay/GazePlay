package net.gazeplay.ui.scenes.userselect;

import net.gazeplay.GazePlay;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserProfilContextFactoryBean implements FactoryBean<UserProfilContext> {

    @Autowired
    private GazePlay gazePlay;

    @Override
    public UserProfilContext getObject() {
        return new UserProfilContext(gazePlay);
    }

    @Override
    public Class<UserProfilContext> getObjectType() {
        return UserProfilContext.class;
    }

}

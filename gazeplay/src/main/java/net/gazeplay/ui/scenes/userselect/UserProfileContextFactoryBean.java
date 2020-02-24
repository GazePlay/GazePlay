package net.gazeplay.ui.scenes.userselect;

import net.gazeplay.GazePlay;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserProfileContextFactoryBean implements FactoryBean<UserProfileContext> {

    @Autowired
    private GazePlay gazePlay;

    @Override
    public UserProfileContext getObject() {
        return new UserProfileContext(gazePlay);
    }

    @Override
    public Class<UserProfileContext> getObjectType() {
        return UserProfileContext.class;
    }

}

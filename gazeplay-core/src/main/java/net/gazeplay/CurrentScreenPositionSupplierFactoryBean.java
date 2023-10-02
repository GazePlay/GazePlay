package net.gazeplay;

import javafx.geometry.Point2D;
import lombok.Setter;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.screen.ScreenPositionSupplier;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component("currentScreenPositionSupplier")
public class CurrentScreenPositionSupplierFactoryBean implements FactoryBean<Supplier<Point2D>> {

    @Autowired
    @Setter
    private GazePlay gazePlay;

    @Override
    public Supplier<Point2D> getObject() {
        return new CachingSupplier<>(
            new ScreenPositionSupplier(
                new CurrentScreenSupplier(gazePlay)
            ),
            2, TimeUnit.SECONDS
        );
    }

    @Override
    public Class<?> getObjectType() {
        return Supplier.class;
    }

}

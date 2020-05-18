package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.screen.PrimaryScreenSupplier;
import net.gazeplay.commons.utils.screen.ScreenDimensionSupplier;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component("currentScreenDimensionSupplier")
public class CurrentScreenDimensionSupplierFactoryBean implements FactoryBean<Supplier<Dimension2D>> {

    @Autowired
    private GazePlay gazePlay;

    @Override
    public Supplier<Dimension2D> getObject() {
        return new CachingSupplier<>(
            new ScreenDimensionSupplier(
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

package net.gazeplay.commons.utils.screen;

import org.springframework.stereotype.Component;

@Component
public class PrimaryScreenDimensionSupplier extends ScreenDimensionSupplier {
    public PrimaryScreenDimensionSupplier() {
        super(new PrimaryScreenSupplier());
    }
}

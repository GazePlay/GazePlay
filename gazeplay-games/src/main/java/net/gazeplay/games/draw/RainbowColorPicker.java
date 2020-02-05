package net.gazeplay.games.draw;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RainbowColorPicker implements ColorPicker {

    private final List<Color> rainbow = createRainbow();

    public RainbowColorPicker() {
        createRainbow();
    }

    private List<Color> createRainbow() {
        final List<Color> rainbow = new ArrayList<>();
        rainbow.add(Color.web("#9400D3"));
        rainbow.add(Color.web("#4B0082"));
        rainbow.add(Color.web("#0000FF"));
        rainbow.add(Color.web("#00FF00"));
        rainbow.add(Color.web("#FFFF00"));
        rainbow.add(Color.web("#FF7F00"));
        rainbow.add(Color.web("#FF0000"));
        return rainbow;
    }

    private final AtomicInteger currentColorIndex = new AtomicInteger(0);

    @Override
    public Color pickColor() {
        int index = currentColorIndex.incrementAndGet();
        if (index >= rainbow.size()) {
            index = 0;
            currentColorIndex.set(0);
        }
        return rainbow.get(index);
    }

}

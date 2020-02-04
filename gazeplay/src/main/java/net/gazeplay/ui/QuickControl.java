package net.gazeplay.ui;

import javafx.scene.control.Slider;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class QuickControl {

    @Getter
    private static final QuickControl instance = new QuickControl();

    protected static final double ICON_SIZE = 64;

    public static final double CONTENT_SPACING = 5;

    public static final int PREF_HEIGHT = 120;


    public static final double SLIDERS_PREF_WIDTH = ICON_SIZE * 2d;

    public static final double SLIDERS_MIN_WIDTH = SLIDERS_PREF_WIDTH;

    public static final double SLIDERS_MAX_WIDTH = SLIDERS_PREF_WIDTH;

    public Slider createVolumeSlider() {
        final Slider slider = new Slider();
        slider.setMinWidth(SLIDERS_MIN_WIDTH);
        slider.setMaxWidth(SLIDERS_PREF_WIDTH);
        slider.setPrefWidth(SLIDERS_MAX_WIDTH);
        slider.setMin(0);
        slider.setMax(1);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.25);
        slider.setSnapToTicks(true);
        return slider;
    }

}

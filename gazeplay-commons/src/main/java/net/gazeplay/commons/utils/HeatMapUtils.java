package net.gazeplay.commons.utils;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.Utils;
import org.tc33.jheatchart.HeatChart;

import java.io.File;

@Slf4j
public class HeatMapUtils {

    private static final String heatMapFileName = "java-heat-chart.png";

    public static void buildHeatMap(double[][] data) {

        log.info(String.format("Heatmap size: %3d X %3d", data[0].length, data.length));

        // Step 1: Create our heat map chart using our data.
        HeatChart map = new HeatChart(data);

        map.setHighValueColour(java.awt.Color.RED);
        map.setLowValueColour(java.awt.Color.lightGray);

        map.setShowXAxisValues(false);
        map.setShowYAxisValues(false);
        map.setChartMargin(0);

        // creation of temp folder if it doesn't already exist.
        (new File(Utils.getTempFolder())).mkdir();

        String heatMapPath = Utils.getTempFolder() + heatMapFileName;

        File saveFile = new File(heatMapPath);

        try {
            map.saveToFile(saveFile);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    public static String getHeatMapPath() {

        return Utils.getTempFolder() + heatMapFileName;
    }

}

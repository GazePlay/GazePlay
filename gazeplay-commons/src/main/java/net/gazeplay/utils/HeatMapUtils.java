package net.gazeplay.utils;

import lombok.extern.slf4j.Slf4j;
import org.tc33.jheatchart.HeatChart;
import utils.games.Utils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class HeatMapUtils {

    private static final String heatMapFileName = "java-heat-chart.png";

    public static void buildHeatMap(double[][] data) {

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
        } catch (IOException e) {
            log.error("Exception", e);
        }
    }

    public static String getHeatMapPath() {

        return Utils.getTempFolder() + heatMapFileName;
    }

}

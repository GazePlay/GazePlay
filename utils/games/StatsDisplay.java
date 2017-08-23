package utils.games;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;

public class StatsDisplay {

    static LineChart<String,Number> buildLineChart(Stats stats) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<String,Number> lineChart =
                new LineChart<String,Number>(xAxis,yAxis);

       // lineChart.setTitle("Réaction");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        //series.setName("Temps de réaction");

        XYChart.Series average = new XYChart.Series();
       // average.setName("Moyenne");

        XYChart.Series sdp = new XYChart.Series();
        //sdp.setName("Moyenne");

        XYChart.Series sdm = new XYChart.Series();
        //sdm.setName("Moyenne");

      //  xAxis.setLabel("Lancer");
      //  yAxis.setLabel("ms");

        //populating the series with data

        ArrayList<Integer> shoots = stats.getShoots();

        double sd = stats.getSD();

        int i = 0;

        average.getData().add(new XYChart.Data(0+"", stats.getAverageLength()));
        sdp.getData().add(new XYChart.Data(0+"", stats.getAverageLength()+sd));
        sdm.getData().add(new XYChart.Data(0+"", stats.getAverageLength()-sd));

        for(Integer I: shoots){

            i++;
            series.getData().add(new XYChart.Data(i+"", I.intValue()));
            average.getData().add(new XYChart.Data(i+"", stats.getAverageLength()));

            sdp.getData().add(new XYChart.Data(i+"", stats.getAverageLength()+sd));
            sdm.getData().add(new XYChart.Data(i+"", stats.getAverageLength()-sd));
        }

        i++;
        average.getData().add(new XYChart.Data(i+"", stats.getAverageLength()));
        sdp.getData().add(new XYChart.Data(i+"", stats.getAverageLength()+sd));
        sdm.getData().add(new XYChart.Data(i+"", stats.getAverageLength()-sd));

        lineChart.setCreateSymbols(false);

        lineChart.getData().add(average);
        lineChart.getData().add(sdp);
        lineChart.getData().add(sdm);
        lineChart.getData().add(series);


        series.getNode().setStyle("-fx-stroke-width: 3; -fx-stroke: red; -fx-stroke-dash-offset:5;");
        average.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: lightgreen;");
        sdp.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");
        sdm.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");

        return lineChart;
    }
}

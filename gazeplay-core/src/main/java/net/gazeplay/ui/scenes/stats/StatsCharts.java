package net.gazeplay.ui.scenes.stats;

import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.ChiData;
import net.gazeplay.commons.utils.stats.Stats;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The charts and the table of the advanced mode of the statistics screen (refonte of 16/11/2026), in the colors of
 * the theme: their lines and axes have style classes ({@code gp-stats-chart}, {@code gp-series-*}, menus.css), so that
 * they read as well in the light theme as in the dark one. Each chart has its legend under it.
 */
final class StatsCharts {

    /**
     * The risks of the columns of the chi-squared table, as given by {@code ChiReport.decisions}.
     */
    private static final List<String> RISKS = List.of("50 %", "25 %", "10 %", "5 %", "1 %", "0,5 %");

    private StatsCharts() {
    }

    /**
     * @return the duration of each round in seconds, with the average and the average plus and minus the standard
     *   deviation (population, ÷ n), and its legend
     */
    static VBox durations(final Stats stats, final Translator translator) {
        final List<Long> durations = StatsFigures.durations(stats);
        final double average = stats.computeRoundsDurationAverageDuration() / 1000.0;
        final double sd = stats.computeRoundsDurationStandardDeviation() / 1000.0;
        final LineChart<Number, Number> chart = lineChart(durations.size(), translator.translate("StatsAxisSeconds"));
        final XYChart.Series<Number, Number> values = new XYChart.Series<>();
        for (int i = 0; i < durations.size(); i++) {
            values.getData().add(new XYChart.Data<>(i + 1, durations.get(i) / 1000.0));
        }
        addWithLevelLines(chart, values, durations.size(), average, sd);
        return withLegend(chart, legend(translator, "StatsLegendDuration", "StatsLegendAverage", "StatsLegendSD"));
    }

    /**
     * @return the level reached at each round, with its average and standard deviation, and its legend
     */
    static VBox levels(final Stats stats, final Translator translator) {
        final List<Long> levels = stats.getLevelsRounds();
        final LineChart<Number, Number> chart = lineChart(levels.size(), translator.translate("StatsAxisLevel"));
        final XYChart.Series<Number, Number> values = new XYChart.Series<>();
        for (int i = 0; i < levels.size(); i++) {
            values.getData().add(new XYChart.Data<>(i + 1, levels.get(i)));
        }
        addWithLevelLines(chart, values, levels.size(), stats.getLevelsReport().computeAverageLevel(),
            stats.getLevelsReport().computeSD());
        return withLegend(chart, legend(translator, "StatsLegendLevel", "StatsLegendAverage", "StatsLegendSD"));
    }

    /**
     * @return the horizontal and vertical positions of the fixations over the game (seconds since the first one), and
     *   its legend
     */
    static VBox positions(final Stats stats, final Translator translator) {
        final List<FixationPoint> points = StatsFigures.fixations(stats).stream()
            .sorted(Comparator.comparingLong(FixationPoint::getTimeGaze)).toList();
        final long start = points.isEmpty() ? 0 : points.get(0).getTimeGaze();
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(translator.translate("StatsAxisTime"));
        xAxis.setForceZeroInRange(true);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(translator.translate("StatsAxisPixels"));
        final LineChart<Number, Number> chart = styled(new LineChart<>(xAxis, yAxis));
        final XYChart.Series<Number, Number> horizontal = new XYChart.Series<>();
        final XYChart.Series<Number, Number> vertical = new XYChart.Series<>();
        // The fixation points hold the vertical coordinate in "x" and the horizontal one in "y" (FixationPoint)
        for (final FixationPoint point : points) {
            final double time = (point.getTimeGaze() - start) / 1000.0;
            horizontal.getData().add(new XYChart.Data<>(time, point.getY()));
            vertical.getData().add(new XYChart.Data<>(time, point.getX()));
        }
        chart.getData().add(horizontal);
        chart.getData().add(vertical);
        horizontal.getNode().getStyleClass().add("gp-series-x");
        vertical.getNode().getStyleClass().add("gp-series-y");
        final HBox legend = new HBox(20, legendItem("gp-series-x", translator.translate("StatsLegendHorizontal")),
            legendItem("gp-series-y", translator.translate("StatsLegendVertical")));
        legend.setAlignment(Pos.CENTER);
        return withLegend(chart, legend);
    }

    /**
     * @return the chi-squared test of each block of rounds: its level, the value observed, and for each risk whether
     *   the answers are not due to chance (the observed value reaches the critical value)
     */
    static VBox chi2(final Stats stats, final Translator translator, final Locale locale) {
        final GridPane table = new GridPane();
        table.getStyleClass().add("gp-stats-table");
        table.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        final List<String> heads = new java.util.ArrayList<>(List.of(translator.translate("StatsChiBlock"),
            translator.translate("StatsChiLevel"), translator.translate("StatsChiObserved")));
        heads.addAll(RISKS);
        for (int column = 0; column < heads.size(); column++) {
            final Label head = new Label(heads.get(column));
            head.getStyleClass().add("gp-stats-table-head");
            head.setMaxWidth(Double.MAX_VALUE);
            table.add(head, column, 0);
            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setHgrow(Priority.SOMETIMES);
            constraints.setHalignment(column == 0 ? HPos.LEFT : HPos.CENTER);
            table.getColumnConstraints().add(constraints);
        }
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        int row = 1;
        for (final ChiData data : stats.getChiReport().createData()) {
            table.add(cell(String.valueOf(data.getId() + 1)), 0, row);
            table.add(cell(String.valueOf(data.getLevel())), 1, row);
            table.add(cell(format.format(data.getChiObs())), 2, row);
            final List<String> decisions = List.of(data.getAlpha50(), data.getAlpha25(), data.getAlpha10(),
                data.getAlpha5(), data.getAlpha1(), data.getAlpha05());
            for (int risk = 0; risk < decisions.size(); risk++) {
                final boolean good = "good".equals(decisions.get(risk));
                final Label decision = cell(good ? "✓" : "–");
                decision.getStyleClass().add(good ? "gp-stats-good" : "gp-stats-muted");
                table.add(decision, 3 + risk, row);
            }
            row++;
        }
        final Label note = new Label(translator.translate("StatsChiNote"));
        note.getStyleClass().add("gp-stats-note");
        note.setWrapText(true);
        final VBox box = new VBox(12, table, note);
        box.setFillWidth(true);
        return box;
    }

    private static Label cell(final String text) {
        final Label cell = new Label(text);
        cell.getStyleClass().add("gp-stats-table-cell");
        cell.setMaxWidth(Double.MAX_VALUE);
        return cell;
    }

    /**
     * @return a chart of values per round (1 to {@code count} along the bottom)
     */
    private static LineChart<Number, Number> lineChart(final int count, final String yLabel) {
        final int upper = Math.max(count, 2);
        final NumberAxis xAxis = new NumberAxis(1, upper, Math.max(1, Math.ceil(upper / 10.0)));
        xAxis.setMinorTickVisible(false);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        yAxis.setForceZeroInRange(true);
        return styled(new LineChart<>(xAxis, yAxis));
    }

    private static LineChart<Number, Number> styled(final LineChart<Number, Number> chart) {
        chart.getStyleClass().add("gp-stats-chart");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        chart.setMinSize(0, 0);
        return chart;
    }

    /**
     * Adds the values, over the average and the average plus and minus the standard deviation drawn as straight lines.
     * The style classes are given once every series is in the chart: adding a series resets the classes of the
     * series added before it (seen on 16/11/2026, their default colors came back).
     */
    private static void addWithLevelLines(final LineChart<Number, Number> chart,
                                          final XYChart.Series<Number, Number> values, final int count,
                                          final double average, final double sd) {
        final int upper = Math.max(count, 2);
        final XYChart.Series<Number, Number> mean = straight(1, upper, average);
        final XYChart.Series<Number, Number> above = straight(1, upper, average + sd);
        final XYChart.Series<Number, Number> below = straight(1, upper, Math.max(0, average - sd));
        chart.getData().addAll(List.of(mean, above, below, values));
        mean.getNode().getStyleClass().add("gp-series-average");
        above.getNode().getStyleClass().add("gp-series-sd");
        below.getNode().getStyleClass().add("gp-series-sd");
        values.getNode().getStyleClass().add("gp-series-main");
    }

    private static XYChart.Series<Number, Number> straight(final double from, final double to, final double value) {
        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(from, value));
        series.getData().add(new XYChart.Data<>(to, value));
        return series;
    }

    private static FlowPane legend(final Translator translator, final String valuesKey, final String averageKey,
                                   final String sdKey) {
        final FlowPane legend = new FlowPane(20, 6, legendItem("gp-series-main", translator.translate(valuesKey)),
            legendItem("gp-series-average", translator.translate(averageKey)),
            legendItem("gp-series-sd", translator.translate(sdKey)));
        legend.setAlignment(Pos.CENTER);
        return legend;
    }

    /**
     * @return a short line in the style of a series, and its name
     */
    private static HBox legendItem(final String seriesClass, final String text) {
        final Line line = new Line(0, 0, 26, 0);
        line.getStyleClass().addAll("gp-legend-line", seriesClass);
        final Label label = new Label(text);
        label.getStyleClass().add("gp-stats-legend");
        final HBox item = new HBox(8, line, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private static VBox withLegend(final Node chart, final Node legend) {
        final VBox box = new VBox(6, chart, legend);
        VBox.setVgrow(chart, Priority.ALWAYS);
        box.setFillWidth(true);
        return box;
    }
}

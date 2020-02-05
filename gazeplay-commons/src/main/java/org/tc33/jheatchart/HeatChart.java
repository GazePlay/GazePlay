/*
 *  Copyright 2010 Tom Castle (www.tc33.org)
 *  Licensed under GNU Lesser General public  License
 *
 *  This file is part of JHeatChart - the heat maps charting api for Java.
 *
 *  JHeatChart is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General public  License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JHeatChart is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General public  License for more details.
 *
 *  You should have received a copy of the GNU Lesser General public  License
 *  along with JHeatChart.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tc33.jheatchart;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * The <code>HeatChart</code> class describes a chart which can display 3-dimensions of getLanguages - x,y and z, where
 * x and y are the usual 2-dimensional axis and z is portrayed by colour intensity. Heat charts are sometimes known as
 * heat maps.
 *
 * <p>
 * Use of this chart would typically involve 3 steps:
 * <ol>
 * <li>Construction of a new instance, providing the necessary z-getLanguages.</li>
 * <li>Configure the visual settings.</li>
 * <li>A call to either <code>getChartImage()</code> or <code>saveToFile(String)</code>.</li>
 * </ol>
 *
 * <h3>Instantiation</h3>
 * <p>
 * Construction of a new <code>HeatChart</code> instance is through its one constructor which takes a 2-dimensional
 * array of <tt>doubles</tt> which should contain the z-getLanguages for the chart. Consider this array to be the grid
 * of getLanguages which will instead be represented as colours in the chart.
 *
 * <p>
 * Setting of the x-getLanguages and y-getLanguages which are displayed along the appropriate axis is optional, and by
 * default will simply display the getLanguages 0 to n-1, where n is the number of rows or columns. Otherwise, the x/y
 * axis getLanguages can be set with the <code>setXValues</code> and <code>setYValues
 * </code> methods. Both methods are overridden with two forms:
 *
 * <h4>Object axis getLanguages</h4>
 *
 * <p>
 * The simplest way to set the axis getLanguages is to use the methods which take an array of Object[]. This array must
 * have the same length as the number of columns for setXValues and same as the number of rows for setYValues. The
 * string representation of the objects will then be used as the axis getLanguages.
 *
 * <h4>Offset and Interval</h4>
 *
 * <p>
 * This is convenient way of defining numerical getLanguages along the axis. One of the two methods takes an interval
 * and an offset for either the x or y axis. These parameters supply the necessary information to describe the
 * getLanguages based upon the z-value indexes. The quantity of x-getLanguages and y-getLanguages is already known from
 * the lengths of the z-getLanguages array dimensions. Then the offset parameters indicate what the first value will be,
 * with the intervals providing the increment from one column or row to the next.
 *
 * <p>
 * <strong>Consider an example:</strong> <blockquote>
 *
 * <pre>
 * double[][] zValues = new double[][] { { 1.2, 1.3, 1.5 }, { 1.0, 1.1, 1.6 }, { 0.7, 0.9, 1.3 } };
 *
 * double xOffset = 1.0;
 * double yOffset = 0.0;
 * double xInterval = 1.0;
 * double yInterval = 2.0;
 *
 * chart.setXValues(xOffset, xInterval);
 * chart.setYValues(yOffset, yInterval);
 * </pre>
 *
 * </blockquote>
 *
 * <p>
 * In this example, the z-getLanguages range from 0.7 to 1.6. The x-getLanguages range from the xOffset value 1.0 to
 * 4.0, which is calculated as the number of x-getLanguages multiplied by the xInterval, shifted by the xOffset of 1.0.
 * The y-getLanguages are calculated in the same way to give a range of getLanguages from 0.0 to 6.0.
 *
 * <h3>configuration</h3>
 * <p>
 * This step is optional. By default the heat chart will be generated without a title or labels on the axis, and the
 * colouring of the heat map will be in grayscale. A large range of configuration options are available to customise the
 * chart. All customisations are available through simple accessor methods. See the javadoc of each of the methods for
 * more information.
 *
 * <h3>Output</h3>
 * <p>
 * The generated heat chart can be obtained in two forms, using the following methods:
 * <ul>
 * <li><strong>getChartImage()</strong> - The chart will be returned as a <code>BufferedImage</code> object that can be
 * used in any number of ways, most notably it can be inserted into a Swing component, for use in a GUI
 * application.</li>
 * <li><strong>saveToFile(File)</strong> - The chart will be saved to the file system at the file location specified as
 * a parameter. The images format that the images will be saved in is derived from the extension of the file name.</li>
 * </ul>
 *
 * <strong>Note:</strong> The chart images will not actually be created until either saveToFile(File) or getChartImage()
 * are called, and will be regenerated on each successive call.
 */
public class HeatChart {

    /**
     * A basic logarithmic scale value of 0.3.
     */
    public static final double SCALE_LOGARITHMIC = 0.3;

    /**
     * The linear scale value of 1.0.
     */
    public static final double SCALE_LINEAR = 1.0;

    /**
     * A basic exponential scale value of 3.0.
     */
    public static final double SCALE_EXPONENTIAL = 3;

    // x, y, z data getLanguages.
    private double[][] zValues;
    private Object[] xValues;
    private Object[] yValues;

    private boolean xValuesHorizontal;
    private boolean yValuesHorizontal;

    // General chart settings.
    private Dimension cellSize;
    private Dimension chartSize;
    private int margin;
    private Color backgroundColour;

    // Title settings.
    private String title;
    private Font titleFont;
    private Color titleColour;
    private Dimension titleSize;
    private int titleAscent;

    // Axis settings.
    private int axisThickness;
    private Color axisColour;
    private Font axisLabelsFont;
    private Color axisLabelColour;
    private String xAxisLabel;
    private String yAxisLabel;
    private Color axisValuesColour;
    private Font axisValuesFont; // The font size will be considered the maximum font size - it may be smaller if needed
    // to fit in.
    private int xAxisValuesFrequency;
    private int yAxisValuesFrequency;
    private boolean showXAxisValues;
    private boolean showYAxisValues;

    // Generated axis properties.
    private int xAxisValuesHeight;
    private int xAxisValuesWidthMax;

    private int yAxisValuesHeight;
    private int yAxisValuesAscent;
    private int yAxisValuesWidthMax;

    private Dimension xAxisLabelSize;
    private int xAxisLabelDescent;

    private Dimension yAxisLabelSize;
    private int yAxisLabelAscent;

    // Heat map colour settings.
    private Color highValueColour;
    private Color lowValueColour;

    // How many RGB steps there are between the high and low colours.
    private int colourValueDistance;

    private double lowValue;
    private double highValue;

    // Key co-ordinate positions.
    private Point heatMapTL;
    private Point heatMapBR;
    private Point heatMapC;

    // Heat map dimensions.
    private Dimension heatMapSize;

    // Control variable for mapping z-getLanguages to colours.
    private double colourScale;

    /**
     * Constructs a heatmap for the given z-getLanguages against x/y-getLanguages that by default will be the
     * getLanguages 0 to n-1, where n is the number of columns or rows.
     *
     * @param zValues the z-getLanguages, where each element is a row of z-getLanguages in the resultant heat chart.
     */
    public HeatChart(final double[][] zValues) {
        this(zValues, min(zValues), max(zValues));
    }

    /**
     * Constructs a heatmap for the given z-getLanguages against x/y-getLanguages that by default will be the
     * getLanguages 0 to n-1, where n is the number of columns or rows.
     *
     * @param zValues the z-getLanguages, where each element is a row of z-getLanguages in the resultant heat chart.
     * @param low     the minimum possible value, which may or may not appear in the z-getLanguages.
     * @param high    the maximum possible value, which may or may not appear in the z-getLanguages.
     */
    public HeatChart(final double[][] zValues, final double low, final double high) {
        this.zValues = zValues;
        this.lowValue = low;
        this.highValue = high;

        // Default x/y-value settings.
        setXValues(0, 1);
        setYValues(0, 1);

        // Default chart settings.
        this.cellSize = new Dimension(20, 20);
        this.margin = 20;
        this.backgroundColour = Color.WHITE;

        // Default title settings.
        this.title = null;
        this.titleFont = new Font("Sans-Serif", Font.BOLD, 16);
        this.titleColour = Color.BLACK;

        // Default axis settings.
        this.xAxisLabel = null;
        this.yAxisLabel = null;
        this.axisThickness = 2;
        this.axisColour = Color.BLACK;
        this.axisLabelsFont = new Font("Sans-Serif", Font.PLAIN, 12);
        this.axisLabelColour = Color.BLACK;
        this.axisValuesColour = Color.BLACK;
        this.axisValuesFont = new Font("Sans-Serif", Font.PLAIN, 10);
        this.xAxisValuesFrequency = 1;
        this.xAxisValuesHeight = 0;
        this.xValuesHorizontal = false;
        this.showXAxisValues = true;
        this.showYAxisValues = true;
        this.yAxisValuesFrequency = 1;
        this.yAxisValuesHeight = 0;
        this.yValuesHorizontal = true;

        // Default heatmap settings.
        this.highValueColour = Color.BLACK;
        this.lowValueColour = Color.WHITE;
        this.colourScale = SCALE_LINEAR;

        updateColourDistance();
    }

    /**
     * Returns the low value. This is the value at which the low value colour will be applied.
     *
     * @return the low value.
     */
    public double getLowValue() {
        return lowValue;
    }

    /**
     * Returns the high value. This is the value at which the high value colour will be applied.
     *
     * @return the high value.
     */
    public double getHighValue() {
        return highValue;
    }

    /**
     * Returns the 2-dimensional array of z-getLanguages currently in use. Each element is a double array which
     * represents one row of the heat map, or all the z-getLanguages for one y-value.
     *
     * @return an array of the z-getLanguages in current use, that is, those getLanguages which will define the colour
     * of each cell in the resultant heat map.
     */
    public double[][] getZValues() {
        return zValues;
    }

    /**
     * Replaces the z-getLanguages array. See the {@link #setZValues(double[][], double, double)} method for an example
     * of z-getLanguages. The smallest and largest getLanguages in the array are used as the minimum and maximum
     * getLanguages respectively.
     *
     * @param zValues the array to replace the current array with. The number of elements in each inner array must be
     *                identical.
     */
    public void setZValues(final double[][] zValues) {
        setZValues(zValues, min(zValues), max(zValues));
    }

    /**
     * Replaces the z-getLanguages array. The number of elements should match the number of y-getLanguages, with each
     * element containing a double array with an equal number of elements that matches the number of x-getLanguages. Use
     * this method where the minimum and maximum getLanguages possible are not contained within the dataset.
     *
     * <h2>Example</h2>
     *
     * <blockcode>
     *
     * <pre>
     * new double[][]{
     *   {1.0,1.2,1.4},
     *   {1.2,1.3,1.5},
     *   {0.9,1.3,1.2},
     *   {0.8,1.6,1.1}
     * };
     * </pre>
     *
     * </blockcode>
     * <p>
     * The above zValues array is equivalent to:
     *
     * <table border="1">
     * <tr>
     * <td rowspan="4" width="20"><center><strong>y</strong></center></td>
     * <td>1.0</td>
     * <td>1.2</td>
     * <td>1.4</td>
     * </tr>
     * <tr>
     * <td>1.2</td>
     * <td>1.3</td>
     * <td>1.5</td>
     * </tr>
     * <tr>
     * <td>0.9</td>
     * <td>1.3</td>
     * <td>1.2</td>
     * </tr>
     * <tr>
     * <td>0.8</td>
     * <td>1.6</td>
     * <td>1.1</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td colspan="3"><center><strong>x</strong></center></td>
     * </tr>
     * </table>
     *
     * @param zValues the array to replace the current array with. The number of elements in each inner array must be
     *                identical.
     * @param low     the minimum possible value, which may or may not appear in the z-getLanguages.
     * @param high    the maximum possible value, which may or may not appear in the z-getLanguages.
     */
    public void setZValues(final double[][] zValues, final double low, final double high) {
        this.zValues = zValues;
        this.lowValue = low;
        this.highValue = high;
    }

    /**
     * Sets the x-getLanguages which are plotted along the x-axis. The x-getLanguages are calculated based upon the
     * indexes of the z-getLanguages array:
     *
     * <blockcode>
     *
     * <pre>
     * x-value = x-offset + (column-index * x-interval)
     * </pre>
     *
     * </blockcode>
     *
     * <p>
     * The x-interval defines the gap between each x-value and the x-offset is applied to each value to offset them all
     * from zero.
     *
     * <p>
     * Alternatively the x-getLanguages can be set more directly with the <code>setXValues(Object[])</code> method.
     *
     * @param xOffset   an offset value to be applied to the index of each z-value element.
     * @param xInterval an interval that will separate each x-value item.
     */
    public void setXValues(final double xOffset, final double xInterval) {
        // Update the x-getLanguages according to the offset and interval.
        xValues = new Object[zValues[0].length];
        for (int i = 0; i < zValues[0].length; i++) {
            xValues[i] = xOffset + (i * xInterval);
        }
    }

    /**
     * Sets the x-getLanguages which are plotted along the x-axis. The given x-getLanguages array must be the same
     * length as the z-getLanguages array has columns. Each of the x-getLanguages elements will be displayed according
     * to their toString representation.
     *
     * @param xValues an array of elements to be displayed as getLanguages along the x-axis.
     */
    public void setXValues(final Object[] xValues) {
        this.xValues = xValues;
    }

    /**
     * Sets the y-getLanguages which are plotted along the y-axis. The y-getLanguages are calculated based upon the
     * indexes of the z-getLanguages array:
     *
     * <blockcode>
     *
     * <pre>
     * y-value = y-offset + (column-index * y-interval)
     * </pre>
     *
     * </blockcode>
     *
     * <p>
     * The y-interval defines the gap between each y-value and the y-offset is applied to each value to offset them all
     * from zero.
     *
     * <p>
     * Alternatively the y-getLanguages can be set more directly with the <code>setYValues(Object[])</code> method.
     *
     * @param yOffset   an offset value to be applied to the index of each z-value element.
     * @param yInterval an interval that will separate each y-value item.
     */
    public void setYValues(final double yOffset, final double yInterval) {
        // Update the y-getLanguages according to the offset and interval.
        yValues = new Object[zValues.length];
        for (int i = 0; i < zValues.length; i++) {
            yValues[i] = yOffset + (i * yInterval);
        }
    }

    /**
     * Sets the y-getLanguages which are plotted along the y-axis. The given y-getLanguages array must be the same
     * length as the z-getLanguages array has columns. Each of the y-getLanguages elements will be displayed according
     * to their toString representation.
     *
     * @param yValues an array of elements to be displayed as getLanguages along the y-axis.
     */
    public void setYValues(final Object[] yValues) {
        this.yValues = yValues;
    }

    /**
     * Returns the x-getLanguages which are currently set to display along the x-axis. The array that is returned is
     * either that which was explicitly set with <code>setXValues(Object[])</code> or that was generated from the offset
     * and interval that were given to <code>setXValues(double, double)</code>, in which case the object type of each
     * element will be <code>Double</code>.
     *
     * @return an array of the getLanguages that are to be displayed along the x-axis.
     */
    public Object[] getXValues() {
        return xValues;
    }

    /**
     * Returns the y-getLanguages which are currently set to display along the y-axis. The array that is returned is
     * either that which was explicitly set with <code>setYValues(Object[])</code> or that was generated from the offset
     * and interval that were given to <code>setYValues(double, double)</code>, in which case the object type of each
     * element will be <code>Double</code>.
     *
     * @return an array of the getLanguages that are to be displayed along the y-axis.
     */
    public Object[] getYValues() {
        return yValues;
    }

    /**
     * Sets whether the text of the getLanguages along the x-axis should be drawn horizontally left-to-right, or
     * vertically top-to-bottom.
     *
     * @param xValuesHorizontal true if x-getLanguages should be drawn horizontally, false if they should be drawn vertically.
     */
    public void setXValuesHorizontal(final boolean xValuesHorizontal) {
        this.xValuesHorizontal = xValuesHorizontal;
    }

    /**
     * Returns whether the text of the getLanguages along the x-axis are to be drawn horizontally left-to-right, or
     * vertically top-to-bottom.
     *
     * @return true if the x-getLanguages will be drawn horizontally, false if they will be drawn vertically.
     */
    public boolean isXValuesHorizontal() {
        return xValuesHorizontal;
    }

    /**
     * Sets whether the text of the getLanguages along the y-axis should be drawn horizontally left-to-right, or
     * vertically top-to-bottom.
     *
     * @param yValuesHorizontal true if y-getLanguages should be drawn horizontally, false if they should be drawn vertically.
     */
    public void setYValuesHorizontal(final boolean yValuesHorizontal) {
        this.yValuesHorizontal = yValuesHorizontal;
    }

    /**
     * Returns whether the text of the getLanguages along the y-axis are to be drawn horizontally left-to-right, or
     * vertically top-to-bottom.
     *
     * @return true if the y-getLanguages will be drawn horizontally, false if they will be drawn vertically.
     */
    public boolean isYValuesHorizontal() {
        return yValuesHorizontal;
    }

    /**
     * Sets the width of each individual cell that constitutes a value in x,y,z data space. By setting the cell width,
     * any previously set chart width will be overwritten with a value calculated based upon this value and the number
     * of cells in there are along the x-axis.
     *
     * @param cellWidth the new width to use for each individual data cell.
     * @deprecated As of release 0.6, replaced by {@link #setCellSize(Dimension)}
     */
    @Deprecated
    public void setCellWidth(final int cellWidth) {
        setCellSize(new Dimension(cellWidth, cellSize.height));
    }

    /**
     * Returns the width of each individual data cell that constitutes a value in the x,y,z space.
     *
     * @return the width of each cell.
     * @deprecated As of release 0.6, replaced by {@link #getCellSize}
     */
    @Deprecated
    public int getCellWidth() {
        return cellSize.width;
    }

    /**
     * Sets the height of each individual cell that constitutes a value in x,y,z data space. By setting the cell height,
     * any previously set chart height will be overwritten with a value calculated based upon this value and the number
     * of cells in there are along the y-axis.
     *
     * @param cellHeight the new height to use for each individual data cell.
     * @deprecated As of release 0.6, replaced by {@link #setCellSize(Dimension)}
     */
    @Deprecated
    public void setCellHeight(final int cellHeight) {
        setCellSize(new Dimension(cellSize.width, cellHeight));
    }

    /**
     * Returns the height of each individual data cell that constitutes a value in the x,y,z space.
     *
     * @return the height of each cell.
     * @deprecated As of release 0.6, replaced by {@link #getCellSize()}
     */
    @Deprecated
    public int getCellHeight() {
        return cellSize.height;
    }

    /**
     * Sets the size of each individual cell that constitutes a value in x,y,z data space. By setting the cell size, any
     * previously set chart size will be overwritten with a value calculated based upon this value and the number of
     * cells along each axis.
     *
     * @param cellSize the new size to use for each individual data cell.
     * @since 0.6
     */
    public void setCellSize(final Dimension cellSize) {
        this.cellSize = cellSize;
    }

    /**
     * Returns the size of each individual data cell that constitutes a value in the x,y,z space.
     *
     * @return the size of each individual data cell.
     * @since 0.6
     */
    public Dimension getCellSize() {
        return cellSize;
    }

    /**
     * Returns the width of the chart in pixels as calculated according to the cell dimensions, chart margin and other
     * size settings.
     *
     * @return the width in pixels of the chart images to be generated.
     * @deprecated As of release 0.6, replaced by {@link #getChartSize()}
     */
    @Deprecated
    public int getChartWidth() {
        return chartSize.width;
    }

    /**
     * Returns the height of the chart in pixels as calculated according to the cell dimensions, chart margin and other
     * size settings.
     *
     * @return the height in pixels of the chart images to be generated.
     * @deprecated As of release 0.6, replaced by {@link #getChartSize()}
     */
    @Deprecated
    public int getChartHeight() {
        return chartSize.height;
    }

    /**
     * Returns the size of the chart in pixels as calculated according to the cell dimensions, chart margin and other
     * size settings.
     *
     * @return the size in pixels of the chart images to be generated.
     * @since 0.6
     */
    public Dimension getChartSize() {
        return chartSize;
    }

    /**
     * Returns the String that will be used as the title of any successive calls to generate a chart.
     *
     * @return the title of the chart.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the String that will be used as the title of any successive calls to generate a chart. The title will be
     * displayed centralised horizontally at the top of any generated charts.
     *
     * <p>
     * If the title is set to <tt>null</tt> then no title will be displayed.
     *
     * <p>
     * Defaults to null.
     *
     * @param title the chart title to set.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Returns the String that will be displayed as a description of the x-axis in any generated charts.
     *
     * @return the display label describing the x-axis.
     */
    public String getXAxisLabel() {
        return xAxisLabel;
    }

    /**
     * Sets the String that will be displayed as a description of the x-axis in any generated charts. The label will be
     * displayed horizontally central of the x-axis bar.
     *
     * <p>
     * If the xAxisLabel is set to <tt>null</tt> then no label will be displayed.
     *
     * <p>
     * Defaults to null.
     *
     * @param xAxisLabel the label to be displayed describing the x-axis.
     */
    public void setXAxisLabel(final String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    /**
     * Returns the String that will be displayed as a description of the y-axis in any generated charts.
     *
     * @return the display label describing the y-axis.
     */
    public String getYAxisLabel() {
        return yAxisLabel;
    }

    /**
     * Sets the String that will be displayed as a description of the y-axis in any generated charts. The label will be
     * displayed horizontally central of the y-axis bar.
     *
     * <p>
     * If the yAxisLabel is set to <tt>null</tt> then no label will be displayed.
     *
     * <p>
     * Defaults to null.
     *
     * @param yAxisLabel the label to be displayed describing the y-axis.
     */
    public void setYAxisLabel(final String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }

    /**
     * Returns the width of the margin in pixels to be left as empty space around the heat map element.
     *
     * @return the size of the margin to be left blank around the edge of the chart.
     */
    public int getChartMargin() {
        return margin;
    }

    /**
     * Sets the width of the margin in pixels to be left as empty space around the heat map element. If a title is set
     * then half the margin will be directly above the title and half directly below it. Where axis labels are set then
     * the axis labels may sit partially in the margin.
     *
     * <p>
     * Defaults to 20 pixels.
     *
     * @param margin the new margin to be left as blank space around the heat map.
     */
    public void setChartMargin(final int margin) {
        this.margin = margin;
    }

    /**
     * Returns an object that represents the colour to be used as the background for the whole chart.
     *
     * @return the colour to be used to fill the chart background.
     */
    public Color getBackgroundColour() {
        return backgroundColour;
    }

    /**
     * Sets the colour to be used on the background of the chart. A transparent background can be set by setting a
     * background colour with an alpha value. The transparency will only be effective when the images is saved as a png
     * or gif.
     *
     * <p>
     * Defaults to <code>Color.WHITE</code>.
     *
     * @param backgroundColour the new colour to be set as the background fill.
     */
    public void setBackgroundColour(Color backgroundColour) {
        if (backgroundColour == null) {
            backgroundColour = Color.WHITE;
        }

        this.backgroundColour = backgroundColour;
    }

    /**
     * Returns the <code>Font</code> that describes the visual style of the title.
     *
     * @return the Font that will be used to render the title.
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * Sets a new <code>Font</code> to be used in rendering the chart's title String.
     *
     * <p>
     * Defaults to Sans-Serif, BOLD, 16 pixels.
     *
     * @param titleFont the Font that should be used when rendering the chart title.
     */
    public void setTitleFont(final Font titleFont) {
        this.titleFont = titleFont;
    }

    /**
     * Returns the <code>Color</code> that represents the colour the title text should be painted in.
     *
     * @return the currently set colour to be used in painting the chart title.
     */
    public Color getTitleColour() {
        return titleColour;
    }

    /**
     * Sets the <code>Color</code> that describes the colour to be used for the chart title String.
     *
     * <p>
     * Defaults to <code>Color.BLACK</code>.
     *
     * @param titleColour the colour to paint the chart's title String.
     */
    public void setTitleColour(final Color titleColour) {
        this.titleColour = titleColour;
    }

    /**
     * Returns the width of the axis bars in pixels. Both axis bars have the same thickness.
     *
     * @return the thickness of the axis bars in pixels.
     */
    public int getAxisThickness() {
        return axisThickness;
    }

    /**
     * Sets the width of the axis bars in pixels. Both axis bars use the same thickness.
     *
     * <p>
     * Defaults to 2 pixels.
     *
     * @param axisThickness the thickness to use for the axis bars in any newly generated charts.
     */
    public void setAxisThickness(final int axisThickness) {
        this.axisThickness = axisThickness;
    }

    /**
     * Returns the colour that is set to be used for the axis bars. Both axis bars use the same colour.
     *
     * @return the colour in use for the axis bars.
     */
    public Color getAxisColour() {
        return axisColour;
    }

    /**
     * Sets the colour to be used on the axis bars. Both axis bars use the same colour.
     *
     * <p>
     * Defaults to <code>Color.BLACK</code>.
     *
     * @param axisColour the colour to be set for use on the axis bars.
     */
    public void setAxisColour(final Color axisColour) {
        this.axisColour = axisColour;
    }

    /**
     * Returns the font that describes the visual style of the labels of the axis. Both axis' labels use the same font.
     *
     * @return the font used to define the visual style of the axis labels.
     */
    public Font getAxisLabelsFont() {
        return axisLabelsFont;
    }

    /**
     * Sets the font that describes the visual style of the axis labels. Both axis' labels use the same font.
     *
     * <p>
     * Defaults to Sans-Serif, PLAIN, 12 pixels.
     *
     * @param axisLabelsFont the font to be used to define the visual style of the axis labels.
     */
    public void setAxisLabelsFont(final Font axisLabelsFont) {
        this.axisLabelsFont = axisLabelsFont;
    }

    /**
     * Returns the current colour of the axis labels. Both labels use the same colour.
     *
     * @return the colour of the axis label text.
     */
    public Color getAxisLabelColour() {
        return axisLabelColour;
    }

    /**
     * Sets the colour of the text displayed as axis labels. Both labels use the same colour.
     *
     * <p>
     * Defaults to Color.BLACK.
     *
     * @param axisLabelColour the colour to use for the axis label text.
     */
    public void setAxisLabelColour(final Color axisLabelColour) {
        this.axisLabelColour = axisLabelColour;
    }

    /**
     * Returns the font which describes the visual style of the axis getLanguages. The axis getLanguages are those
     * getLanguages displayed alongside the axis bars at regular intervals. Both axis use the same font.
     *
     * @return the font in use for the axis getLanguages.
     */
    public Font getAxisValuesFont() {
        return axisValuesFont;
    }

    /**
     * Sets the font which describes the visual style of the axis getLanguages. The axis getLanguages are those
     * getLanguages displayed alongside the axis bars at regular intervals. Both axis use the same font.
     *
     * <p>
     * Defaults to Sans-Serif, PLAIN, 10 pixels.
     *
     * @param axisValuesFont the font that should be used for the axis getLanguages.
     */
    public void setAxisValuesFont(final Font axisValuesFont) {
        this.axisValuesFont = axisValuesFont;
    }

    /**
     * Returns the colour of the axis getLanguages as they will be painted along the axis bars. Both axis use the same
     * colour.
     *
     * @return the colour of the getLanguages displayed along the axis bars.
     */
    public Color getAxisValuesColour() {
        return axisValuesColour;
    }

    /**
     * Sets the colour to be used for the axis getLanguages as they will be painted along the axis bars. Both axis use
     * the same colour.
     *
     * <p>
     * Defaults to Color.BLACK.
     *
     * @param axisValuesColour the new colour to be used for the axis bar getLanguages.
     */
    public void setAxisValuesColour(final Color axisValuesColour) {
        this.axisValuesColour = axisValuesColour;
    }

    /**
     * Returns the frequency of the getLanguages displayed along the x-axis. The frequency is how many columns in the
     * x-dimension have their value displayed. A frequency of 2 would mean every other column has a value shown and a
     * frequency of 3 would mean every third column would be given a value.
     *
     * @return the frequency of the getLanguages displayed against columns.
     */
    public int getXAxisValuesFrequency() {
        return xAxisValuesFrequency;
    }

    /**
     * Sets the frequency of the getLanguages displayed along the x-axis. The frequency is how many columns in the
     * x-dimension have their value displayed. A frequency of 2 would mean every other column has a value and a
     * frequency of 3 would mean every third column would be given a value.
     *
     * <p>
     * Defaults to 1. Every column is given a value.
     *
     * @param axisValuesFrequency the frequency of the getLanguages displayed against columns, where 1 is every column and 2 is every
     *                            other column.
     */
    public void setXAxisValuesFrequency(final int axisValuesFrequency) {
        this.xAxisValuesFrequency = axisValuesFrequency;
    }

    /**
     * Returns the frequency of the getLanguages displayed along the y-axis. The frequency is how many rows in the
     * y-dimension have their value displayed. A frequency of 2 would mean every other row has a value and a frequency
     * of 3 would mean every third row would be given a value.
     *
     * @return the frequency of the getLanguages displayed against rows.
     */
    public int getYAxisValuesFrequency() {
        return yAxisValuesFrequency;
    }

    /**
     * Sets the frequency of the getLanguages displayed along the y-axis. The frequency is how many rows in the
     * y-dimension have their value displayed. A frequency of 2 would mean every other row has a value and a frequency
     * of 3 would mean every third row would be given a value.
     *
     * <p>
     * Defaults to 1. Every row is given a value.
     *
     * @param axisValuesFrequency the frequency of the getLanguages displayed against rows, where 1 is every row and 2 is every other
     *                            row.
     */
    public void setYAxisValuesFrequency(final int axisValuesFrequency) {
        yAxisValuesFrequency = axisValuesFrequency;
    }

    /**
     * Returns whether axis getLanguages are to be shown at all for the x-axis.
     *
     * <p>
     * If axis getLanguages are not shown then more space is allocated to the heat map.
     *
     * @return true if the x-axis getLanguages will be displayed, false otherwise.
     */
    public boolean isShowXAxisValues() {
        // TODO Could get rid of these flags and use a frequency of -1 to signal no getLanguages.
        return showXAxisValues;
    }

    /**
     * Sets whether axis getLanguages are to be shown at all for the x-axis.
     *
     * <p>
     * If axis getLanguages are not shown then more space is allocated to the heat map.
     *
     * <p>
     * Defaults to true.
     *
     * @param showXAxisValues true if x-axis getLanguages should be displayed, false if they should be hidden.
     */
    public void setShowXAxisValues(final boolean showXAxisValues) {
        this.showXAxisValues = showXAxisValues;
    }

    /**
     * Returns whether axis getLanguages are to be shown at all for the y-axis.
     *
     * <p>
     * If axis getLanguages are not shown then more space is allocated to the heat map.
     *
     * @return true if the y-axis getLanguages will be displayed, false otherwise.
     */
    public boolean isShowYAxisValues() {
        return showYAxisValues;
    }

    /**
     * Sets whether axis getLanguages are to be shown at all for the y-axis.
     *
     * <p>
     * If axis getLanguages are not shown then more space is allocated to the heat map.
     *
     * <p>
     * Defaults to true.
     *
     * @param showYAxisValues true if y-axis getLanguages should be displayed, false if they should be hidden.
     */
    public void setShowYAxisValues(final boolean showYAxisValues) {
        this.showYAxisValues = showYAxisValues;
    }

    /**
     * Returns the colour that is currently to be displayed for the heat map cells with the highest z-value in the
     * dataset.
     *
     * <p>
     * The full colour range will go through each RGB step between the high value colour and the low value colour.
     *
     * @return the colour in use for cells of the highest z-value.
     */
    public Color getHighValueColour() {
        return highValueColour;
    }

    /**
     * Sets the colour to be used to fill cells of the heat map with the highest z-getLanguages in the dataset.
     *
     * <p>
     * The full colour range will go through each RGB step between the high value colour and the low value colour.
     *
     * <p>
     * Defaults to Color.BLACK.
     *
     * @param highValueColour the colour to use for cells of the highest z-value.
     */
    public void setHighValueColour(final Color highValueColour) {
        this.highValueColour = highValueColour;

        updateColourDistance();
    }

    /**
     * Returns the colour that is currently to be displayed for the heat map cells with the lowest z-value in the
     * dataset.
     *
     * <p>
     * The full colour range will go through each RGB step between the high value colour and the low value colour.
     *
     * @return the colour in use for cells of the lowest z-value.
     */
    public Color getLowValueColour() {
        return lowValueColour;
    }

    /**
     * Sets the colour to be used to fill cells of the heat map with the lowest z-getLanguages in the dataset.
     *
     * <p>
     * The full colour range will go through each RGB step between the high value colour and the low value colour.
     *
     * <p>
     * Defaults to Color.WHITE.
     *
     * @param lowValueColour the colour to use for cells of the lowest z-value.
     */
    public void setLowValueColour(final Color lowValueColour) {
        this.lowValueColour = lowValueColour;

        updateColourDistance();
    }

    /**
     * Returns the scale that is currently in use to map z-value to colour. A value of 1.0 will give a
     * <strong>linear</strong> scale, which will spread the distribution of colours evenly amoungst the full range of
     * represented z-getLanguages. A value of greater than 1.0 will give an <strong>exponential</strong> scale that will
     * produce greater emphasis for the separation between higher getLanguages and a value between 0.0 and 1.0 will
     * provide a <strong>logarithmic</strong> scale, with greater separation of low getLanguages.
     *
     * @return the scale factor that is being used to map from z-value to colour.
     */
    public double getColourScale() {
        return colourScale;
    }

    /**
     * Sets the scale that is currently in use to map z-value to colour. A value of 1.0 will give a
     * <strong>linear</strong> scale, which will spread the distribution of colours evenly amoungst the full range of
     * represented z-getLanguages. A value of greater than 1.0 will give an <strong>exponential</strong> scale that will
     * produce greater emphasis for the separation between higher getLanguages and a value between 0.0 and 1.0 will
     * provide a <strong>logarithmic</strong> scale, with greater separation of low getLanguages. Values of 0.0 or less
     * are illegal.
     *
     * <p>
     * Defaults to a linear scale value of 1.0.
     *
     * @param colourScale the scale that should be used to map from z-value to colour.
     */
    public void setColourScale(final double colourScale) {
        this.colourScale = colourScale;
    }

    /*
     * Calculate and update the field for the distance between the low colour and high colour. The distance is the
     * number of steps between one colour and the other using an RGB coding with 0-255 getLanguages for each of red,
     * green and blue. So the maximum colour distance is 255 + 255 + 255.
     */
    private void updateColourDistance() {
        final int r1 = lowValueColour.getRed();
        final int g1 = lowValueColour.getGreen();
        final int b1 = lowValueColour.getBlue();
        final int r2 = highValueColour.getRed();
        final int g2 = highValueColour.getGreen();
        final int b2 = highValueColour.getBlue();

        colourValueDistance = Math.abs(r1 - r2);
        colourValueDistance += Math.abs(g1 - g2);
        colourValueDistance += Math.abs(b1 - b2);
    }

    /**
     * Generates a new chart <code>Image</code> based upon the currently held settings and then attempts to save that
     * images to disk, to the location provided as a File parameter. The images type of the saved file will equal the
     * extension of the filename provided, so it is essential that a suitable extension be included on the file name.
     *
     * <p>
     * All supported <code>ImageIO</code> file types are supported, including PNG, JPG and GIF.
     *
     * <p>
     * No chart will be generated until this or the related <code>getChartImage()</code> method are called. All
     * successive calls will result in the generation of a new chart images, no caching is used.
     *
     * @param outputFile the file location that the generated images file should be written to. The File must have a suitable
     *                   filename, with an extension of a valid images format (as supported by <code>ImageIO</code>).
     * @throws IOException if the output file's filename has no extension or if there the file is unable to written to. Reasons
     *                     for this include a non-existant file location (check with the File exists() method on the parent
     *                     directory), or the permissions of the write location may be incorrect.
     */
    public void saveToFile(final File outputFile) throws IOException {
        final String filename = outputFile.getName();

        final int extPoint = filename.lastIndexOf('.');

        if (extPoint < 0) {
            throw new IOException("Illegal filename, no extension used.");
        }

        // Determine the extension of the filename.
        final String ext = filename.substring(extPoint + 1);

        // Handle jpg without transparency.
        if (ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
            final BufferedImage chart = (BufferedImage) getChartImage(false);

            // Save our graphic.
            saveGraphicJpeg(chart, outputFile, 1.0f);
        } else {
            final BufferedImage chart = (BufferedImage) getChartImage(true);

            ImageIO.write(chart, ext, outputFile);
        }
    }

    private void saveGraphicJpeg(final BufferedImage chart, final File outputFile, final float quality) throws IOException {
        // Setup correct compression for jpeg.
        final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        final ImageWriter writer = iter.next();
        final ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);

        // Output the images.
        final FileImageOutputStream output = new FileImageOutputStream(outputFile);
        writer.setOutput(output);
        final IIOImage image = new IIOImage(chart, null, null);
        writer.write(null, image, iwp);
        writer.dispose();

    }

    /**
     * Generates and returns a new chart <code>Image</code> configured according to this object's currently held
     * settings. The given parameter determines whether transparency should be enabled for the generated images.
     *
     * <p>
     * No chart will be generated until this or the related <code>saveToFile(File)</code> method are called. All
     * successive calls will result in the generation of a new chart images, no caching is used.
     *
     * @param alpha whether to enable transparency.
     * @return A newly generated chart <code>Image</code>. The returned images is a <code>BufferedImage</code>.
     */
    public Image getChartImage(final boolean alpha) {
        // Calculate all unknown dimensions.
        measureComponents();
        updateCoordinates();

        // Determine images type based upon whether require alpha or not.
        // Using BufferedImage.TYPE_INT_ARGB seems to break on jpg.
        final int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);

        // Create our chart images which we will eventually draw everything on.
        final BufferedImage chartImage = new BufferedImage(chartSize.width, chartSize.height, imageType);
        final Graphics2D chartGraphics = chartImage.createGraphics();

        // Use anti-aliasing where ever possible.
        chartGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background.
        chartGraphics.setColor(backgroundColour);
        chartGraphics.fillRect(0, 0, chartSize.width, chartSize.height);

        // Draw the title.
        drawTitle(chartGraphics);

        // Draw the heatmap images.
        drawHeatMap(chartGraphics, zValues);

        // Draw the axis labels.
        drawXLabel(chartGraphics);
        drawYLabel(chartGraphics);

        // Draw the axis bars.
        drawAxisBars(chartGraphics);

        // Draw axis getLanguages.
        drawXValues(chartGraphics);
        drawYValues(chartGraphics);

        return chartImage;
    }

    /**
     * Generates and returns a new chart <code>Image</code> configured according to this object's currently held
     * settings. By default the images is generated with no transparency.
     *
     * <p>
     * No chart will be generated until this or the related <code>saveToFile(File)</code> method are called. All
     * successive calls will result in the generation of a new chart images, no caching is used.
     *
     * @return A newly generated chart <code>Image</code>. The returned images is a <code>BufferedImage</code>.
     */
    public Image getChartImage() {
        return getChartImage(false);
    }

    /*
     * Calculates all unknown component dimensions.
     */
    private void measureComponents() {
        // TODO This would be a good place to check that all settings have sensible getLanguages or throw illegal state
        // exception.

        // TODO Put this somewhere so it only gets created once.
        final BufferedImage chartImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D tempGraphics = chartImage.createGraphics();

        // Calculate title dimensions.
        if (title != null) {
            tempGraphics.setFont(titleFont);
            final FontMetrics metrics = tempGraphics.getFontMetrics();
            titleSize = new Dimension(metrics.stringWidth(title), metrics.getHeight());
            titleAscent = metrics.getAscent();
        } else {
            titleSize = new Dimension(0, 0);
        }

        // Calculate x-axis label dimensions.
        if (xAxisLabel != null) {
            tempGraphics.setFont(axisLabelsFont);
            final FontMetrics metrics = tempGraphics.getFontMetrics();
            xAxisLabelSize = new Dimension(metrics.stringWidth(xAxisLabel), metrics.getHeight());
            xAxisLabelDescent = metrics.getDescent();
        } else {
            xAxisLabelSize = new Dimension(0, 0);
        }

        // Calculate y-axis label dimensions.
        if (yAxisLabel != null) {
            tempGraphics.setFont(axisLabelsFont);
            final FontMetrics metrics = tempGraphics.getFontMetrics();
            yAxisLabelSize = new Dimension(metrics.stringWidth(yAxisLabel), metrics.getHeight());
            yAxisLabelAscent = metrics.getAscent();
        } else {
            yAxisLabelSize = new Dimension(0, 0);
        }

        // Calculate x-axis value dimensions.
        if (showXAxisValues) {
            tempGraphics.setFont(axisValuesFont);
            final FontMetrics metrics = tempGraphics.getFontMetrics();
            xAxisValuesHeight = metrics.getHeight();
            xAxisValuesWidthMax = 0;
            for (final Object o : xValues) {
                final int w = metrics.stringWidth(o.toString());
                if (w > xAxisValuesWidthMax) {
                    xAxisValuesWidthMax = w;
                }
            }
        } else {
            xAxisValuesHeight = 0;
        }

        // Calculate y-axis value dimensions.
        if (showYAxisValues) {
            tempGraphics.setFont(axisValuesFont);
            final FontMetrics metrics = tempGraphics.getFontMetrics();
            yAxisValuesHeight = metrics.getHeight();
            yAxisValuesAscent = metrics.getAscent();
            yAxisValuesWidthMax = 0;
            for (final Object o : yValues) {
                final int w = metrics.stringWidth(o.toString());
                if (w > yAxisValuesWidthMax) {
                    yAxisValuesWidthMax = w;
                }
            }
        } else {
            yAxisValuesHeight = 0;
        }

        // Calculate heatmap dimensions.
        final int heatMapWidth = (zValues[0].length * cellSize.width);
        final int heatMapHeight = (zValues.length * cellSize.height);
        heatMapSize = new Dimension(heatMapWidth, heatMapHeight);

        final int yValuesHorizontalSize;
        if (yValuesHorizontal) {
            yValuesHorizontalSize = yAxisValuesWidthMax;
        } else {
            yValuesHorizontalSize = yAxisValuesHeight;
        }

        final int xValuesVerticalSize;
        if (xValuesHorizontal) {
            xValuesVerticalSize = xAxisValuesHeight;
        } else {
            xValuesVerticalSize = xAxisValuesWidthMax;
        }

        // Calculate chart dimensions.
        final int chartWidth = heatMapWidth + (2 * margin) + yAxisLabelSize.height + yValuesHorizontalSize + axisThickness;
        final int chartHeight = heatMapHeight + (2 * margin) + xAxisLabelSize.height + xValuesVerticalSize + titleSize.height
            + axisThickness;
        chartSize = new Dimension(chartWidth, chartHeight);
    }

    /*
     * Calculates the co-ordinates of some key positions.
     */
    private void updateCoordinates() {
        // Top-left of heat map.
        int x = margin + axisThickness + yAxisLabelSize.height;
        x += (yValuesHorizontal ? yAxisValuesWidthMax : yAxisValuesHeight);
        int y = titleSize.height + margin;
        heatMapTL = new Point(x, y);

        // Top-right of heat map.
        x = heatMapTL.x + heatMapSize.width;
        y = heatMapTL.y + heatMapSize.height;
        heatMapBR = new Point(x, y);

        // Centre of heat map.
        x = heatMapTL.x + (heatMapSize.width / 2);
        y = heatMapTL.y + (heatMapSize.height / 2);
        heatMapC = new Point(x, y);
    }

    /*
     * Draws the title String on the chart if title is not null.
     */
    private void drawTitle(final Graphics2D chartGraphics) {
        if (title != null) {
            // Strings are drawn from the baseline position of the leftmost char.
            final int yTitle = (margin / 2) + titleAscent;
            final int xTitle = (chartSize.width / 2) - (titleSize.width / 2);

            chartGraphics.setFont(titleFont);
            chartGraphics.setColor(titleColour);
            chartGraphics.drawString(title, xTitle, yTitle);
        }
    }

    /*
     * Creates the actual heatmap element as an images, that can then be drawn onto a chart.
     */
    private void drawHeatMap(final Graphics2D chartGraphics, final double[][] data) {
        // Calculate the available size for the heatmap.
        final int noYCells = data.length;
        final int noXCells = data[0].length;

        // double dataMin = min(data);
        // double dataMax = max(data);

        final BufferedImage heatMapImage = new BufferedImage(heatMapSize.width, heatMapSize.height,
            BufferedImage.TYPE_INT_ARGB);
        final Graphics2D heatMapGraphics = heatMapImage.createGraphics();

        for (int x = 0; x < noXCells; x++) {
            for (int y = 0; y < noYCells; y++) {
                // Set colour depending on zValues.
                heatMapGraphics.setColor(getCellColour(data[y][x], lowValue, highValue));

                final int cellX = x * cellSize.width;
                final int cellY = y * cellSize.height;

                heatMapGraphics.fillRect(cellX, cellY, cellSize.width, cellSize.height);
            }
        }

        // Draw the heat map onto the chart.
        chartGraphics.drawImage(heatMapImage, heatMapTL.x, heatMapTL.y, heatMapSize.width, heatMapSize.height, null);
    }

    /*
     * Draws the x-axis label string if it is not null.
     */
    private void drawXLabel(final Graphics2D chartGraphics) {
        if (xAxisLabel != null) {
            // Strings are drawn from the baseline position of the leftmost char.
            final int yPosXAxisLabel = chartSize.height - (margin / 2) - xAxisLabelDescent;
            // TODO This will need to be updated if the y-axis getLanguages/label can be moved to the right.
            final int xPosXAxisLabel = heatMapC.x - (xAxisLabelSize.width / 2);

            chartGraphics.setFont(axisLabelsFont);
            chartGraphics.setColor(axisLabelColour);
            chartGraphics.drawString(xAxisLabel, xPosXAxisLabel, yPosXAxisLabel);
        }
    }

    /*
     * Draws the y-axis label string if it is not null.
     */
    private void drawYLabel(final Graphics2D chartGraphics) {
        if (yAxisLabel != null) {
            // Strings are drawn from the baseline position of the leftmost char.
            final int yPosYAxisLabel = heatMapC.y + (yAxisLabelSize.width / 2);
            final int xPosYAxisLabel = (margin / 2) + yAxisLabelAscent;

            chartGraphics.setFont(axisLabelsFont);
            chartGraphics.setColor(axisLabelColour);

            // Create 270 degree rotated transform.
            final AffineTransform transform = chartGraphics.getTransform();
            final AffineTransform originalTransform = (AffineTransform) transform.clone();
            transform.rotate(Math.toRadians(270), xPosYAxisLabel, yPosYAxisLabel);
            chartGraphics.setTransform(transform);

            // Draw string.
            chartGraphics.drawString(yAxisLabel, xPosYAxisLabel, yPosYAxisLabel);

            // Revert to original transform before rotation.
            chartGraphics.setTransform(originalTransform);
        }
    }

    /*
     * Draws the bars of the x-axis and y-axis.
     */
    private void drawAxisBars(final Graphics2D chartGraphics) {
        if (axisThickness > 0) {
            chartGraphics.setColor(axisColour);

            // Draw x-axis.
            int x = heatMapTL.x - axisThickness;
            int y = heatMapBR.y;
            int width = heatMapSize.width + axisThickness;
            int height = axisThickness;
            chartGraphics.fillRect(x, y, width, height);

            // Draw y-axis.
            x = heatMapTL.x - axisThickness;
            y = heatMapTL.y;
            width = axisThickness;
            height = heatMapSize.height;
            chartGraphics.fillRect(x, y, width, height);
        }
    }

    /*
     * Draws the x-getLanguages onto the x-axis if showXAxisValues is set to true.
     */
    private void drawXValues(final Graphics2D chartGraphics) {
        if (!showXAxisValues) {
            return;
        }

        chartGraphics.setColor(axisValuesColour);

        for (int i = 0; i < xValues.length; i++) {
            if (i % xAxisValuesFrequency != 0) {
                continue;
            }

            final String xValueStr = xValues[i].toString();

            chartGraphics.setFont(axisValuesFont);
            final FontMetrics metrics = chartGraphics.getFontMetrics();

            final int valueWidth = metrics.stringWidth(xValueStr);

            if (xValuesHorizontal) {
                // Draw the value with whatever font is now set.
                int valueXPos = (i * cellSize.width) + ((cellSize.width / 2) - (valueWidth / 2));
                valueXPos += heatMapTL.x;
                final int valueYPos = heatMapBR.y + metrics.getAscent() + 1;

                chartGraphics.drawString(xValueStr, valueXPos, valueYPos);
            } else {
                final int valueXPos = heatMapTL.x + (i * cellSize.width) + ((cellSize.width / 2) + (xAxisValuesHeight / 2));
                final int valueYPos = heatMapBR.y + axisThickness + valueWidth;

                // Create 270 degree rotated transform.
                final AffineTransform transform = chartGraphics.getTransform();
                final AffineTransform originalTransform = (AffineTransform) transform.clone();
                transform.rotate(Math.toRadians(270), valueXPos, valueYPos);
                chartGraphics.setTransform(transform);

                // Draw the string.
                chartGraphics.drawString(xValueStr, valueXPos, valueYPos);

                // Revert to original transform before rotation.
                chartGraphics.setTransform(originalTransform);
            }
        }
    }

    /*
     * Draws the y-getLanguages onto the y-axis if showYAxisValues is set to true.
     */
    private void drawYValues(final Graphics2D chartGraphics) {
        if (!showYAxisValues) {
            return;
        }

        chartGraphics.setColor(axisValuesColour);

        for (int i = 0; i < yValues.length; i++) {
            if (i % yAxisValuesFrequency != 0) {
                continue;
            }

            final String yValueStr = yValues[i].toString();

            chartGraphics.setFont(axisValuesFont);
            final FontMetrics metrics = chartGraphics.getFontMetrics();

            final int valueWidth = metrics.stringWidth(yValueStr);

            if (yValuesHorizontal) {
                // Draw the value with whatever font is now set.
                final int valueXPos = margin + yAxisLabelSize.height + (yAxisValuesWidthMax - valueWidth);
                final int valueYPos = heatMapTL.y + (i * cellSize.height) + (cellSize.height / 2) + (yAxisValuesAscent / 2);

                chartGraphics.drawString(yValueStr, valueXPos, valueYPos);
            } else {
                final int valueXPos = margin + yAxisLabelSize.height + yAxisValuesAscent;
                final int valueYPos = heatMapTL.y + (i * cellSize.height) + (cellSize.height / 2) + (valueWidth / 2);

                // Create 270 degree rotated transform.
                final AffineTransform transform = chartGraphics.getTransform();
                final AffineTransform originalTransform = (AffineTransform) transform.clone();
                transform.rotate(Math.toRadians(270), valueXPos, valueYPos);
                chartGraphics.setTransform(transform);

                // Draw the string.
                chartGraphics.drawString(yValueStr, valueXPos, valueYPos);

                // Revert to original transform before rotation.
                chartGraphics.setTransform(originalTransform);
            }
        }
    }

    /*
     * Determines what colour a heat map cell should be based upon the cell getLanguages.
     */
    private Color getCellColour(final double data, final double min, final double max) {
        final double range = max - min;
        final double position = data - min;

        // What proportion of the way through the possible getLanguages is that.
        final double percentPosition = position / range;

        // Which colour group does that put us in.
        final int colourPosition = getColourPosition(percentPosition);

        int r = lowValueColour.getRed();
        int g = lowValueColour.getGreen();
        int b = lowValueColour.getBlue();

        // Make n shifts of the colour, where n is the colourPosition.
        for (int i = 0; i < colourPosition; i++) {
            final int rDistance = r - highValueColour.getRed();
            final int gDistance = g - highValueColour.getGreen();
            final int bDistance = b - highValueColour.getBlue();

            if ((Math.abs(rDistance) >= Math.abs(gDistance)) && (Math.abs(rDistance) >= Math.abs(bDistance))) {
                // Red must be the largest.
                r = changeColourValue(r, rDistance);
            } else if (Math.abs(gDistance) >= Math.abs(bDistance)) {
                // Green must be the largest.
                g = changeColourValue(g, gDistance);
            } else {
                // Blue must be the largest.
                b = changeColourValue(b, bDistance);
            }
        }

        return new Color(r, g, b);
    }

    /*
     * Returns how many colour shifts are required from the lowValueColour to get to the correct colour position. The
     * result will be different depending on the colour scale used: LINEAR, LOGARITHMIC, EXPONENTIAL.
     */
    private int getColourPosition(final double percentPosition) {
        return (int) Math.round(colourValueDistance * Math.pow(percentPosition, colourScale));
    }

    private int changeColourValue(final int colourValue, final int colourDistance) {
        if (colourDistance < 0) {
            return colourValue + 1;
        } else if (colourDistance > 0) {
            return colourValue - 1;
        } else {
            // This shouldn't actually happen here.
            return colourValue;
        }
    }

    /**
     * Finds and returns the maximum value in a 2-dimensional array of doubles.
     *
     * @return the largest value in the array.
     */
    public static double max(final double[][] values) {
        double max = 0;
        for (final double[] value : values) {
            for (final double v : value) {
                max = Math.max(v, max);
            }
        }
        return max;
    }

    /**
     * Finds and returns the minimum value in a 2-dimensional array of doubles.
     *
     * @return the smallest value in the array.
     */
    public static double min(final double[][] values) {
        double min = Double.MAX_VALUE;
        for (final double[] value : values) {
            for (final double v : value) {
                min = Math.min(v, min);
            }
        }
        return min;
    }

}

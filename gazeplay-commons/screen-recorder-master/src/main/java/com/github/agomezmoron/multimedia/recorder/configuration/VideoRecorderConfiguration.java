/**
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alejandro Gómez Morón
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.agomezmoron.multimedia.recorder.configuration;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;

import com.github.agomezmoron.multimedia.recorder.VideoRecorder;

/**
 * Video recorder configuration to be used in {@link VideoRecorder}.
 * @author Alejandro Gomez <agommor@gmail.com>
 *
 */
public class VideoRecorderConfiguration {

    /**
     * Screen Width.
     */
    private static int width = getMaxWidth();

    /**
     * Screen Height.
     */
    private static int height = getMaxHeight();

    /**
     * Flag to know if the user want to keep the frames.
     */
    private static boolean keepFrames = false;

    /**
     * Flag to know if the video will be in full screen or not.
     */
    private static boolean useFullScreen = true;

    /**
     * X coordinate.
     */
    private static int x = 0;

    /**
     * y coordinate.
     */
    private static int y = 0;

    /**
     * Interval where the images will be capture (in milliseconds).
     */
    private static int captureInterval = 50;

    private static File defaultDirectory = (System.getProperty("java.io.tmpdir") != null)
            ? new File(System.getProperty("java.io.tmpdir")) : new File(".");

    /**
     * Temporal directory to be used.
     */
    private static File tempDirectory = defaultDirectory;

    /**
     * Video path where the video will be saved.
     */
    private static File videoDirectory = defaultDirectory;

    /**
     * @return the width
     */
    public static int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public static void setWidth(int width) {
        if (VideoRecorderConfiguration.width >= 0 && width <= getMaxWidth()) {
            VideoRecorderConfiguration.width = width;
            if (VideoRecorderConfiguration.width < getMaxWidth()) {
                useFullScreen = false;
            }
        }
    }

    /**
     * @return the height
     */
    public static int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public static void setHeight(int height) {
        if (VideoRecorderConfiguration.height >= 0 && height <= getMaxHeight()) {
            VideoRecorderConfiguration.height = height;
            if (VideoRecorderConfiguration.height < getMaxHeight()) {
                useFullScreen = false;
            }
        }
    }

    /**
     * @return the keepFrames
     */
    public static boolean wantToKeepFrames() {
        return keepFrames;
    }

    /**
     * @param keepFrames the keepFrames to set
     */
    public static void setKeepFrames(boolean keepFrames) {
        VideoRecorderConfiguration.keepFrames = keepFrames;
    }

    /**
     * @return the useFullScreen
     */
    public static boolean wantToUseFullScreen() {
        return useFullScreen;
    }

    /**
     * @param useFullScreen the useFullScreen to set
     */
    public static void wantToUseFullScreen(boolean useFullScreen) {
        VideoRecorderConfiguration.useFullScreen = useFullScreen;
    }

    /**
     * @return the x
     */
    public static int getX() {
        return x;
    }

    /**
     * @param x the x to set.
     */
    public static void setX(int x) {
        if (x >= 0 && x <= getMaxWidth()) {
            VideoRecorderConfiguration.x = x;
        }
    }

    /**
     * @return the y
     */
    public static int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public static void setY(int y) {
        if (y >= 0 && y <= getMaxHeight()) {
            VideoRecorderConfiguration.y = y;
        }
    }

    /**
     * @return the captureInterval
     */
    public static int getCaptureInterval() {
        return captureInterval;
    }

    /**
     * @param captureInterval the captureInterval to set
     */
    public static void setCaptureInterval(int captureInterval) {
        VideoRecorderConfiguration.captureInterval = captureInterval;
    }

    /**
     * @return the tempDirectory
     */
    public static File getTempDirectory() {
        return tempDirectory;
    }

    /**
     * @param tempDirectory the tempDirectory to set
     */
    public static void setTempDirectory(File tempDirectory) {
        VideoRecorderConfiguration.tempDirectory = tempDirectory;
    }

    /**
     * It sets the x coordinate.
     * @param newX to be used.
     * @param newY to be used.
     */
    public static void setCoordinates(int newX, int newY) {
        setX(newX);
        setY(newY);
    }

    /**
     * @return the defaultDirectory
     */
    public static File getDefaultDirectory() {
        return defaultDirectory;
    }

    /**
     * @return the videoPath
     */
    public static File getVideoDirectory() {
        return videoDirectory;
    }

    /**
     * @param videoPath the videoPath to set
     */
    public static void setVideoDirectory(File videoPath) {
        VideoRecorderConfiguration.videoDirectory = videoPath;
    }

    public static final int getMaxWidth() {
        int maxWidth = 0;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle allScreenBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            maxWidth += screenBounds.width;
        }
        return maxWidth;
    }

    public static final int getMaxHeight() {
        int maxHeight = 0;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle allScreenBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            maxHeight += screenBounds.height;
        }
        return maxHeight;
    }
}

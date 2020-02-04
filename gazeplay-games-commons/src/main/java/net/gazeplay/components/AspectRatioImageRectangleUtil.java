package net.gazeplay.components;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class AspectRatioImageRectangleUtil {

    public void setFillImageKeepingAspectRatio(final Rectangle rectangle, final String imageResourceLocation,
                                               final Dimension2D gamingContextDimension2D) {
        final Image image = new Image(imageResourceLocation);
        setFillImageKeepingAspectRatio(rectangle, image, gamingContextDimension2D);
    }

    public void setFillImageKeepingAspectRatio(final Rectangle rectangle, final Image image, final Dimension2D gamingContextDimension2D) {
        final double imageWidth = image.getWidth();
        final double imageHeight = image.getHeight();
        final double imageHeightToWidthRatio = imageHeight / imageWidth;

        final double initialHeight = rectangle.getHeight();
        final double initialWidth = initialHeight / imageHeightToWidthRatio;

        final double positionX = (gamingContextDimension2D.getWidth() - initialWidth) / 2;
        final double positionY = (gamingContextDimension2D.getHeight() - initialHeight) / 2;

        rectangle.setFill(new ImagePattern(image));

        rectangle.setX(positionX);
        rectangle.setY(positionY);
        rectangle.setWidth(initialWidth);
        rectangle.setHeight(initialHeight);

        rectangle.setTranslateX(0);
        rectangle.setScaleX(1);
        rectangle.setScaleY(1);
        rectangle.setScaleZ(1);
    }

}

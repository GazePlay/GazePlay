package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class AspectRatioImageRectangleUtil {

    public void setFillImageKeepingAspectRatio(Rectangle rectangle, String imageResourceLocation,
            Dimension2D gamingContextDimension2D) {
        Image image = new Image(imageResourceLocation);
        setFillImageKeepingAspectRatio(rectangle, image, gamingContextDimension2D);
    }

    public void setFillImageKeepingAspectRatio(Rectangle rectangle, Image image, Dimension2D gamingContextDimension2D) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageHeightToWidthRatio = imageHeight / imageWidth;

        double initialHeight = rectangle.getHeight();
        double initialWidth = initialHeight / imageHeightToWidthRatio;

        double positionX = (gamingContextDimension2D.getWidth() - initialWidth) / 2;
        double positionY = (gamingContextDimension2D.getHeight() - initialHeight) / 2;

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

package net.gazeplay.games.magicpotions;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;

/**
 * @author Johana MARKU
 */
@Slf4j
class Explosion extends Rectangle {

    private static final String defaultPictureResourceLocation = "data/common/images/explosion.gif";

    private static final String defaultSoundResourceLocation = "data/common/sounds/explosion.mp3";

    @Setter
    private boolean enableRewardSound;

    Explosion(final IGameContext gameContext, final Dimension2D gameDimension/* , Image expImage */) {
        super(0, 0, 0, 0);
        this.enableRewardSound = gameContext.getConfiguration().isEnableRewardSound();

        final Image img = new Image(defaultPictureResourceLocation);
        final double imgWidth = img.getWidth();
        final double imgHeight = img.getHeight();

        final double posX = (gameDimension.getWidth() - imgWidth) / 2;
        final double posY = (gameDimension.getHeight() - imgHeight);

        setFill(new ImagePattern(img));
        setX(posX);
        setY(posY);
        setWidth(imgWidth);
        setHeight(imgHeight);

        setTranslateX(0);
        setScaleX(1);
        setScaleY(1);
        setScaleZ(1);

        setOpacity(1);

        log.debug("Playing graphic animation ...");

        if (this.enableRewardSound) {
            log.debug("Playing sound animation ...");
            try {
                ForegroundSoundsUtils.playSound(defaultSoundResourceLocation);
            } catch (final Exception e) {

                log.warn("file doesn't exist : {}", defaultSoundResourceLocation);
                log.warn(e.getMessage());
            }
        }
    }
}

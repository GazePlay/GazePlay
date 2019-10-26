package net.gazeplay.games.magicPotions;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.geometry.Dimension2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.Utils;

/**
 *
 * @author Johana MARKU
 *
 */

@Slf4j
public class Explosion extends Rectangle {

    private static final int soundClipDuration = 5000;

    private static final String defaultPictureResourceLocation = "data/common/images/explosion.gif";

    private static final String defaultSoundResourceLocation = "data/common/sounds/explosion.mp3";

    @Setter
    private boolean enableRewardSound;

    public Explosion(Dimension2D gameDimension/* , Image expImage */) {
        super(0, 0, 0, 0);
        this.enableRewardSound = Configuration.getInstance().isEnableRewardSound();

        Image img = new Image(defaultPictureResourceLocation);
        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();

        double posX = (gameDimension.getWidth() - imgWidth) / 2;
        double posY = (gameDimension.getHeight() - imgHeight);

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
                Utils.playSound(defaultSoundResourceLocation);
            } catch (Exception e) {

                log.warn("file doesn't exist : {}", defaultSoundResourceLocation);
                log.warn(e.getMessage());
            }
        }
    }
}
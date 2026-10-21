package net.gazeplay.ui.scenes.stats;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;

import java.io.File;

/**
 * Converts the screen recording of a game (AVI written by Monte Screen Recorder) into MP4, which JavaFX can play.
 * Only use of JAVE (ffmpeg).
 */
final class VideoConverter {

    private VideoConverter() {
    }

    /**
     * @return the MP4 file, next to the AVI file and with the same name
     */
    static File toMp4(final File aviFile) throws EncoderException {
        final String name = aviFile.getName();
        final String baseName = name.endsWith(".avi") ? name.substring(0, name.length() - ".avi".length()) : name;
        final File target = new File(aviFile.getParentFile(), baseName + ".mp4");

        final VideoAttributes videoAttributes = new VideoAttributes();
        videoAttributes.setCodec("mpeg4");

        final EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("mp4");
        attrs.setVideoAttributes(videoAttributes);

        new Encoder().encode(new MultimediaObject(aviFile), target, attrs);
        return target;
    }
}

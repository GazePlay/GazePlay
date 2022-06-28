package net.gazeplay.games.mediaPlayer;

import javafx.beans.property.SimpleIntegerProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MediaFileReader {

    private static final String PLAYER_LIST_CSV = "playerList.csv";

    private final IGameContext gameContext;

    @Getter
    private final List<MediaFile> mediaList;

    @Getter
    private SimpleIntegerProperty firstMediaDisplayedIndex;

    @Getter
    private SimpleIntegerProperty playingMediaIndex;

    MediaFileReader(IGameContext gameContext) {
        this.gameContext = gameContext;
        mediaList = new ArrayList<>();
        firstMediaDisplayedIndex = new SimpleIntegerProperty(-1);
        playingMediaIndex = new SimpleIntegerProperty(-1);

        final File mediaPlayerDirectory = getMediaPlayerDirectory();
        final File playlistFile = new File(mediaPlayerDirectory, PLAYER_LIST_CSV);

        if (playlistFile.exists()) {
            try (
                InputStream fileInputStream = Files.newInputStream(playlistFile.toPath());
                BufferedReader b = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
            ) {
                String readLine;
                while ((readLine = b.readLine()) != null) {
                    String[] split = readLine.split(",");
                    if (split.length == 3 || split[3] == null || split[3].isEmpty()) {
                        mediaList.add(new MediaFile(split[0], split[1], split[2], null));
                    } else if (split.length == 4) {
                        mediaList.add(new MediaFile(split[0], split[1], split[2], split[3]));
                    }
                    if (mediaList.size() > 0) {
                        firstMediaDisplayedIndex.setValue(0);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (mediaPlayerDirectory.exists() || mediaPlayerDirectory.mkdirs()) {
                try {
                    if (!playlistFile.createNewFile()) {
                        log.debug("Can't create file {}", playlistFile.getAbsoluteFile());
                    }
                } catch (IOException ioe) {
                    log.debug("Can't create file {}", playlistFile.getAbsoluteFile());
                    ioe.printStackTrace();
                }
            }
        }
    }

    public void setPlayingMediaIndex(int newIndex) {
        playingMediaIndex.setValue(newIndex);
    }

    public void next() {
        if (!mediaList.isEmpty()) {
            firstMediaDisplayedIndex.setValue((firstMediaDisplayedIndex.getValue() + 1) % mediaList.size());
        }
    }

    void previous() {
        if (!mediaList.isEmpty()) {
            firstMediaDisplayedIndex.setValue((firstMediaDisplayedIndex.getValue() - 1 + mediaList.size()) % mediaList.size());
        }
    }

    MediaFile mediaToPlayNext() {
        if (mediaList.isEmpty()) {
            return null;
        }
        playingMediaIndex.setValue((playingMediaIndex.getValue() + 1) % mediaList.size());
        return mediaList.get(playingMediaIndex.getValue());
    }


    MediaFile mediaToPlayPrevious() {
        if (mediaList.isEmpty()) {
            return null;
        }
        playingMediaIndex.setValue((playingMediaIndex.getValue() - 1 + mediaList.size()) % mediaList.size());
        return mediaList.get(playingMediaIndex.getValue());
    }

    int getIndexOfFirstToDisplay() {
        if (mediaList.isEmpty()) {
            return -1;
        }
        return firstMediaDisplayedIndex.getValue();
    }

    void addMedia(MediaFile mf) {
        final File mediaPlayerDirectory = getMediaPlayerDirectory();
        boolean mediaPlayerDirectoryCreated = mediaPlayerDirectory.mkdirs();
        log.debug("mediaPlayerDirectoryCreated = {}", mediaPlayerDirectoryCreated);
        final File playlistFile = new File(mediaPlayerDirectory, PLAYER_LIST_CSV);

        try {
            boolean fileIsUsable = playlistFile.exists();
            if (!fileIsUsable) {
                fileIsUsable = playlistFile.createNewFile();
            }

            if (fileIsUsable) {
                try (
                    OutputStream fileOutputStream = Files.newOutputStream(playlistFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))
                ) {
                    if (mediaList.size() == 0) {
                        bw.write("" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
                    } else {
                        bw.write("\n" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mediaList.add(mf);
        firstMediaDisplayedIndex.setValue(mediaList.size() - 1);
    }

    public File getMediaPlayerDirectory() {
        Configuration config = gameContext.getConfiguration();
        String userName = config.getUserName();
        return new File(GazePlayDirectories.getUserDataFolder(userName), "mediaPlayer");
    }

    public void deleteMedia(MediaFile mf) {
        final File mediaPlayerDirectory = getMediaPlayerDirectory();
        final File playlistFile = new File(mediaPlayerDirectory, PLAYER_LIST_CSV);

        if (playlistFile.exists()) {
            try {
                try (
                    OutputStream fileOutputStream = Files.newOutputStream(playlistFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))
                ) {
                    boolean firstDone = false;
                    for (MediaFile mediaFile : mediaList) {
                        if (mediaFile != mf) {
                            if (firstDone) {
                                bw.write("\n");
                            }
                            bw.write(
                                mediaFile.getType() + "," +
                                    mediaFile.getPath() + "," +
                                    mediaFile.getName() + "," +
                                    mediaFile.getImagepath()
                            );
                            log.info("writing {}", mediaFile.getName());
                            if (!firstDone) {
                                firstDone = true;
                            }
                        } else {
                            log.info("not writing {}", mediaFile.getName());
                        }
                    }
                    mediaList.remove(mf);
                    log.info("removing {}", mf.getName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

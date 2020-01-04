package net.gazeplay.games.mediaPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MediaFileReader {

    private static final String PLAYER_LIST_CSV = "playerList.csv";

    private final IGameContext gameContext;

    @Getter
    private final List<MediaFile> mediaList;

    private int index;

    @Getter
    @Setter
    private int playing;

    MediaFileReader(IGameContext gameContext) {
        this.gameContext = gameContext;
        mediaList = new ArrayList<>();
        index = -1;
        playing = -1;

        final File mediaPlayerDirectory = getMediaPlayerDirectory();
        final File playlistFile = new File(mediaPlayerDirectory, PLAYER_LIST_CSV);

        if (playlistFile.exists()) {
            try (
                FileInputStream fileInputStream = new FileInputStream(playlistFile);
                BufferedReader b = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
            ) {
                String readLine;
                while ((readLine = b.readLine()) != null) {
                    String[] split = readLine.split(",");
                    if (split.length == 3 || split[3] == null || split[3].equals("")) {
                        mediaList.add(new MediaFile(split[0], split[1], split[2], null));
                    } else {
                        mediaList.add(new MediaFile(split[0], split[1], split[2], split[3]));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public MediaFile next() {
        if (mediaList.isEmpty()) {
            return null;
        }
        index = (index + 1) % mediaList.size();
        return mediaList.get(index);
    }

    MediaFile previous() {
        if (mediaList.isEmpty()) {
            return null;
        }
        index = (index - 1 + mediaList.size()) % mediaList.size();
        return mediaList.get(index);
    }

    MediaFile nextPlayed() {
        if (mediaList.isEmpty()) {
            return null;
        }
        playing = (playing + 1) % mediaList.size();
        return mediaList.get(playing);
    }

    MediaFile prevPlayed() {
        if (mediaList.isEmpty()) {
            return null;
        }
        playing = (playing - 1 + mediaList.size()) % mediaList.size();
        return mediaList.get(playing);
    }

    void addMedia(MediaFile mf) {
        final File mediaPlayerDirectory = getMediaPlayerDirectory();
        boolean mediaPlayerDirectoryCreated = mediaPlayerDirectory.mkdirs();
        log.debug("mediaPlayerDirectoryCreated = {}", mediaPlayerDirectoryCreated);
        //
        final File playlistFile = new File(mediaPlayerDirectory, PLAYER_LIST_CSV);

        try (
            FileOutputStream fileOutputStream = new FileOutputStream(playlistFile, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))
        ) {
            if (mediaList.size() == 0) {
                bw.write("" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
            } else {
                bw.write("\n" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mediaList.add(mf);
    }

    private File getMediaPlayerDirectory() {
        Configuration config = gameContext.getConfiguration();
        String userName = config.getUserName();
        return new File(GazePlayDirectories.getUserDataFolder(userName), "mediaPlayer");
    }

}

package net.gazeplay.games.mediaPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class MediaFileReader {

    Configuration c;

    List<MediaFile> mediaList;

    int index;

    @Getter
    @Setter
    int playing;

    public MediaFileReader() {
        mediaList = new ArrayList<MediaFile>();
        index = -1;
        playing = -1;
        try {
            c = Configuration.getInstance();

            File f0 = new File(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + c.getUserName()
                    + Utils.FILESEPARATOR + "/data/mediaPlayer");
            f0.mkdirs();
            File f = new File(f0, "playerList.csv");
            f.createNewFile();

            BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));

            String readLine = "";

            while ((readLine = b.readLine()) != null) {
                String[] split = readLine.split(",");
                if (split.length == 3 || split[3] == null || split[3].equals("")) {
                    mediaList.add(new MediaFile(split[0], split[1], split[2], null));
                } else {
                    mediaList.add(new MediaFile(split[0], split[1], split[2], split[3]));
                }
            }

            b.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaFile next() {
        if (mediaList.size() > 0) {
            MediaFile mf = mediaList.get(index = (index + 1) % mediaList.size());
            return mf;
        }
        return null;
    }

    public MediaFile previous() {
        if (mediaList.size() > 0) {
            MediaFile mf = mediaList.get((index - 3 + 3 * mediaList.size()) % mediaList.size());
            index = (index - 1 + mediaList.size()) % mediaList.size();
            return mf;
        }
        return null;
    }

    public MediaFile nextPlayed() {
        if (mediaList.size() > 0) {
            playing = (playing + 1) % mediaList.size();
            return mediaList.get(playing);
        }
        return null;
    }

    public MediaFile prevPlayed() {
        if (mediaList.size() > 0) {
            playing = (playing - 1 + mediaList.size()) % mediaList.size();
            return mediaList.get(playing);
        }
        return null;
    }

    public void addMedia(MediaFile mf) {
        try {
            c = Configuration.getInstance();

            File f;

            if (c.getUserName() == null || c.getUserName().equals("")) {
                f = new File(Utils.getGazePlayFolder() + "data/mediaPlayer/playerList.csv");
            } else {
                f = new File(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + c.getUserName()
                        + Utils.FILESEPARATOR + "/data/mediaPlayer/playerList.csv");
            }

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF8"));

            if (mediaList.size() == 0) {
                bw.write("" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
            } else {
                bw.write("\n" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaList.add(mf);

    }
}

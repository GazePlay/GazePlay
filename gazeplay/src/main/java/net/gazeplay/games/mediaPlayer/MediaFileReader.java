package net.gazeplay.games.mediaPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class MediaFileReader {

    Configuration c;

    List<MediaFile> mediaList;

    int index;

    public MediaFileReader() {
        mediaList = new ArrayList<MediaFile>();
        index = -1;
        try {
            c = Configuration.getInstance();

            File f = new File(Utils.getGazePlayFolder() + "profiles" + Utils.FILESEPARATOR + c.getUserName()
                    + Utils.FILESEPARATOR + "/data/mediaPlayer/playerList.csv");

            BufferedReader b = new BufferedReader(new FileReader(f));

            String readLine = "";

            while ((readLine = b.readLine()) != null) {
                String[] split = readLine.split(",");
                mediaList.add(new MediaFile(split[0], split[1], split[2]));
            }

            b.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaFile next() {
        MediaFile mf = mediaList.get(index = (index + 1) % mediaList.size());
        return mf;
    }

    public MediaFile previous() {
        MediaFile mf = mediaList.get((index - 3 + mediaList.size()) % mediaList.size());
        index = (index - 1 + mediaList.size()) % mediaList.size();
        return mf;
    }

}

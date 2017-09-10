package utils.games.multilinguism;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Multilinguism {

        private static Multilinguism multilinguism;
        private HashMap<Entries, String> traductions;

        private Multilinguism(){

            traductions = new HashMap<>(1000);

            try {
                BufferedReader br = null;

                br = new BufferedReader(new FileReader("data/multilinguism/multilinguism.csv"));

                String ligne = null;

                boolean firstline = true;

                String[] languages = null, data = null ;

                while ((ligne = br.readLine()) != null){
                    if(firstline) {
                        // Retourner la ligne dans un tableau
                        languages = ligne.split(",");
                        firstline = false;
                    }
                    else {
                        data = ligne.split(",");
                        String key = data[0];
                        for (int i = 1; i < data.length; i++){

                            System.out.println(key + ", " + languages[i] + ", " +data[i]);
                            traductions.put(new Entries(key, languages[i]), data[i]);
                        }
                    }
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static Multilinguism getMultilinguism(){

            if(multilinguism == null)
                multilinguism = new Multilinguism();

            return multilinguism;
        }

        public String getTrad(String key, String language){

            return traductions.get(new Entries(key, language));
        };
}

class Entries{

    public String key;
    public String language;

    public Entries(String key, String language) {
        this.key = key;
        this.language = language;
    }
}

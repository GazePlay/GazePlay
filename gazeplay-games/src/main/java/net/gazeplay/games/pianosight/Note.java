package net.gazeplay.games.pianosight;

public class Note {

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public int key;
    private int octave;
    public int velocity;
    public long tick;

    public Note(int key, int velocity, long tick) {
        this.key = key;
        this.octave = (key / 12) - 1;
        this.velocity = velocity;
        this.tick = tick;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Note && this.key == ((Note) obj).key;
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Note -> " + this.octave + " key=" + this.key + " velocity=" + this.velocity + " tick=" + this.tick;
    }
}

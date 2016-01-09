package tk.lefourretoutsonore.lefourre_toutsonore;

import java.io.Serializable;

/**
 * Created by transpalette on 1/2/16.
 */
public class Song implements Serializable {
    private int id;
    private int likes;
    private String title;
    private String artist;
    private String styles;
    private String link;

    public Song(int id, int likes, String title, String artist, String styles, String link) {
        this.id = id;
        this.likes = likes;
        this.title = title;
        this.artist = artist;
        this.styles = styles;
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getStyles() {
        return styles;
    }

    public String getLink() {
        return link;
    }

    public int getLikes() {
        return likes;
    }
}

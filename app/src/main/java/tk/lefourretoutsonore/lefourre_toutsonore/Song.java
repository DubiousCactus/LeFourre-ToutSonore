package tk.lefourretoutsonore.lefourre_toutsonore;

import java.io.Serializable;

/**
 * Created by transpalette on 1/2/16.
 */
public class Song implements Serializable {
    private int id;
    private int likes;
    private int sharer;
    private String title;
    private String artist;
    private String styles;
    private String link;
    private String description;

    public Song(int id, int likes, String title, String artist, String styles, String link) {
        this.id = id;
        this.likes = likes;
        this.title = title;
        this.artist = artist;
        this.styles = styles;
        this.link = link;
    }

    public Song() {

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

    public String getDescription() {
        return description;
    }

    public int getSharer() {
        return sharer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setSharer(int sharer) {
        this.sharer = sharer;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

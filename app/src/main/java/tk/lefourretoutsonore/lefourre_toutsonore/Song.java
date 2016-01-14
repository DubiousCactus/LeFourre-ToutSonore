package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Context;
import java.io.Serializable;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;

/**
 * Created by transpalette on 1/2/16.
 */
public class Song implements Serializable {

    private int id;
    private int likes;
    private long sharer;
    private String title;
    private String artist;
    private String styles;
    private String link;
    private String description;
    private String coverUrl;
    private boolean liked;

    public Song(int id, int likes, long sharer, String title, String artist, String styles, String link, String description, boolean liked, PlayList playlist) {
        this.id = id;
        this.likes = likes;
        this.title = title;
        this.artist = artist;
        this.styles = styles;
        this.link = link;
        this.sharer = sharer;
        this.liked = liked;
        this.description = description;
        coverUrl = "";
    }

    public Song() {
    }



    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
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

    public boolean getLiked() {
        return liked;
    }

    public String getDescription() {
        return description;
    }

    public long getSharer() {
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

package tk.lefourretoutsonore.lefourre_toutsonore;

/**
 * Created by M4gicT0 on 09/01/2016.
 */
public class Style {
    private int genreId;
    private int id;
    private String name;

    public Style(int genreId, int id, String name) {
        this.genreId = genreId;
        this.id = id;
        this.name = name;
    }

    public int getGenreId() {
        return genreId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

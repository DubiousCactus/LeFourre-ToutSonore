package tk.lefourretoutsonore.lefourre_toutsonore;

import java.io.Serializable;

/**
 * Created by transpalette on 1/4/16.
 */
public class User implements Serializable {

    private String name;
    private long id;

    public User(String aName, long anId) {
        name = aName;
        id = anId;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
